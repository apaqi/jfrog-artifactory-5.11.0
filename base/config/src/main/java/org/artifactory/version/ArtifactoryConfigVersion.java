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

package org.artifactory.version;

import org.artifactory.version.converter.NamespaceConverter;
import org.artifactory.version.converter.SnapshotUniqueVersionConverter;
import org.artifactory.version.converter.XmlConverter;
import org.artifactory.version.converter.v100.BackupToElementConverter;
import org.artifactory.version.converter.v100.RepositoriesKeysConverter;
import org.artifactory.version.converter.v110.SnapshotNonUniqueValueConverter;
import org.artifactory.version.converter.v120.AnonAccessNameConverter;
import org.artifactory.version.converter.v130.AnnonAccessUnderSecurityConverter;
import org.artifactory.version.converter.v130.BackupListConverter;
import org.artifactory.version.converter.v130.LdapSettings130Converter;
import org.artifactory.version.converter.v131.LdapAuthenticationPatternsConverter;
import org.artifactory.version.converter.v132.BackupKeyConverter;
import org.artifactory.version.converter.v132.LdapListConverter;
import org.artifactory.version.converter.v134.BackupExcludedVirtualRepoConverter;
import org.artifactory.version.converter.v135.ProxyNTHostConverter;
import org.artifactory.version.converter.v136.IndexerCronRemoverConverter;
import org.artifactory.version.converter.v136.RepositoryTypeConverter;
import org.artifactory.version.converter.v141.ProxyDefaultConverter;
import org.artifactory.version.converter.v1410.GcSystemPropertyConverter;
import org.artifactory.version.converter.v1412.IndexerCronExpPropertyConverter;
import org.artifactory.version.converter.v1414.ArchiveBrowsingConverter;
import org.artifactory.version.converter.v1414.AssumedOfflineConverter;
import org.artifactory.version.converter.v1414.CleanupConfigConverter;
import org.artifactory.version.converter.v142.RepoIncludeExcludePatternsConverter;
import org.artifactory.version.converter.v143.RemoteChecksumPolicyConverter;
import org.artifactory.version.converter.v144.MultiLdapXmlConverter;
import org.artifactory.version.converter.v144.ServerIdXmlConverter;
import org.artifactory.version.converter.v147.DefaultRepoLayoutConverter;
import org.artifactory.version.converter.v147.JfrogRemoteRepoUrlConverter;
import org.artifactory.version.converter.v147.UnusedArtifactCleanupSwitchConverter;
import org.artifactory.version.converter.v149.ReplicationElementNameConverter;
import org.artifactory.version.converter.v152.BlackDuckProxyConverter;
import org.artifactory.version.converter.v153.VirtualCacheCleanupConverter;
import org.artifactory.version.converter.v160.AddonsDefaultLayoutConverter;
import org.artifactory.version.converter.v160.MavenIndexerConverter;
import org.artifactory.version.converter.v160.SingleRepoTypeConverter;
import org.artifactory.version.converter.v160.SuppressConsitencyConverter;
import org.artifactory.version.converter.v162.FolderDownloadConfigConverter;
import org.artifactory.version.converter.v166.SourceDeletedDetectionConverter;
import org.artifactory.version.converter.v167.TrashcanConfigConverter;
import org.artifactory.version.converter.v167.UserLockConfigConverter;
import org.artifactory.version.converter.v168.PasswordPolicyConverter;
import org.artifactory.version.converter.v169.PasswordMaxAgeConverter;
import org.artifactory.version.converter.v171.SimpleLayoutConverter;
import org.artifactory.version.converter.v172.BlockMismatchingMimeTypesConverter;
import org.artifactory.version.converter.v175.DockerForceAuthRemovalConverter;
import org.artifactory.version.converter.v177.LdapPoisoningProtectionConverter;
import org.artifactory.version.converter.v178.SigningKeysConverter;
import org.artifactory.version.converter.v180.ExternalProvidersRemovalConverter;
import org.artifactory.version.converter.v180.XrayRepoConfigConverter;
import org.artifactory.version.converter.v181.ComposerDefaultLayoutConverter;
import org.artifactory.version.converter.v182.ConanDefaultLayoutConverter;
import org.artifactory.version.converter.v201.PuppetDefaultLayoutConverter;
import org.artifactory.version.converter.v204.AccessTokenSettingsRenameToAccessClientSettingsConverter;
import org.artifactory.version.converter.v204.EventBasedRemoteReplicationConverter;
import org.artifactory.version.converter.v205.YumEnableFilelistsIndexingForExistingLocalReposConverter;
import org.artifactory.version.converter.v207.FolderDownloadForAnonymousConfigConverter;
import org.artifactory.version.converter.v207.RemoveAccessAdminCredentialsConverter;
import org.artifactory.version.converter.v208.AddReplicationKey;
import org.artifactory.version.converter.v211.XrayMinBlockedSeverityAndBlockUnscannedConverter;
import org.artifactory.version.converter.v212.PyPIRegistryUrlConverter;
import org.artifactory.version.converter.v213.AllowCrowdUsersToAccessProfilePageConverter;
import org.artifactory.version.converter.v213.GoDefaultLayoutConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * NOTE! each version declares the converters that should run when moving forward *from* it, meaning for example that
 * v180 starts at 4.12.1 (denoted v4121) up to 4.13.2 and the converter that needs to run for it
 * is XrayRepoConfigConverter (which is declared in v171) --> when moving forward from it to v181
 * ComposerDefaultLayoutConverter will run.
 *
 * @author freds
 * @author Yossi Shaul
 */
