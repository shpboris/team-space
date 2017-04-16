package org.teamspace.persistence.common.schema;

import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.teamspace.persistence.common.Dao;
import org.teamspace.persistence.common.TableCreator;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import static org.teamspace.persistence.common.CommonConstants.BASE_PACKAGE;

/**
 * Created by shpilb on 16/04/2017.
 */
@Component
@Slf4j
public class SchemaCreator implements ApplicationContextAware{

    private ApplicationContext applicationContext;

    public void createSchema(){
        Reflections reflections = new Reflections(BASE_PACKAGE);
        Set<Class<?>> mapperTypes = reflections.getTypesAnnotatedWith(Dao.class);
        for(Class<?> mapperType : mapperTypes) {
            Map<String, ?> mapperInstances = applicationContext.getBeansOfType(mapperType);
            for (Object mapperInstance : mapperInstances.values()) {
                try {
                    for(Class<?> mapperInterface : mapperInstance.getClass().getInterfaces()){
                        for(Method m : mapperInterface.getDeclaredMethods()){
                            if(m.isAnnotationPresent(TableCreator.class)){
                                m.invoke(mapperInstance, null);
                                break;
                            }
                        }
                    }

                } catch (Exception e) {
                    log.warn("Unable to create a table for bean " + mapperType.getName(), e);
                }
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
