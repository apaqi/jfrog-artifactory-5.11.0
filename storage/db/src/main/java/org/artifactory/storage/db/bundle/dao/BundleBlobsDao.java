package org.artifactory.storage.db.bundle.dao;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.bundle.model.BundleTransactionStatus;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.storage.util.DbUtils;
import org.jfrog.storage.wrapper.BlobWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Repository
public class BundleBlobsDao extends BaseDao {

    private DbService dbService;

    @Autowired
    public BundleBlobsDao(JdbcHelper jdbcHelper, DbService dbService) {
        super(jdbcHelper);
        this.dbService = dbService;
    }

    public int create(String data, long bundleId) throws SQLException {
        BlobWrapper blobWrapper = new BlobWrapper(data);
        long id = dbService.nextId();
        return jdbcHelper.executeUpdate(
                "INSERT INTO bundle_blobs (id, data, bundle_id) VALUES (?, ?, ?)", id, blobWrapper, bundleId);
    }

    public int create(InputStream data, long bundleId) throws SQLException {
        BlobWrapper blobWrapper = new BlobWrapper(data);
        long id = dbService.nextId();
        return jdbcHelper.executeUpdate(
                "INSERT INTO bundle_blobs (id, data, bundle_id) VALUES (?, ?, ?)", id, blobWrapper, bundleId);
    }

    public String getBundleBlobData(String bundleName, String bundleVersion) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect(
                    "SELECT data FROM bundle_blobs WHERE bundle_blobs.bundle_id = " +
                            "(SELECT id FROM artifact_bundles WHERE name = ? AND version = ?)",
                    bundleName, bundleVersion);
            if (resultSet.next()) {
                InputStream binaryStream = resultSet.getBinaryStream(1);
                return IOUtils.toString(binaryStream, Charsets.UTF_8.name());
            }
            return null;
        } catch (IOException e) {
            throw new SQLException(
                    "Failed to read bundle blob for bundle  '" + bundleName + "/" + bundleVersion + "' due to: " +
                            e.getMessage(), e);
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public String getBundleBlobData(long bundleId) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT data FROM bundle_blobs WHERE bundle_id = ?", bundleId);
            if (resultSet.next()) {
                InputStream binaryStream = resultSet.getBinaryStream(1);
                return IOUtils.toString(binaryStream, Charsets.UTF_8.name());
            }
            return null;
        } catch (IOException e) {
            throw new SQLException(
                    "Failed to read bundle blob for bundle with id '" + bundleId + "' due to: " + e.getMessage(), e);
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public String getBundleBlobDataById(long id) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT data FROM bundle_blobs WHERE id = ?", id);
            if (resultSet.next()) {
                try(InputStream binaryStream = resultSet.getBinaryStream(1)) {
                    return IOUtils.toString(binaryStream, Charsets.UTF_8.name());
                }
            }
            return null;
        } catch (IOException e) {
            throw new SQLException(
                    "Failed to read bundle blob with id '" + id + "' due to: " + e.getMessage(), e);
        } finally {
            DbUtils.close(resultSet);
        }
    }

    /**
     * return a map containing blob id (key) to <bundle name>_<bundle version> (value)
     */
    public  Map<Long, String> getAllBlobIdsForExport() throws SQLException {
        ResultSet resultSet = null;
        Map<Long, String> blobIds = Maps.newHashMap();
        try {
            resultSet = jdbcHelper.executeSelect(
                    "SELECT bundle_blobs.id, artifact_bundles.name, artifact_bundles.version FROM bundle_blobs, artifact_bundles" +
                            " WHERE artifact_bundles.id = bundle_blobs.bundle_id AND artifact_bundles.status = ?",
                    BundleTransactionStatus.COMPLETE.name());
            while (resultSet.next()) {
                long blobId = resultSet.getLong(1);
                String bundleNameVersion = resultSet.getString(2) + "_" + resultSet.getString(3);
                blobIds.put(blobId, bundleNameVersion);
            }
        } finally {
            DbUtils.close(resultSet);
        }
        return blobIds;
    }

    public boolean deleteBlob(long bundleId) throws SQLException {
        int deleted = jdbcHelper.executeUpdate("DELETE FROM bundle_blobs WHERE bundle_id = ?", bundleId);
        return deleted > 0;
    }

    public boolean deleteAllBlobs() throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM bundle_blobs") > 0;
    }
}
