package org.teamspace.persistence.common.schema;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.teamspace.persistence.common.Dao;
import org.teamspace.persistence.common.TableCreator;

import java.lang.reflect.Method;
import java.util.*;

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
        Map<Integer, Pair<Method, Object>> orderToTableCreationMethodMap = new HashMap<>();
        for(Class<?> mapperType : mapperTypes) {
            Map<String, ?> mapperInstances = applicationContext.getBeansOfType(mapperType);
            for (Object mapperInstance : mapperInstances.values()) {
                    for(Class<?> mapperInterface : mapperInstance.getClass().getInterfaces()){
                        for(Method m : mapperInterface.getDeclaredMethods()){
                            if(m.isAnnotationPresent(TableCreator.class)){
                                TableCreator tableCreator = m.getAnnotation(TableCreator.class);
                                Pair<Method, Object> pair = new ImmutablePair<>(m, mapperInstance);
                                orderToTableCreationMethodMap.put(tableCreator.creationOrder(), pair);
                                break;
                            }
                        }
                    }
            }
        }
        for(int i=0; i<orderToTableCreationMethodMap.size(); i++){
            Pair<Method, Object> pair = orderToTableCreationMethodMap.get(i);
            try {
                pair.getLeft().invoke(pair.getRight());
            } catch (Exception e){
                log.warn("Unable to create a table for bean {}", pair.getRight().getClass().getName(), e);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
