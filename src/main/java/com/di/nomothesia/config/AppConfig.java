package com.di.nomothesia.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.servlet.Filter;
import java.util.Locale;

/**
 * Created by psour on 14/11/2016.
 * <p>
 * Application Configuration File - Read from Properties.
 */
@Configuration
@PropertySource (value = {"file:${CATALINA_BASE}/conf/nomothesia.properties"})
@EnableConfigurationProperties (AppConfig.ApplicationProperties.class)
//@EnableIntegrationMBeanExport(registration = RegistrationPolicy.REPLACE_EXISTING)
public class AppConfig extends WebMvcConfigurerAdapter {

    protected final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Autowired
    private ApplicationProperties applicationProperties;

    @ConfigurationProperties
    public static class ApplicationProperties {
        @Value ("${sesame.server}")
        private String sesameServer;

        @Value ("${sesame.repository.id}")
        private String sesameRepositoryID;

        @Value ("${strabon.host}")
        private String strabonHost;

        @Value ("${strabon.port}")
        private String strabonPort;

        @Value ("${strabon.app.name}")
        private String strabonAppName;

        public String getSesameServer() {
            return sesameServer;
        }

        public void setSesameServer(String sesameServer) {
            this.sesameServer = sesameServer;
        }

        public String getSesameRepositoryID() {
            return sesameRepositoryID;
        }

        public void setSesameRepositoryID(String sesameRepositoryID) {
            this.sesameRepositoryID = sesameRepositoryID;
        }

        public String getStrabonHost() {
            return strabonHost;
        }

        public void setStrabonHost(String strabonHost) {
            this.strabonHost = strabonHost;
        }

        public String getStrabonPort() {
            return strabonPort;
        }

        public void setStrabonPort(String strabonPort) {
            this.strabonPort = strabonPort;
        }

        public String getStrabonAppName() {
            return strabonAppName;
        }

        public void setStrabonAppName(String strabonAppName) {
            this.strabonAppName = strabonAppName;
        }
    }

    @Bean
    public Filter characterEncodingFilter() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);
        return characterEncodingFilter;
    }

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public InternalResourceViewResolver setupViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        return resolver;
    }

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.ENGLISH);
        return slr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("language");
        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

}
