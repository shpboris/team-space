package org.teamspace.client.common.config;

import lombok.extern.slf4j.Slf4j;
import org.teamspace.client.common.config.annotations.PropertiesSource;

import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * Created by shpilb on 01/05/2017.
 */
@Slf4j
public class ConfigurationManager {

    private Map<Class, Object> configs = new HashMap<>();
    private static ConfigurationManager configurationManager = new ConfigurationManager();

    private ConfigurationManager(){

    }

    public static ConfigurationManager getInstance(){
        return configurationManager;
    }

    public <T> T getConfiguration(Class<T> clazz){
        T proxy = null;
        if(configs.get(clazz) != null){
            proxy = (T)configs.get(clazz);
        } else {
            if(clazz.isAnnotationPresent(PropertiesSource.class)){
                PropertiesSource propertiesSource = clazz.getAnnotation(PropertiesSource.class);
                Properties properties = getProperties(propertiesSource.value());
                ConfigInvocationHandler handler = new ConfigInvocationHandler(properties);
                proxy = (T)Proxy.newProxyInstance(
                        clazz.getClassLoader(),
                        new Class[] { clazz },
                        handler);
                configs.put(clazz, proxy);
            }
            else{
                throw new RuntimeException("Configuration location is undefined");
            }
        }
        return proxy;
    }

    private Properties getProperties(String propsLocation){
        Properties properties = new Properties();
        InputStream is = null;
        try {
            is = this.getClass().getResourceAsStream(propsLocation);
            properties.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Unable to read properties");
        }
        return properties;
    }

}
