package org.teamspace.batch;

import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * Created by shpilb on 08/09/2017.
 */
public interface InfrastructureConfiguration {
    @Bean
    public abstract DataSource dataSource();
}
