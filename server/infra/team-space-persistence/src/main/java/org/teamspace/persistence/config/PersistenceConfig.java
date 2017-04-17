package org.teamspace.persistence.config;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.teamspace.persistence.common.Dao;

import javax.sql.DataSource;

import static org.teamspace.persistence.common.CommonConstants.BASE_PACKAGE;

/**
 * Created by shpilb on 11/04/2017.
 */
@Configuration
@MapperScan(basePackages = {BASE_PACKAGE}, annotationClass = Dao.class)
public class PersistenceConfig {

    public static String DRIVER;
    public static String URL;
    public static String USER;
    public static String PASSWORD;
    public static final int MAXIMUM_ACTIVE_CONNECTIONS = 10;
    public static final int MAXIMUM_IDLE_CONNECTIONS = 5;

    @Bean
    public DataSource getDataSource() {
        PooledDataSource dataSource = new PooledDataSource();
        dataSource.setDriver(DRIVER);
        dataSource.setUrl(URL);
        dataSource.setUsername(USER);
        dataSource.setPassword(PASSWORD);
        dataSource.setPoolMaximumActiveConnections(MAXIMUM_ACTIVE_CONNECTIONS);
        dataSource.setPoolMaximumIdleConnections(MAXIMUM_IDLE_CONNECTIONS);
        return dataSource;
    }
    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(getDataSource());
    }
    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(getDataSource());
        return sessionFactory.getObject();
    }
}
