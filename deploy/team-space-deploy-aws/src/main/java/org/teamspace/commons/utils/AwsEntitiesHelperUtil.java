package org.teamspace.commons.utils;

/**
 * Created by shpilb on 20/05/2017.
 */
public class AwsEntitiesHelperUtil {
    public static String getEntityName(String envTag, String entityType){
        return envTag + "-" + entityType;
    }
}
