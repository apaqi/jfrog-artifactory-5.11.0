package org.artifactory.release.bundle;

import org.artifactory.api.release.bundle.ReleaseBundleService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.storage.db.bundle.dao.ArtifactBundlesDao;
import org.artifactory.storage.db.bundle.dao.BundleBlobsDao;
import org.artifactory.storage.db.bundle.model.ArtifactsBundle;
import org.artifactory.storage.db.bundle.model.BundleNode;
import org.artifactory.storage.db.bundle.model.BundleTransactionStatus;
import org.jfrog.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static org.artifactory.util.distribution.DistributionConstants.IN_TRANSIT_REPO_KEY;

@Service
public class ReleaseBundleServiceImpl implements ReleaseBundleService {
    private static final Logger log = LoggerFactory.getLogger(ReleaseBundleServiceImpl.class);

    @Autowired
    private ArtifactBundlesDao bundlesDao;
    @Autowired
    private BundleBlobsDao blobsDao;
    @Autowired
    private RepositoryService repoService;

    @Override
    @Nonnull
    public List<Long> findExpiredBundlesIds() throws StorageException {
        try {
            return bundlesDao.getExpiredBundlesIds();
        } catch (SQLException e) {
            throw new StorageException("Could not get expired Release Bundle transactions", e);
        }
    }

    @Override
    public boolean deleteBundle(Long id, boolean deleteContent) throws StorageException {
        log.info("Deleting Release Bundle with id '" + id + "'" + (deleteContent ? " including content" : ""));
        try {
            ArtifactsBundle artifactsBundle = bundlesDao.getArtifactsBundle(id);
            if (artifactsBundle == null) {
                return false;
            }
            List<BundleNode> bundleNodes = bundlesDao.getBundleNodes(artifactsBundle.getId());
            if (!bundleNodes.isEmpty()) {
                bundlesDao.deleteBundleNodes(bundleNodes.stream()
                        .map(BundleNode::getId)
                        .collect(Collectors.toList()));
            }
            if (deleteContent) {
                deleteBundleContent(artifactsBundle, bundleNodes);
            }
            blobsDao.deleteBlob(artifactsBundle.getId());
            bundlesDao.deleteArtifactsBundle(artifactsBundle.getId());
            return true;
        } catch (SQLException e) {
            throw new StorageException("Could not delete Release Bundle", e);
        }
    }

    @Override
    public boolean redistributeBundle(Long id) throws StorageException {
        log.info("Redistributing Release Bundle with id '" + id + "'");
        try {
            return bundlesDao.updateArtifactsBundleCreationDate(id) > 0;
        } catch (SQLException e) {
            throw new StorageException("Could not redistribute Release Bundle with id '" + id + "'", e);
        }
    }

    private void deleteBundleContent(ArtifactsBundle artifactsBundle, List<BundleNode> bundleNodes) throws SQLException {
        if (BundleTransactionStatus.INPROGRESS.equals(artifactsBundle.getStatus())) {
            // Delete the temp folder and all of its content
            String txPath = IN_TRANSIT_REPO_KEY + "/" + artifactsBundle.getName() + "/" + artifactsBundle.getVersion();
            repoService.undeploy(RepoPathFactory.create(txPath));
        } else {
            for (BundleNode bundleNode : bundleNodes) {
                // If the artifact is related to a different bundle, skip deletion
                if (bundlesDao.isNodeRelatedToBundle(bundleNode.getNodeId())) {
                    log.info("Artifact " + bundleNode.getRepoPath() + " is related to a different release bundle and will not be deleted.");
                    continue;
                }
                repoService.undeploy(RepoPathFactory.create(bundleNode.getRepoPath()));
            }
        }
    }
}
