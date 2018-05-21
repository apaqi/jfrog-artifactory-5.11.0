package org.artifactory.rest.filter;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import org.artifactory.rest.common.BlockOnConversion;

import java.util.Collections;
import java.util.List;

public class ConversionRestFilterFactory implements ResourceFilterFactory {

    private final List<ResourceFilter> filters;

    public ConversionRestFilterFactory() {

        ResourceFilter filter = internalCreateResourceFilter();

        filters = Collections.singletonList(filter);
    }

    private ResourceFilter internalCreateResourceFilter() {
        return new ResourceFilter() {

            @Override
            public ContainerRequestFilter getRequestFilter() {
                return new ConversionFilterWrapper();
            }

            @Override
            public ContainerResponseFilter getResponseFilter() {
                return null;
            }
        };
    }

    @Override
    public List<ResourceFilter> create(AbstractMethod abstractMethod) {
        boolean blockOnUpdate = abstractMethod.getAnnotation(BlockOnConversion.class) != null;
        if (!blockOnUpdate) {
            return null;
        }
        return filters;
    }
}
