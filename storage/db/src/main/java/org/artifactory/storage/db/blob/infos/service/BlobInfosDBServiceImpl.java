package org.artifactory.storage.db.blob.infos.service;

import org.artifactory.api.blob.infos.BlobInfosDBService;
import org.artifactory.api.jackson.JacksonReader;
import org.artifactory.api.jackson.JacksonWriter;
import org.artifactory.api.rest.blob.BlobInfo;
import org.artifactory.exception.SQLIntegrityException;
import org.artifactory.storage.db.blob.infos.dao.BlobInfosDao;
import org.artifactory.storage.db.blob.infos.model.DbBlobInfo;
import org.jfrog.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;

/**
 * @author Inbar Tal
 */

@Service
public class BlobInfosDBServiceImpl implements BlobInfosDBService {
    private static final Logger log = LoggerFactory.getLogger(BlobInfosDBServiceImpl.class);

    @Autowired
    private BlobInfosDao dao;

    public boolean putBlobInfo(String checksum, BlobInfo blobInfo) throws StorageException {
        try {
            log.debug("Create new blob info with checksum: {}", checksum);
            DbBlobInfo dbBlobInfo = new DbBlobInfo(checksum, blobInfoToJson(blobInfo));
            return dao.create(dbBlobInfo) > 0;
        }  catch (SQLIntegrityConstraintViolationException e) {
            throw new SQLIntegrityException("Failed to create blobInfo, blobInfo is already exists", e);
        }  catch (Exception e) {
            throw new StorageException("Error occurred while inserting blobInfo with checksum: " + checksum, e);
        }
    }

    public int deleteBlobInfo(String checksum) throws StorageException {
        try {
            log.debug("Delete blob info with checksum: {}", checksum);
            return dao.delete(checksum);
        } catch (SQLException e) {
            throw new StorageException("Error occurred while deleting blobInfo with checksum: " + checksum, e);
        }
    }

    public int deleteBlobInfos(List<String> checksums) throws StorageException {
        try {
            int numDeleted = dao.deleteBulk(checksums);
            log.debug("{} blob infos were deleted successfully", numDeleted);
            return numDeleted;
        } catch (SQLException e) {
            throw new StorageException("Error occurred while deleting blobInfos from cache", e);
        }
    }

    public BlobInfo getBlobInfo(String checksum) throws StorageException {
        try {
            Optional<DbBlobInfo> dbBlobInfo = dao.find(checksum);
            return (dbBlobInfo.isPresent()) ? jsonToBlobInfo(dbBlobInfo.get().getBlobInfo()) : null;
        } catch (Exception e) {
            throw new StorageException("Error occurred while searching for blobInfo with checksum: " + checksum, e);
        }
    }

    private BlobInfo jsonToBlobInfo(String blobinfoJson) throws IOException {
        return JacksonReader.bytesAsClass(blobinfoJson.getBytes(), BlobInfo.class);
    }

    private String blobInfoToJson(BlobInfo blobInfo) throws IOException {
        return JacksonWriter.serialize(blobInfo);
    }
}
