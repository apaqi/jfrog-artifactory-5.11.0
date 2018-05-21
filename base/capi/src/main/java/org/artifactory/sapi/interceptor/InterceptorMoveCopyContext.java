package org.artifactory.sapi.interceptor;

import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.fs.FileInfo;

import javax.annotation.Nullable;

/**
 * A context to pass when performing item move or copy.
 *
 * @author Yuval Reches
 */
public class InterceptorMoveCopyContext {

    // Checksum of the target *before* the copy/move event
    private final ChecksumsInfo targetOriginalChecksumsInfo;

    public InterceptorMoveCopyContext() {
        targetOriginalChecksumsInfo = new ChecksumsInfo();
    }

    public InterceptorMoveCopyContext(@Nullable FileInfo targetFileInfo) {
        targetOriginalChecksumsInfo = targetFileInfo == null ? new ChecksumsInfo() :
                new ChecksumsInfo(targetFileInfo.getChecksumsInfo());
    }

    public ChecksumsInfo getTargetOriginalChecksumsInfo() {
        return targetOriginalChecksumsInfo;
    }
}
