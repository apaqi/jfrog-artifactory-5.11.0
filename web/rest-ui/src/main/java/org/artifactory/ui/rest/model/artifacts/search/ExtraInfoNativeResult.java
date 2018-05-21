package org.artifactory.ui.rest.model.artifacts.search;

import lombok.Data;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

import java.util.Date;

/**
 * @author: ortalh
 */
@Data
public class ExtraInfoNativeResult implements RestModel {

    private Integer totalDownloads;
    private int totalVersions;
    private Date lastModified;

    public void setExtraInfoByRow(AqlItem aqlBaseFullRow, boolean includeTotalDownloads) {
        if (includeTotalDownloads) {
            this.totalDownloads = totalDownloads + ((AqlBaseFullRowImpl) aqlBaseFullRow).getDownloads();
        }
        this.totalVersions = totalVersions + 1;
        Date rowLastModified = aqlBaseFullRow.getModified();
        if (lastModified == null || lastModified.before(rowLastModified)) {
            this.lastModified = rowLastModified;
        }
    }

    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