public enum ArtifactoryConfigVersion implements SubConfigElementVersion {
    v100("http://artifactory.jfrog.org/xsd/1.0.0",
            "http://www.jfrog.org/xsd/artifactory-v1_0_0.xsd",
            ArtifactoryVersion.v122rc0,
            ArtifactoryVersion.v125rc6,
            new SnapshotUniqueVersionConverter(),
            new BackupToElementConverter(),
            new RepositoriesKeysConverter()),
    v110("http://artifactory.jfrog.org/xsd/1.1.0",
            "http://www.jfrog.org/xsd/artifactory-v1_1_0.xsd",
            ArtifactoryVersion.v125,
            ArtifactoryVersion.v125,
            new SnapshotNonUniqueValueConverter()),
    v120("http://artifactory.jfrog.org/xsd/1.2.0",
            "http://www.jfrog.org/xsd/artifactory-v1_2_0.xsd",
            ArtifactoryVersion.v125u1,
            ArtifactoryVersion.v125u1,
            new AnonAccessNameConverter()),
    v130("http://artifactory.jfrog.org/xsd/1.3.0",
            "http://www.jfrog.org/xsd/artifactory-v1_3_0.xsd",
            ArtifactoryVersion.v130beta1,
            ArtifactoryVersion.v130beta2,
            new BackupListConverter(), new AnnonAccessUnderSecurityConverter(),
            new LdapSettings130Converter()),
    v131("http://artifactory.jfrog.org/xsd/1.3.1",
            "http://www.jfrog.org/xsd/artifactory-v1_3_1.xsd",
            ArtifactoryVersion.v130beta3,
            ArtifactoryVersion.v130beta3,
            new LdapAuthenticationPatternsConverter()),
    v132("http://artifactory.jfrog.org/xsd/1.3.2",
            "http://www.jfrog.org/xsd/artifactory-v1_3_2.xsd",
            ArtifactoryVersion.v130beta4,
            ArtifactoryVersion.v130beta4,
            new BackupKeyConverter(), new LdapListConverter()),
    v133("http://artifactory.jfrog.org/xsd/1.3.3",
            "http://www.jfrog.org/xsd/artifactory-v1_3_3.xsd",
            ArtifactoryVersion.v130beta5,
            ArtifactoryVersion.v130beta61),
    v134("http://artifactory.jfrog.org/xsd/1.3.4",
            "http://www.jfrog.org/xsd/artifactory-v1_3_4.xsd",
            ArtifactoryVersion.v130rc1,
            ArtifactoryVersion.v130rc1,
            new BackupExcludedVirtualRepoConverter()),
    v135("http://artifactory.jfrog.org/xsd/1.3.5",
            "http://www.jfrog.org/xsd/artifactory-v1_3_5.xsd",
            ArtifactoryVersion.v130rc2,
            ArtifactoryVersion.v205,
            new ProxyNTHostConverter()),
    v136("http://artifactory.jfrog.org/xsd/1.3.6",
            "http://www.jfrog.org/xsd/artifactory-v1_3_6.xsd",
            ArtifactoryVersion.v206,
            ArtifactoryVersion.v208,
            new IndexerCronRemoverConverter(), new RepositoryTypeConverter()),
    v140("http://artifactory.jfrog.org/xsd/1.4.0",
            "http://www.jfrog.org/xsd/artifactory-v1_4_0.xsd",
            ArtifactoryVersion.v210,
            ArtifactoryVersion.v210, new ProxyDefaultConverter()),
    v141("http://artifactory.jfrog.org/xsd/1.4.1",
            "http://www.jfrog.org/xsd/artifactory-v1_4_1.xsd",
            ArtifactoryVersion.v211,
            ArtifactoryVersion.v212,
            new RepoIncludeExcludePatternsConverter()),
    v142("http://artifactory.jfrog.org/xsd/1.4.2",
            "http://www.jfrog.org/xsd/artifactory-v1_4_2.xsd",
            ArtifactoryVersion.v213,
            ArtifactoryVersion.v221,
            new RemoteChecksumPolicyConverter()),
    v143("http://artifactory.jfrog.org/xsd/1.4.3",
            "http://www.jfrog.org/xsd/artifactory-v1_4_3.xsd",
            ArtifactoryVersion.v222,
            ArtifactoryVersion.v223,
            new MultiLdapXmlConverter(), new ServerIdXmlConverter()),
    v144("http://artifactory.jfrog.org/xsd/1.4.4",
            "http://www.jfrog.org/xsd/artifactory-v1_4_4.xsd",
            ArtifactoryVersion.v224,
            ArtifactoryVersion.v225),
    v145("http://artifactory.jfrog.org/xsd/1.4.5",
            "http://www.jfrog.org/xsd/artifactory-v1_4_5.xsd",
            ArtifactoryVersion.v230,
            ArtifactoryVersion.v230),
    v146("http://artifactory.jfrog.org/xsd/1.4.6",
            "http://www.jfrog.org/xsd/artifactory-v1_4_6.xsd",
            ArtifactoryVersion.v231,
            ArtifactoryVersion.v231, new JfrogRemoteRepoUrlConverter(), new DefaultRepoLayoutConverter(),
            new UnusedArtifactCleanupSwitchConverter()),
    v147("http://artifactory.jfrog.org/xsd/1.4.7",
            "http://www.jfrog.org/xsd/artifactory-v1_4_7.xsd",
            ArtifactoryVersion.v232,
            ArtifactoryVersion.v232),
    v148("http://artifactory.jfrog.org/xsd/1.4.8",
            "http://www.jfrog.org/xsd/artifactory-v1_4_8.xsd",
            ArtifactoryVersion.v233,
            ArtifactoryVersion.v2331, new ReplicationElementNameConverter()),
    v149("http://artifactory.jfrog.org/xsd/1.4.9",
            "http://www.jfrog.org/xsd/artifactory-v1_4_9.xsd",
            ArtifactoryVersion.v234,
            ArtifactoryVersion.v2341, new GcSystemPropertyConverter()),
    v1410("http://artifactory.jfrog.org/xsd/1.4.10",
            "http://www.jfrog.org/xsd/artifactory-v1_4_10.xsd",
            ArtifactoryVersion.v240,
            ArtifactoryVersion.v242),
    v1411("http://artifactory.jfrog.org/xsd/1.4.11",
            "http://www.jfrog.org/xsd/artifactory-v1_4_11.xsd",
            ArtifactoryVersion.v250,
            ArtifactoryVersion.v250, new IndexerCronExpPropertyConverter()),
    v1412("http://artifactory.jfrog.org/xsd/1.4.12",
            "http://www.jfrog.org/xsd/artifactory-v1_4_12.xsd",
            ArtifactoryVersion.v251,
            ArtifactoryVersion.v2511),
    v1413("http://artifactory.jfrog.org/xsd/1.4.13",
            "http://www.jfrog.org/xsd/artifactory-v1_4_13.xsd",
            ArtifactoryVersion.v252,
            ArtifactoryVersion.v252, new CleanupConfigConverter(), new AssumedOfflineConverter(),
            new ArchiveBrowsingConverter()),
    v1414("http://artifactory.jfrog.org/xsd/1.4.14",
            "http://www.jfrog.org/xsd/artifactory-v1_4_14.xsd",
            ArtifactoryVersion.v260,
            ArtifactoryVersion.v261),
    v1415("http://artifactory.jfrog.org/xsd/1.4.15",
            "http://www.jfrog.org/xsd/artifactory-v1_4_15.xsd",
            ArtifactoryVersion.v262,
            ArtifactoryVersion.v263),
    v1416("http://artifactory.jfrog.org/xsd/1.4.16",
            "http://www.jfrog.org/xsd/artifactory-v1_4_16.xsd",
            ArtifactoryVersion.v264,
            ArtifactoryVersion.v264),
    v1417("http://artifactory.jfrog.org/xsd/1.4.17",
            "http://www.jfrog.org/xsd/artifactory-v1_4_17.xsd",
            ArtifactoryVersion.v265,
            ArtifactoryVersion.v265),
    v1418("http://artifactory.jfrog.org/xsd/1.4.18",
            "http://www.jfrog.org/xsd/artifactory-v1_4_18.xsd",
            ArtifactoryVersion.v266,
            ArtifactoryVersion.v2671),
    v150("http://artifactory.jfrog.org/xsd/1.5.0",
            "http://www.jfrog.org/xsd/artifactory-v1_5_0.xsd",
            ArtifactoryVersion.v300,
            ArtifactoryVersion.v302),
    v151("http://artifactory.jfrog.org/xsd/1.5.1",
            "http://www.jfrog.org/xsd/artifactory-v1_5_1.xsd",
            ArtifactoryVersion.v3021,
            ArtifactoryVersion.v303),
    v152("http://artifactory.jfrog.org/xsd/1.5.2",
            "http://www.jfrog.org/xsd/artifactory-v1_5_2.xsd",
            ArtifactoryVersion.v304,
            ArtifactoryVersion.v304, new BlackDuckProxyConverter()),
    v153("http://artifactory.jfrog.org/xsd/1.5.3",
            "http://www.jfrog.org/xsd/artifactory-v1_5_3.xsd",
            ArtifactoryVersion.v310,
            ArtifactoryVersion.v3111, new VirtualCacheCleanupConverter()),
    v154("http://artifactory.jfrog.org/xsd/1.5.4",
            "http://www.jfrog.org/xsd/artifactory-v1_5_4.xsd",
            ArtifactoryVersion.v320,
            ArtifactoryVersion.v322),
    v155("http://artifactory.jfrog.org/xsd/1.5.5",
            "http://www.jfrog.org/xsd/artifactory-v1_5_5.xsd",
            ArtifactoryVersion.v330,
            ArtifactoryVersion.v331),
    v156("http://artifactory.jfrog.org/xsd/1.5.6",
            "http://www.jfrog.org/xsd/artifactory-v1_5_6.xsd",
            ArtifactoryVersion.v340,
            ArtifactoryVersion.v341),
    v157("http://artifactory.jfrog.org/xsd/1.5.7",
            "http://www.jfrog.org/xsd/artifactory-v1_5_7.xsd",
            ArtifactoryVersion.v342,
            ArtifactoryVersion.v342),
    v158("http://artifactory.jfrog.org/xsd/1.5.8",
            "http://www.jfrog.org/xsd/artifactory-v1_5_8.xsd",
            ArtifactoryVersion.v350,
            ArtifactoryVersion.v350),
    v159("http://artifactory.jfrog.org/xsd/1.5.9",
            "http://www.jfrog.org/xsd/artifactory-v1_5_9.xsd",
            ArtifactoryVersion.v351,
            ArtifactoryVersion.v353),
    v1510("http://artifactory.jfrog.org/xsd/1.5.10",
            "http://www.jfrog.org/xsd/artifactory-v1_5_10.xsd",
            ArtifactoryVersion.v360,
            ArtifactoryVersion.v360),
    v1511("http://artifactory.jfrog.org/xsd/1.5.11",
            "http://www.jfrog.org/xsd/artifactory-v1_5_11.xsd",
            ArtifactoryVersion.v370,
            ArtifactoryVersion.v370),
    v1512("http://artifactory.jfrog.org/xsd/1.5.12",
            "http://www.jfrog.org/xsd/artifactory-v1_5_12.xsd",
            ArtifactoryVersion.v380,
            ArtifactoryVersion.v380),
    v1513("http://artifactory.jfrog.org/xsd/1.5.13",
            "http://www.jfrog.org/xsd/artifactory-v1_5_13.xsd",
            ArtifactoryVersion.v390,
            ArtifactoryVersion.v395, new AddonsDefaultLayoutConverter(), new SingleRepoTypeConverter(),
            new SuppressConsitencyConverter(),new MavenIndexerConverter()),
    v160("http://artifactory.jfrog.org/xsd/1.6.0",
            "http://www.jfrog.org/xsd/artifactory-v1_6_0.xsd",
            ArtifactoryVersion.v400,
            ArtifactoryVersion.v400),
    v161("http://artifactory.jfrog.org/xsd/1.6.1",
            "http://www.jfrog.org/xsd/artifactory-v1_6_1.xsd",
            ArtifactoryVersion.v401,
            ArtifactoryVersion.v402, new FolderDownloadConfigConverter()),
    v162("http://artifactory.jfrog.org/xsd/1.6.2",
            "http://www.jfrog.org/xsd/artifactory-v1_6_2.xsd",
            ArtifactoryVersion.v410,
            ArtifactoryVersion.v412),
    v163("http://artifactory.jfrog.org/xsd/1.6.3",
            "http://www.jfrog.org/xsd/artifactory-v1_6_3.xsd",
            ArtifactoryVersion.v413,
            ArtifactoryVersion.v422),
    v164("http://artifactory.jfrog.org/xsd/1.6.4",
            "http://www.jfrog.org/xsd/artifactory-v1_6_4.xsd",
            ArtifactoryVersion.v430,
            ArtifactoryVersion.v430),
    v165("http://artifactory.jfrog.org/xsd/1.6.5",
            "http://www.jfrog.org/xsd/artifactory-v1_6_5.xsd",
            ArtifactoryVersion.v431,
            ArtifactoryVersion.v432, new SourceDeletedDetectionConverter()),
    v166("http://artifactory.jfrog.org/xsd/1.6.6",
            "http://www.jfrog.org/xsd/artifactory-v1_6_6.xsd",
            ArtifactoryVersion.v433,
            ArtifactoryVersion.v433, new UserLockConfigConverter(), new TrashcanConfigConverter()),
    v167("http://artifactory.jfrog.org/xsd/1.6.7",
            "http://www.jfrog.org/xsd/artifactory-v1_6_7.xsd",
            ArtifactoryVersion.v440,
            ArtifactoryVersion.v440, new PasswordPolicyConverter()),
    v168("http://artifactory.jfrog.org/xsd/1.6.8",
            "http://www.jfrog.org/xsd/artifactory-v1_6_8.xsd",
            ArtifactoryVersion.v441,
            ArtifactoryVersion.v441, new PasswordMaxAgeConverter()),
    v169("http://artifactory.jfrog.org/xsd/1.6.9",
            "http://www.jfrog.org/xsd/artifactory-v1_6_9.xsd",
            ArtifactoryVersion.v442,
            ArtifactoryVersion.v442),
    v170("http://artifactory.jfrog.org/xsd/1.7.0",
            "http://www.jfrog.org/xsd/artifactory-v1_7_0.xsd",
            ArtifactoryVersion.v443,
            ArtifactoryVersion.v452, new SimpleLayoutConverter()),
    v171("http://artifactory.jfrog.org/xsd/1.7.1",
            "http://www.jfrog.org/xsd/artifactory-v1_7_1.xsd",
            ArtifactoryVersion.v460,
            ArtifactoryVersion.v460, new BlockMismatchingMimeTypesConverter()),
    v172("http://artifactory.jfrog.org/xsd/1.7.2",
            "http://www.jfrog.org/xsd/artifactory-v1_7_2.xsd",
            ArtifactoryVersion.v461,
            ArtifactoryVersion.v470),
    v173("http://artifactory.jfrog.org/xsd/1.7.3",
                 "http://www.jfrog.org/xsd/artifactory-v1_7_3.xsd",
            ArtifactoryVersion.v471,
            ArtifactoryVersion.v478),
    v174("http://artifactory.jfrog.org/xsd/1.7.4",
            "http://www.jfrog.org/xsd/artifactory-v1_7_4.xsd",
            ArtifactoryVersion.v480,
            ArtifactoryVersion.v484, new DockerForceAuthRemovalConverter()),
    v175("http://artifactory.jfrog.org/xsd/1.7.5",
            "http://www.jfrog.org/xsd/artifactory-v1_7_5.xsd",
            ArtifactoryVersion.v490,
            ArtifactoryVersion.v493),
    v176("http://artifactory.jfrog.org/xsd/1.7.6",
            "http://www.jfrog.org/xsd/artifactory-v1_7_6.xsd",
            ArtifactoryVersion.v4100,
            ArtifactoryVersion.v4100,new LdapPoisoningProtectionConverter()),
    v177("http://artifactory.jfrog.org/xsd/1.7.7",
            "http://www.jfrog.org/xsd/artifactory-v1_7_7.xsd",
            ArtifactoryVersion.v4110,
            ArtifactoryVersion.v4110, new SigningKeysConverter()),
    v178("http://artifactory.jfrog.org/xsd/1.7.8",
            "http://www.jfrog.org/xsd/artifactory-v1_7_8.xsd",
            ArtifactoryVersion.v4111,
            ArtifactoryVersion.v4112),
    v179("http://artifactory.jfrog.org/xsd/1.7.9",
            "http://www.jfrog.org/xsd/artifactory-v1_7_9.xsd",
            ArtifactoryVersion.v4120,
            ArtifactoryVersion.v41201, new XrayRepoConfigConverter()),
    v180("http://artifactory.jfrog.org/xsd/1.8.0",
            "http://www.jfrog.org/xsd/artifactory-v1_8_0.xsd",
            ArtifactoryVersion.v4121,
            ArtifactoryVersion.v4133, new ComposerDefaultLayoutConverter()),
    v181("http://artifactory.jfrog.org/xsd/1.8.1",
            "http://www.jfrog.org/xsd/artifactory-v1_8_1.xsd",
            ArtifactoryVersion.v4140,
            ArtifactoryVersion.v4143, new ConanDefaultLayoutConverter()),
    v182("http://artifactory.jfrog.org/xsd/1.8.2",
            "http://www.jfrog.org/xsd/artifactory-v1_8_2.xsd",
            ArtifactoryVersion.v4150,
            ArtifactoryVersion.v4161, new ExternalProvidersRemovalConverter()),
    v200("http://artifactory.jfrog.org/xsd/2.0.0",
            "http://www.jfrog.org/xsd/artifactory-v2_0_0.xsd",
            ArtifactoryVersion.v500beta1,
            ArtifactoryVersion.v500rc5, new PuppetDefaultLayoutConverter()),
    v201("http://artifactory.jfrog.org/xsd/2.0.1",
            "http://www.jfrog.org/xsd/artifactory-v2_0_1.xsd",
            ArtifactoryVersion.v500rc6,
            ArtifactoryVersion.v520),
    v202("http://artifactory.jfrog.org/xsd/2.0.2",
            "http://www.jfrog.org/xsd/artifactory-v2_0_2.xsd",
            ArtifactoryVersion.v521m003,
            ArtifactoryVersion.v521),
    v203("http://artifactory.jfrog.org/xsd/2.0.3",
            "http://www.jfrog.org/xsd/artifactory-v2_0_3.xsd",
            ArtifactoryVersion.v522m001,
            ArtifactoryVersion.v534, new AccessTokenSettingsRenameToAccessClientSettingsConverter()),
    v204("http://artifactory.jfrog.org/xsd/2.0.4",
            "http://www.jfrog.org/xsd/artifactory-v2_0_4.xsd",
            ArtifactoryVersion.v540m001,
            ArtifactoryVersion.v540m001,new YumEnableFilelistsIndexingForExistingLocalReposConverter()),
    v205("http://artifactory.jfrog.org/xsd/2.0.5",
            "http://www.jfrog.org/xsd/artifactory-v2_0_5.xsd",
            ArtifactoryVersion.v540,
            ArtifactoryVersion.v548, new EventBasedRemoteReplicationConverter()),
    v206("http://artifactory.jfrog.org/xsd/2.0.6",
            "http://www.jfrog.org/xsd/artifactory-v2_0_6.xsd",
            ArtifactoryVersion.v550m001,
            ArtifactoryVersion.v551p001),
    v207("http://artifactory.jfrog.org/xsd/2.0.7",
            "http://www.jfrog.org/xsd/artifactory-v2_0_7.xsd",
            ArtifactoryVersion.v552m001,
            ArtifactoryVersion.v552, new RemoveAccessAdminCredentialsConverter(), new FolderDownloadForAnonymousConfigConverter()),
    v208("http://artifactory.jfrog.org/xsd/2.0.8",
            "http://www.jfrog.org/xsd/artifactory-v2_0_8.xsd",
            ArtifactoryVersion.v560m001,
            ArtifactoryVersion.v566, new AddReplicationKey()),
    v209("http://artifactory.jfrog.org/xsd/2.0.9",
            "http://www.jfrog.org/xsd/artifactory-v2_0_9.xsd",
            ArtifactoryVersion.v570m001,
            ArtifactoryVersion.v572),
    v210("http://artifactory.jfrog.org/xsd/2.1.0",
            "http://www.jfrog.org/xsd/artifactory-v2_1_0.xsd",
            ArtifactoryVersion.v580,
            ArtifactoryVersion.v592, new XrayMinBlockedSeverityAndBlockUnscannedConverter()),
    v211("http://artifactory.jfrog.org/xsd/2.1.1",
            "http://www.jfrog.org/xsd/artifactory-v2_1_1.xsd",
            ArtifactoryVersion.v5100m009,
            ArtifactoryVersion.v5102, new PyPIRegistryUrlConverter()),
    v212("http://artifactory.jfrog.org/xsd/2.1.2",
            "http://www.jfrog.org/xsd/artifactory-v2_1_2.xsd",
            ArtifactoryVersion.v5103,
            ArtifactoryVersion.v5103, new AllowCrowdUsersToAccessProfilePageConverter(), new GoDefaultLayoutConverter()),
    v213("http://artifactory.jfrog.org/xsd/2.1.3",
            "http://www.jfrog.org/xsd/artifactory-v2_1_3.xsd",
            ArtifactoryVersion.v5110m001,
            ArtifactoryVersion.getCurrent())
    ;

