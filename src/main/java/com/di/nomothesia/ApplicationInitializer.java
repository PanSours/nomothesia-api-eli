package com.di.nomothesia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Created by psour on 14/11/2016.
 *
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class ApplicationInitializer extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ApplicationInitializer.class);
    }

    /**
     * The main method of trace-portal, that loads the proper configuration.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ApplicationInitializer.class, args).close();
    }
}
