/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2016 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.storage.service;

import com.google.common.base.Function;
import com.google.common.collect.*;
import org.apache.commons.io.FileUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.storage.RepoStorageSummaryInfo;
import org.artifactory.api.storage.BinariesInfo;
import org.artifactory.api.storage.StorageQuotaInfo;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.config.CentralConfigKey;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.gc.GcConfigDescriptor;
import org.artifactory.descriptor.quota.QuotaConfigDescriptor;
import org.artifactory.descriptor.repo.*;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.mbean.MBeanRegistrationService;
import org.artifactory.schedule.*;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.FileStoreStorageSummary;
import org.artifactory.storage.StorageSummaryInfo;
import org.artifactory.storage.binstore.service.BinaryService;
import org.artifactory.storage.binstore.service.BinaryStoreGarbageCollectorJob;
import org.artifactory.storage.binstore.service.InternalBinaryService;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.fs.repo.RepoStorageSummary;
import org.artifactory.storage.fs.service.FileService;
import org.artifactory.storage.mbean.ManagedStorage;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.common.config.diff.DataDiff;
import org.jfrog.storage.DbType;
import org.jfrog.storage.binstore.ifc.model.BinaryProvidersInfo;
import org.jfrog.storage.binstore.ifc.model.BinaryTreeElement;
import org.jfrog.storage.binstore.ifc.model.StorageInfo;
import org.jfrog.storage.common.StorageUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.artifactory.api.repo.storage.RepoStorageSummaryInfo.RepositoryType;

/**
 * @author yoavl
 */
@Service
@Reloadable(beanClass = InternalStorageService.class, initAfter = TaskService.class, listenOn = CentralConfigKey.gcConfig)
public class StorageServiceImpl implements InternalStorageService {
    private static final Logger log = LoggerFactory.getLogger(StorageServiceImpl.class);

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private DbService dbService;

    @Autowired
    private InternalBinaryService binaryStore;

    @Autowired
    private FileService fileService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    private boolean derbyUsed;

    @Override
    public void compress(BasicStatusHolder statusHolder) {
        if (!derbyUsed) {
            statusHolder.error("Compress command is not supported on current database type.", log);
            return;
        }
        logStorageSizes();
        dbService.compressDerbyDb(statusHolder);
        logStorageSizes();
    }

    @Override
    public void logStorageSizes() {
        ArtifactoryHome artifactoryHome = ContextHelper.get().getArtifactoryHome();
        File derbyDirectory = new File(artifactoryHome.getDataDir(), "derby");
        long sizeOfDirectory = FileUtils.sizeOfDirectory(derbyDirectory);
        log.info("Derby database storage size: {} ({})", StorageUnit.toReadableString(sizeOfDirectory), derbyDirectory);
    }

    @Override
    public void ping() {
        binaryStore.ping();
    }

    @Override
    public BinaryProvidersInfo<Map<String, String>> getBinaryProviderInfo() {
        BinaryProvidersInfo<Map<String, String>> binaryProvidersInfo = binaryStore.getBinaryProvidersInfo();
        BinaryTreeElement<Map<String, String>> rootElement = binaryProvidersInfo.rootTreeElement;
        // Add the quota limits to the result
        QuotaConfigDescriptor quotaConfig = centralConfigService.getDescriptor().getQuotaConfig();
        if (quotaConfig != null && quotaConfig.isEnabled()) {
            int quotaErrorLimit = quotaConfig.getDiskSpaceLimitPercentage();
            int quotaWarningLimit = quotaConfig.getDiskSpaceWarningPercentage();
            rootElement.getData().put("quotaErrorLimit", "" + quotaErrorLimit);
            rootElement.getData().put("quotaWarningLimit", "" + quotaWarningLimit);
        }
        return binaryProvidersInfo;
    }

    @Override
    public FileStoreStorageSummary getFileStoreStorageSummary() {
        List<File> binariesDirs = Lists.newArrayList();
        File binariesDir = binaryStore.getBinariesDir();
        if (binariesDir != null) {
            binariesDirs.add(binariesDir);
        }
        BinaryService binaryService = ContextHelper.get().beanForType(BinaryService.class);
        BinaryProvidersInfo<Map<String, String>> binaryProvidersInfo = binaryService.getBinaryProvidersInfo();

        return new FileStoreStorageSummary(binariesDirs, binaryProvidersInfo);
    }

