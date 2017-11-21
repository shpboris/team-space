package org.teamspace.commons.utils;

import static org.teamspace.commons.constants.DeploymentConstants.DB_HOST;
import static org.teamspace.commons.constants.DeploymentConstants.DB_NAME;
import static org.teamspace.commons.constants.DeploymentConstants.DB_URL_TEMPLATE;

public class DbHelperUtil {

    public static String getDbUrl(String dbInstancePrivateDns, String tarFileName){
        String dbUrl = DB_URL_TEMPLATE.replace(DB_HOST, dbInstancePrivateDns);
        String dbName = getDbNormalizedName(tarFileName);
        dbUrl = dbUrl.replace(DB_NAME, dbName);
        return dbUrl;
    }

    public static String getDbNormalizedName(String tarFileName){
        String dbName = tarFileName.replaceAll("[^a-zA-Z0-9]+","");
        return  dbName;
    }
}
