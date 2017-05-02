package org.teamspace.client.common.config;

import org.teamspace.client.common.config.annotations.PropertyLocation;

import java.lang.reflect.*;
import java.util.Properties;

/**
 * Created by shpilb on 01/05/2017.
 */
public class ConfigInvocationHandler implements InvocationHandler {

    private Properties properties;

    public ConfigInvocationHandler(Properties properties){
        this.properties = properties;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object res = null;
        if(method.isAnnotationPresent(PropertyLocation.class)){
            PropertyLocation propertyLocation = method.getAnnotation(PropertyLocation.class);
            if(propertyLocation.value() == null){
                throw new RuntimeException("The value for property " + propertyLocation.value() + " not found");
            }
            String property = (String)properties.get(propertyLocation.value());
            res = convertValueToRequiredType(method.getReturnType().getName(), property);
        } else {
            throw new RuntimeException("Property location is not configured");
        }
        return res;
    }

    private Object convertValueToRequiredType(String className, String value) throws Throwable{
        Class<?> cl = Class.forName(className);
        Constructor<?> cons = cl.getConstructor(String.class);
        Object object = cons.newInstance(value);
        return object;
    }
}
