package org.artifactory.storage.db.bundle.dao;

import com.google.common.collect.Lists;
import org.artifactory.api.rest.distribution.bundle.utils.BundleNameVersionResolver;
import org.artifactory.common.ConstantValues;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.bundle.model.ArtifactsBundle;
import org.artifactory.storage.db.bundle.model.BundleNode;
import org.artifactory.storage.db.bundle.model.BundleTransactionStatus;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.common.ClockUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
public class ArtifactBundlesDao extends BaseDao {
    private static final String EXPIRED_BUNDLES_QUERY = "SELECT id FROM artifact_bundles where status = '" +
            BundleTransactionStatus.INPROGRESS.name() + "' and date_created < ?";

    private static final String UPDATE_BUNDLES_CREATION_DATE_QUERY = "UPDATE artifact_bundles SET date_created = ? where " +
            "id = ? and status = '" + BundleTransactionStatus.INPROGRESS.name() + "'";

    private DbService dbService;

    @Autowired
    public ArtifactBundlesDao(JdbcHelper jdbcHelper, DbService dbService) {
        super(jdbcHelper);
        this.dbService = dbService;
    }

    public int create(ArtifactsBundle bundle) throws SQLException {
        bundle.setId(Optional.ofNullable(nullIfZero(bundle.getId()))
                .orElseGet(dbService::nextId));
        bundle.validate();
        return jdbcHelper.executeUpdate(
                "INSERT INTO artifact_bundles (id, name, version, status, date_created, signature) VALUES (?, ?, ?, ?, ?, ?)",
                bundle.getId(), bundle.getName(), bundle.getVersion(),
                bundle.getStatus().name(), bundle.getDateCreated().getMillis(), bundle.getSignature());
    }

    public boolean deleteBundleNodes(List<Long> ids) throws SQLException {
        int deleted = jdbcHelper.executeUpdate("DELETE FROM bundle_files WHERE id IN (#)", ids);
        return deleted == ids.size();
    }

    public boolean deleteArtifactsBundle(long id) throws SQLException {
        int deleted = jdbcHelper.executeUpdate("DELETE FROM artifact_bundles WHERE id = ?", id);
        return deleted > 0;
    }

