package org.artifactory.rest.resource.xray;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.artifactory.addon.xray.XrayScanBuild;
import org.artifactory.rest.common.model.xray.XrayScanBuildModel;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;

/**
 * @author Chen Keinan
 */
class XrayResourceHelper {

    /**
     * map rest model to service model
     * @param xrayScanBuildModel - rest api model (payload)
     * @return xray service model to be send to xray
     */
    static XrayScanBuild toModel(XrayScanBuildModel xrayScanBuildModel){
        return new XrayScanBuild(xrayScanBuildModel.getBuildName(),
                xrayScanBuildModel.getBuildNumber(),
                xrayScanBuildModel.getContext());
    }

    /**
     * Stream response from xray
     * @param response - response from xray
     * @return stream response data from xray to client
     */
    static Response streamResponse(@Nonnull CloseableHttpResponse response) {
          /* stream build scanning results */
        StreamingOutput stream = out -> {
            try (InputStream inputStream = response.getEntity().getContent()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    out.flush();
                }
            } finally {
                IOUtils.closeQuietly(out);
                IOUtils.closeQuietly(response);
            }
        };
        return Response.status(HttpStatus.SC_OK).entity(stream).build();
    }
}
