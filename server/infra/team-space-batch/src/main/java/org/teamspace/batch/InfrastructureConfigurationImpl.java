package org.teamspace.batch;

/**
 * Created by shpilb on 08/09/2017.
 */

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.*;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class InfrastructureConfigurationImpl implements InfrastructureConfiguration {

    @Value("${batch-database.driver}")
    private String driver;

    @Value("${batch-database.url}")
    private String url;

    @Value("${batch-database.user}")
    private String user;

    @Value("${batch-database.password}")
    private String password;

    @Autowired
    private ResourceLoader resourceLoader;

    @Bean
    BatchConfigurer configurer(){
        return new DefaultBatchConfigurer(dataSource());
    }


    @PostConstruct
    protected void initialize() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        Resource initSchema = resourceLoader.getResource("classpath:/org/springframework/batch/core/schema-h2.sql");
        populator.addScript(initSchema);
        populator.setContinueOnError(true);
        DatabasePopulatorUtils.execute(populator , dataSource());
    }

    @Bean
    public DataSource dataSource(){
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        return dataSource;
    }

}