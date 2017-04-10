package org.teamspace.client.config;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Created by shpilb on 10/04/2017.
 */
@Configuration
@PropertySource("classpath:server.properties")
public class ServerLocationConfig {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
    }
}