    private final String xsdUri;
    private final String xsdLocation;
    private final VersionComparator comparator;
    private final XmlConverter[] converters;

    /**
     * @param from       The artifactory version this config version was first used
     * @param until      The artifactory version this config was last used in (inclusive)
     * @param converters A list of converters to use to move from <b>this</b> config version to the <b>next</b> config
     *                   version
     */
    ArtifactoryConfigVersion(String xsdUri, String xsdLocation, ArtifactoryVersion from, ArtifactoryVersion until,
            XmlConverter... converters) {
        this.comparator = new VersionComparator(from, until);
        this.xsdUri = xsdUri;
        this.xsdLocation = xsdLocation;
        this.converters = converters;
    }

    public static ArtifactoryConfigVersion getCurrent() {
        ArtifactoryConfigVersion[] versions = ArtifactoryConfigVersion.values();
        return versions[versions.length - 1];
    }

    public boolean isCurrent() {
        return comparator.isCurrent();
    }

    public String convert(String in) {
        // First create the list of converters to apply
        List<XmlConverter> converters = new ArrayList<>();

        // First thing to do is to change the namespace and schema location
        converters.add(new NamespaceConverter());

        // All converters of versions above me needs to be executed in sequence
        ArtifactoryConfigVersion[] versions = ArtifactoryConfigVersion.values();
        for (ArtifactoryConfigVersion version : versions) {
            if (version.ordinal() >= ordinal() && version.getConverters() != null) {
                converters.addAll(Arrays.asList(version.getConverters()));
            }
        }
        return XmlConverterUtils.convert(converters, in);
    }

    public String getXsdUri() {
        return xsdUri;
    }

    public String getXsdLocation() {
        return xsdLocation;
    }

    public XmlConverter[] getConverters() {
        return converters;
    }

    @Override
    public VersionComparator getComparator() {
        return comparator;
    }

    public static ArtifactoryConfigVersion getConfigVersion(String configXml) {
        // Find correct version by schema URI
        ArtifactoryConfigVersion[] configVersions = values();
        for (ArtifactoryConfigVersion configVersion : configVersions) {
            if (configXml.contains("\"" + configVersion.getXsdUri() + "\"")) {
                return configVersion;
            }
        }
        return null;
    }
}