    @Override
    public StorageQuotaInfo getStorageQuotaInfo(long fileContentLength) {
        CentralConfigDescriptor descriptor = centralConfigService.getDescriptor();
        QuotaConfigDescriptor quotaConfig = descriptor.getQuotaConfig();
        if (quotaConfig == null) {
            return null;
        }
        if (!quotaConfig.isEnabled()) {
            return null;
        }
        StorageInfo storageInfo = binaryStore.getStorageInfoSummary();
        long freeSpace = storageInfo.getFreeSpace();
        long totalSpace = storageInfo.getTotalSpace();
        long usageSpace = storageInfo.getUsedSpace();
        return new StorageQuotaInfo(freeSpace, totalSpace, usageSpace, quotaConfig.getDiskSpaceLimitPercentage(),
                quotaConfig.getDiskSpaceWarningPercentage());
    }

    @Override
    public StorageSummaryInfo getStorageSummaryInfo() {
        List<RepoDescriptor> repos = Lists.newArrayList();
        repos.addAll(repositoryService.getLocalAndCachedRepoDescriptors());
        repos.addAll(repositoryService.getVirtualRepoDescriptors());
        repos.addAll(repositoryService.getDistributionRepoDescriptors());
        Set<RepoStorageSummary> summaries = fileService.getRepositoriesStorageSummary();
        final ImmutableMap<String, RepoDescriptor> reposMap =
                Maps.uniqueIndex(repos, new Function<RepoDescriptor, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable RepoDescriptor input) {
                        if (input == null) {
                            return null;
                        }
                        return input.getKey();
                    }
                });
        Iterable<RepoStorageSummaryInfo> infos = Iterables.transform(summaries,
                new Function<RepoStorageSummary, RepoStorageSummaryInfo>() {
                    @Override
                    public RepoStorageSummaryInfo apply(RepoStorageSummary r) {
                        RepositoryType repoType = getRepoType(r.getRepoKey(), reposMap);
                        RepoDescriptor repoDescriptor = reposMap.get(r.getRepoKey());
                        String repoTypeName = "NA";
                        if (repoDescriptor != null) {
                            repoTypeName = repoDescriptor.getType().getDisplayName();
                        }
                        return new RepoStorageSummaryInfo(
                                r.getRepoKey(), repoType, r.getFoldersCount(), r.getFilesCount(), r.getUsedSpace(),
                                repoTypeName);
                    }

                    private RepositoryType getRepoType(String repoKey,
                                                       ImmutableMap<String, RepoDescriptor> repoDescriptors) {
                        RepoDescriptor repoDescriptor = repoDescriptors.get(repoKey);
                        if (repoDescriptor == null) {
                            return RepositoryType.NA;
                        } else if (repoDescriptor instanceof RemoteRepoDescriptor) {
                            return RepositoryType.REMOTE;
                        } else if (repoDescriptor instanceof VirtualRepoDescriptor) {
                            return RepositoryType.VIRTUAL;
                        } else if (repoDescriptor instanceof LocalCacheRepoDescriptor) {
                            return RepositoryType.CACHE;
                        } else if (repoDescriptor instanceof DistributionRepoDescriptor) {
                            return RepositoryType.DISTRIBUTION;
                        } else if (repoDescriptor instanceof LocalRepoDescriptor) {
                            return RepositoryType.LOCAL;
                        } else {
                            return RepositoryType.NA;
                        }
                    }
                }
        );

        BinariesInfo binariesInfo = binaryStore.getBinariesInfo();

        return new StorageSummaryInfo(Sets.newHashSet(infos), binariesInfo);
    }

    @Override
    public void forceOptimizationOnce() {
        binaryStore.forceOptimizationOnce();
    }

    @Override
    public void callManualGarbageCollect(BasicStatusHolder statusHolder) {
        taskService.checkCanStartManualTask(BinaryStoreGarbageCollectorJob.class, statusHolder);
        if (!statusHolder.isError()) {
            try {
                execOneGcAndWait(true);
            } catch (Exception e) {
                statusHolder.error("Error activating Artifactory Storage Garbage Collector: " + e.getMessage(), e,
                        log);
            }
        }
    }

    @Override
    public void pruneUnreferencedFileInDataStore(BasicStatusHolder statusHolder) {
        binaryStore.prune(statusHolder);
    }

    private String execOneGcAndWait(boolean waitForCompletion) {
        TaskBase task = TaskUtils.createManualTask(BinaryStoreGarbageCollectorJob.class, 0L);
        String token = taskService.startTask(task, true, true);
        if (waitForCompletion) {
            taskService.waitForTaskCompletion(token);
        }
        return token;
    }

    @Override
    public boolean isDerbyUsed() {
        return derbyUsed;
    }

    @Override
    public void init() {
        derbyUsed = dbService.getDatabaseType() == DbType.DERBY;

        ContextHelper.get().beanForType(MBeanRegistrationService.class).
                register(new ManagedStorage(binaryStore), "Storage", "Binary Storage");

        CentralConfigDescriptor descriptor = centralConfigService.getDescriptor();
        new GcSchedulerHandler(descriptor.getGcConfig(), null).reschedule();
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor, List<DataDiff<?>> configDiff) {
        CentralConfigDescriptor descriptor = centralConfigService.getDescriptor();
        new GcSchedulerHandler(descriptor.getGcConfig(), oldDescriptor.getGcConfig()).reschedule();
    }

    @Override
    public void destroy() {
        new GcSchedulerHandler(null, null).unschedule();
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        //nop
    }

    static class GcSchedulerHandler extends BaseTaskServiceDescriptorHandler<GcConfigDescriptor> {

        final List<GcConfigDescriptor> oldDescriptorHolder = Lists.newArrayList();
        final List<GcConfigDescriptor> newDescriptorHolder = Lists.newArrayList();

        GcSchedulerHandler(GcConfigDescriptor newDesc, GcConfigDescriptor oldDesc) {
            if (newDesc != null) {
                newDescriptorHolder.add(newDesc);
            }
            if (oldDesc != null) {
                oldDescriptorHolder.add(oldDesc);
            }
        }

        @Override
        public String jobName() {
            return "Garbage Collector";
        }

        @Override
        public List<GcConfigDescriptor> getNewDescriptors() {
            return newDescriptorHolder;
        }

        @Override
        public List<GcConfigDescriptor> getOldDescriptors() {
            return oldDescriptorHolder;
        }

        @Override
        public Predicate<Task> getAllPredicate() {
            return input -> (input != null) && BinaryStoreGarbageCollectorJob.class.isAssignableFrom(input.getType());
        }

        @Override
        public Predicate<Task> getPredicate(@Nonnull GcConfigDescriptor descriptor) {
            return getAllPredicate();
        }

        @Override
        public void activate(@Nonnull GcConfigDescriptor descriptor, boolean manual) {
            AddonsManager addonsManager = InternalContextHelper.get().beanForType(AddonsManager.class);
            CoreAddons coreAddons = addonsManager.addonByType(CoreAddons.class);
            TaskBase garbageCollectorTask;
            if (coreAddons.isAol()) {
                garbageCollectorTask = TaskUtils.createRepeatingTask(BinaryStoreGarbageCollectorJob.class,
                        TimeUnit.SECONDS.toMillis(ConstantValues.gcIntervalSecs.getLong()),
                        TimeUnit.SECONDS.toMillis(ConstantValues.gcDelaySecs.getLong()));
            } else {
                garbageCollectorTask = TaskUtils.createCronTask(BinaryStoreGarbageCollectorJob.class,
                        descriptor.getCronExp(), "Binaries Garbage Collector");
            }
            InternalContextHelper.get().getTaskService().startTask(garbageCollectorTask, manual);
        }

        @Override
        public GcConfigDescriptor findOldFromNew(@Nonnull GcConfigDescriptor newDescriptor) {
            return oldDescriptorHolder.isEmpty() ? null : oldDescriptorHolder.get(0);
        }
    }
}