    public boolean deleteAllArtifactsBundles() throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM artifact_bundles") > 0;
    }

    public boolean deleteAllBundleNodes() throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM bundle_files") > 0;
    }

    public int create(BundleNode bundleNode) throws SQLException {
        bundleNode.setId(Optional.ofNullable(nullIfZero(bundleNode.getId())).orElseGet(dbService::nextId));
        bundleNode.validate();
        return jdbcHelper.executeUpdate(
                "INSERT INTO bundle_files (id, node_id, bundle_id, repo_path) VALUES (?, ?, ?, ?)",
                bundleNode.getId(), bundleNode.getNodeId(),
                bundleNode.getBundleId(), bundleNode.getRepoPath());
    }

    public int closeTransaction(String transactionPath) throws SQLException {
        BundleNameVersionResolver bundleNameVersion = new BundleNameVersionResolver(transactionPath);
        return closeTransaction(bundleNameVersion.getBundleName(), bundleNameVersion.getBundleVersion());
    }

    private int closeTransaction(String bundleName, String bundleVersion) throws SQLException {
        return jdbcHelper.executeUpdate("UPDATE artifact_bundles SET status = ? " +
                "WHERE name = ? AND version = ?", BundleTransactionStatus.COMPLETE.name(), bundleName, bundleVersion);
    }

    public ArtifactsBundle getArtifactsBundle(String transactionPath) throws SQLException {
        BundleNameVersionResolver bundleNameVersion = new BundleNameVersionResolver(transactionPath);
        return getArtifactsBundle(bundleNameVersion.getBundleName(), bundleNameVersion.getBundleVersion());
    }

    public ArtifactsBundle getArtifactsBundle(String bundleName, String bundleVersion)
            throws SQLException {
        ArtifactsBundle artifactsBundle = null;
        String query = "SELECT * FROM artifact_bundles WHERE name = ? AND version = ?";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query, bundleName, bundleVersion)) {
            if (resultSet.next()) {
                artifactsBundle = artifactsBundleFromResultSet(resultSet);
            }
        }
        return artifactsBundle;
    }

    public ArtifactsBundle getArtifactsBundle(long id) throws SQLException {
        try (ResultSet resultSet = jdbcHelper.executeSelect("SELECT * FROM artifact_bundles WHERE id = ?", id)) {
            if (resultSet.next()) {
                return artifactsBundleFromResultSet(resultSet);
            }
        }
        return null;
    }

    public String getArtifactsBundleStatus(String bundleName, String bundleVersion)
            throws SQLException {
        String query = "SELECT status FROM artifact_bundles WHERE name = ? AND version = ?";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query, bundleName, bundleVersion)) {
            if (resultSet.next()) {
               return resultSet.getString(1);
            }
        }
        return null;
    }

    public List<ArtifactsBundle> getArtifactsBundles(String bundleName)
            throws SQLException {
        List<ArtifactsBundle> artifactsBundles = Lists.newLinkedList();
        String query = "SELECT * FROM artifact_bundles WHERE name = ?";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query, bundleName)) {
            while (resultSet.next()) {
                artifactsBundles.add(artifactsBundleFromResultSet(resultSet));
            }
        }
        return artifactsBundles;
    }

    public List<ArtifactsBundle> getAllArtifactsBundles()
            throws SQLException {
        List<ArtifactsBundle> artifactsBundles = Lists.newLinkedList();
        String query = "SELECT * FROM artifact_bundles";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query)) {
            while (resultSet.next()) {
                artifactsBundles.add(artifactsBundleFromResultSet(resultSet));
            }
        }
        return artifactsBundles;
    }

    public List<BundleNode> getAllBundleNodes()
            throws SQLException {
        List<BundleNode> bundleNodes = Lists.newLinkedList();
        String query = "SELECT * FROM bundle_files";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query)) {
            while (resultSet.next()) {
                bundleNodes.add(bundleNodeFromResultSet(resultSet));
            }
        }
        return bundleNodes;
    }

    public List<BundleNode> getBundleNodes(long bundleId)
            throws SQLException {
        List<BundleNode> bundleNodes = Lists.newLinkedList();
        String query = "SELECT * FROM bundle_files WHERE bundle_id = ?";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query, bundleId)) {
            while (resultSet.next()) {
                bundleNodes.add(bundleNodeFromResultSet(resultSet));
            }
        }
        return bundleNodes;
    }

    public String getRepoPathFromBundleNode(long nodeId) throws SQLException {
        String query = "SELECT repo_path FROM bundle_files WHERE node_id = ?";
        String res = null;
        try (ResultSet resultSet = jdbcHelper.executeSelect(query, nodeId)) {
            if (resultSet.next()) {
                res = resultSet.getString(1);
            }
        }
        return res;
    }

    @Nonnull
    public List<Long> getExpiredBundlesIds() throws SQLException {
        List<Long> ids = new LinkedList<>();
        long expiryDate = ClockUtils.epochMillis() - TimeUnit.HOURS.toMillis(ConstantValues.releaseBundleCleanupMaxAgeHours.getLong());
        try (ResultSet resultSet = jdbcHelper.executeSelect(EXPIRED_BUNDLES_QUERY, expiryDate)) {
            while (resultSet.next()) {
                ids.add(resultSet.getLong("id"));
            }
        }
        return ids;
    }

    public boolean isNodeRelatedToBundle(long nodeId) throws SQLException {
        String query = "SELECT repo_path FROM bundle_files WHERE node_id = ?";
        try (ResultSet resultSet = jdbcHelper.executeSelect(query, nodeId)) {
            if (resultSet.next()) {
                return true;
            }
        }
        return false;
    }

    public int updateArtifactsBundleCreationDate(Long id) throws SQLException {
        return jdbcHelper.executeUpdate(UPDATE_BUNDLES_CREATION_DATE_QUERY, ClockUtils.epochMillis(), id);
    }

    private ArtifactsBundle artifactsBundleFromResultSet(ResultSet resultSet) throws SQLException {
        ArtifactsBundle artifactsBundle = new ArtifactsBundle();
        artifactsBundle.setId(resultSet.getLong(1));
        artifactsBundle.setName(resultSet.getString(2));
        artifactsBundle.setVersion(resultSet.getString(3));
        artifactsBundle.setStatus(BundleTransactionStatus.valueOf(resultSet.getString(4)));
        artifactsBundle.setDateCreated(new DateTime(resultSet.getLong(5)));
        artifactsBundle.setSignature(resultSet.getString(6));
        return artifactsBundle;
    }

    private BundleNode bundleNodeFromResultSet(ResultSet resultSet) throws SQLException {
        BundleNode bundleNode = new BundleNode();
        bundleNode.setId(resultSet.getLong(1));
        bundleNode.setNodeId(resultSet.getLong(2));
        bundleNode.setBundleId(resultSet.getLong(3));
        bundleNode.setRepoPath(resultSet.getString(4));
        return bundleNode;
    }
}
