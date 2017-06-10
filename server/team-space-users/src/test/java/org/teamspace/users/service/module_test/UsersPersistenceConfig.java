package org.teamspace.users.service.module_test;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.*;
import org.teamspace.persistence.common.Dao;
import persistence_test.config.PersistenceTestConfig;

/**
 * Created by shpilb on 10/06/2017.
 */
@Configuration
@Import(PersistenceTestConfig.class)
@ComponentScan({ "org.teamspace.users.service"})
@MapperScan(basePackages = {"org.teamspace.users.dao"}, annotationClass = Dao.class)
public class UsersPersistenceConfig {
}
