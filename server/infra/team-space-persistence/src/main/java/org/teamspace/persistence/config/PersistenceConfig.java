package org.teamspace.persistence.config;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.teamspace.persistence.common.Dao;

import javax.sql.DataSource;

import static org.teamspace.persistence.common.CommonConstants.BASE_PACKAGE;

/**
 * Created by shpilb on 11/04/2017.
 */
@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = {BASE_PACKAGE}, annotationClass = Dao.class)
public class PersistenceConfig {

    @Value("${database.driver}")
    private String driver;

    @Value("${database.url}")
    private String url;

    @Value("${database.user}")
    private String user;

    @Value("${database.password}")
    private String password;

    @Value("${database.maxActive}")
    private Integer maxActive;

    @Value("${database.maxIdle}")
    private Integer maxIdle;

    @Bean
    public DataSource getBasicDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        dataSource.setMaxActive(maxActive);
        dataSource.setMaxIdle(maxIdle);
        return dataSource;
    }
    @Bean
    public DataSourceTransactionManager txManager() {
        return new DataSourceTransactionManager(getBasicDataSource());
    }
    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(getBasicDataSource());
        return sessionFactory.getObject();
    }
}
