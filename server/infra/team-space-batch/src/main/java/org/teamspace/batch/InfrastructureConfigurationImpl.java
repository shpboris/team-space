package org.teamspace.batch;

/**
 * Created by shpilb on 08/09/2017.
 */

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;


@Configuration
@EnableBatchProcessing
@EnableTransactionManagement
public class InfrastructureConfigurationImpl implements InfrastructureConfiguration {

    private static final int CORE_POOL_SIZE = 20;
    private static final int MAX_POOL_SIZE = 80;

    @Value("${batch-database.driver}")
    private String driver;

    @Value("${batch-database.url}")
    private String url;

    @Value("${batch-database.user}")
    private String user;

    @Value("${batch-database.password}")
    private String password;

    @Value("${batch-database.maxActive}")
    private Integer maxActive;

    @Value("${batch-database.maxIdle}")
    private Integer maxIdle;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobRegistry jobRegistry;

    @Autowired
    private JobExplorer jobExplorer;

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
        dataSource.setMaxActive(maxActive);
        dataSource.setMaxIdle(maxIdle);
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public JobOperator jobOperator() {
        SimpleJobOperator jobOperator = new CustomJobOperator();
        jobOperator.setJobExplorer(jobExplorer);
        jobOperator.setJobLauncher(jobLauncher());
        jobOperator.setJobRegistry(jobRegistry);
        jobOperator.setJobRepository(jobRepository);
        return jobOperator;
    }

    @Bean
    public JobLauncher jobLauncher() {
        SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
        simpleJobLauncher.setJobRepository(jobRepository);
        simpleJobLauncher.setTaskExecutor(taskExecutor());
        return simpleJobLauncher;
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(CORE_POOL_SIZE);
        threadPoolTaskExecutor.setMaxPoolSize(MAX_POOL_SIZE);
        return threadPoolTaskExecutor;
    }

   @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }

}