package configuration;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

    registry.addResourceHandler("/**/*")
        .addResourceLocations("classpath:/public/")
        .resourceChain(true)
        .addResolver(new PathResourceResolver() {
            @Override
            protected Resource getResource(String resourcePath, Resource resource) throws IOException {
                Resource requestedResource = resource.createRelative(resourcePath);
                return requestedResource.exists() && requestedResource.isReadable() 
                    ? requestedResource
                    : new ClassPathResource("/public/index.html")
                ;
            }
        });
    }
}