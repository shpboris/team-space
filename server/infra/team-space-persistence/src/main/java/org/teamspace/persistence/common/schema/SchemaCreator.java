package org.teamspace.persistence.common.schema;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.teamspace.persistence.common.Dao;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by shpilb on 16/04/2017.
 */
@Component
@Slf4j
public class SchemaCreator implements ApplicationContextAware{

    private ApplicationContext applicationContext;

    public void createSchema(){
        Map<String, ?> annotatedMappers = applicationContext.getBeansWithAnnotation(Dao.class);
        for (Object mapperInstance : annotatedMappers.values()) {
            try {
                Method m = mapperInstance.getClass().getDeclaredMethod("createTable");
                m.invoke(mapperInstance, null);
            } catch (Exception e) {
                log.warn("Unable to create a table for bean " + mapperInstance.getClass().getName(), e);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
