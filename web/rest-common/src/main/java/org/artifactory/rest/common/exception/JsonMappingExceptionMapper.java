package org.artifactory.rest.common.exception;

import com.sun.jersey.api.Responses;
import org.artifactory.rest.ErrorResponse;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Component
@Provider
public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {
    private static final Logger log = LoggerFactory.getLogger(JsonMappingExceptionMapper.class);

    @Override
    public Response toResponse(JsonMappingException e) {
        String msg = "Error parsing json body";
        log.error(msg, e);
        ErrorResponse errorResponse = new ErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(),
                msg);
        return Responses.clientError().type(MediaType.APPLICATION_JSON).entity(errorResponse).build();
    }
}
