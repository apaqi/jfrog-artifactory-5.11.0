package org.artifactory.aql;

import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;

/**
 * Item row that's fully mappable to a {@link FileInfo}
 *
 * @author Dan Feldman
 */
@Data
public class FileInfoWithStatisticsItemRow extends FileInfoItemRow {


    private long statDownloaded;


    @Override
    public void put(DomainSensitiveField field, Object value) {
        super.put(field, value);
        if (field.getField() == AqlPhysicalFieldEnum.statDownloads) {
            statDownloaded = ((Integer) value).longValue();
        }
    }

    @Override
    public Object get(DomainSensitiveField field) {
        return null;
    }

    public RepoPath getRepoPath() {
        if (StringUtils.equals(path, ".")) {
            return RepoPathFactory.create(repo, name);
        } else {
            return RepoPathFactory.create(repo, path + "/" + name);
        }
    }
}
