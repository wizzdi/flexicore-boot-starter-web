package com.wizzdi.flexicore.boot.rest.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;

@Configuration
public class ExternalStaticFilesConfig {
    // I assign filePath and pathPatterns using @Value annotation
    @Value("${flexicore.externalStatic:/home/flexicore/ui/}")
    private String externalStatic;
    @Value("${flexicore.externalStaticMapping:/**}")
    private String externalStaticMapping;

    @Value("${flexicore.internalStaticLocation:classpath:/static/}")
    private String internalStaticLocation;
    @Value("${flexicore.internalStaticMapping:/FlexiCore/**}")
    private String internalStaticMapping;

    @Bean
    public WebMvcRegistrations webMvcRegistrations(){
        return new WebMvcRegistrations() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                return new CustomRequestMappingHandlerMapping();
            }
        };
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurerAdapter() {
        return new WebMvcConfigurer() {


            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler(internalStaticMapping)
                        .addResourceLocations(internalStaticLocation);
                registry.addResourceHandler(externalStaticMapping)
                        .addResourceLocations("file:" + externalStatic);
            }

            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                registry.addViewController("/FlexiCore").setViewName("redirect:/FlexiCore/"); //delete these two lines if looping with directory as below
                registry.addViewController("/FlexiCore/").setViewName("forward:/FlexiCore/index.html"); //delete these two lines if looping with directory as below
                registry.addViewController("/notFound").setViewName("forward:/index.html");

                String[] directories = listDirectories(externalStatic);
                if(directories!=null){
                    for (String subDir : directories){
                        registry.addViewController("/"+subDir).setViewName("redirect:/" + subDir + "/");
                        registry.addViewController("/"+subDir+"/").setViewName("forward:/" + subDir + "/index.html");
                    }
                }

            }
        };
    }
    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> containerCustomizer() {
        return container -> {
            container.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND,
                    "/notFound"));
        };
    }

    private String[] listDirectories(String root){
        File file = new File(root);
        return file.list((current, name) -> new File(current, name).isDirectory());
    }
}