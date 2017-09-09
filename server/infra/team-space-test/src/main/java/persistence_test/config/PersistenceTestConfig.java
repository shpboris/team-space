package persistence_test.config;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Created by shpilb on 11/04/2017.
 */
@Configuration
@Import(TestsConfig.class)
@EnableTransactionManagement
@ComponentScan({"org.teamspace.persistence.common.schema"})
//@MapperScan(basePackages = {"org.teamspace"}, annotationClass = Dao.class)
public class PersistenceTestConfig {

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
    public DataSource getDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName(driver);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        dataSource.setMaxActive(maxActive);
        dataSource.setMaxIdle(maxIdle);
        return dataSource;
    }
    @Bean
    public DataSourceTransactionManager txManager() {
        return new DataSourceTransactionManager(getDataSource());
    }
    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(getDataSource());
        return sessionFactory.getObject();
    }
}
