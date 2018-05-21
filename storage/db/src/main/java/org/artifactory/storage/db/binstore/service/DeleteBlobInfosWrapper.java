package org.artifactory.storage.db.binstore.service;

import org.artifactory.api.blob.infos.BlobInfosDBService;
import org.artifactory.api.repo.Async;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Inbar Tal
 */
@Component
public class DeleteBlobInfosWrapper {

    @Autowired
    private BlobInfosDBService blobInfosDBService;

    @Async
    public void deleteBlobInfosAsync(List<String> checksums) {
        blobInfosDBService.deleteBlobInfos(checksums);
    }
}
