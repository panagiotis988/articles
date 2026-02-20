package com.wikipedia.articles.config;

import java.io.IOException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class DocsWebConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/docs").setViewName("redirect:/docs/");
        registry.addViewController("/docs/").setViewName("forward:/docs/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/docs/**")
            .addResourceLocations("classpath:/static/docs/")
            .resourceChain(true)
            .addResolver(new PathResourceResolver() {
                @Override
                protected Resource getResource(String resourcePath, Resource location) throws IOException {
                    Resource resource = super.getResource(resourcePath, location);
                    if (resource != null) {
                        return resource;
                    }

                    String path = resourcePath;
                    if (path.isEmpty()) {
                        path = "index.html";
                    } else if (path.endsWith("/")) {
                        path = path + "index.html";
                    } else if (!path.contains(".")) {
                        path = path + "/index.html";
                    } else {
                        return null;
                    }

                    return super.getResource(path, location);
                }
            });
    }
}
