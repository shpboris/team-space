package org.teamspace.commons.components;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.teamspace.deploy_common.constants.DeployCommonConstants;

import java.io.InputStream;

import static org.teamspace.commons.constants.DeploymentConstants.*;
import static org.teamspace.commons.utils.DbHelperUtil.getDbNormalizedName;

@Component
@Slf4j
public class UserDataHelper {

    @Autowired
    private ResourceLoader resourceLoader;

    public String getUserDataScript(String userDataClasspathLocation, String tarFileName, String regionName, String bucketName,
                                     String dbMode, String dbInstancePrivateDns, String user,
                                     String password, boolean isEncodingRequired){
        log.info("Getting user data script ...");
        String userDataScript = null;
        InputStream inputStream = null;
        try {
            Resource resource = resourceLoader.getResource(userDataClasspathLocation);
            inputStream = resource.getInputStream();
            userDataScript = IOUtils.toString(inputStream, "UTF-8");
            userDataScript = userDataScript.replace(DeployCommonConstants.TAR_FILE_NAME, tarFileName);
            userDataScript = userDataScript.replace(REGION_NAME, regionName);
            userDataScript = userDataScript.replace(BUCKET_NAME, bucketName);
            userDataScript = userDataScript.replace(USER, user);
            userDataScript = userDataScript.replace(DeployCommonConstants.PASSWORD, password);
            userDataScript = userDataScript.replace(DeployCommonConstants.DB_MODE, dbMode);
            userDataScript = userDataScript.replace(DeployCommonConstants.DB_NAME, getDbNormalizedName(tarFileName));
            if(dbMode.equals(DeployCommonConstants.DB_MODE_MYSQL) || dbMode.equals(DB_MODE_RDS)) {
                userDataScript = userDataScript.replace(DeployCommonConstants.DB_NAME, getDbNormalizedName(tarFileName));
                userDataScript = userDataScript.replace(DeployCommonConstants.DB_HOST, dbInstancePrivateDns);
            }
        } catch (Exception e){
            log.error("Unable to read user data", e);
            throw new RuntimeException("Unable to read user data");
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        log.info("Got user data script");
        log.debug("\n" + userDataScript);
        if(isEncodingRequired) {
            userDataScript = new String(Base64.encodeBase64(userDataScript.getBytes()));
        }
        return userDataScript;
    }

    public String getDbUserDataScript(String tarFileName, String user, String password){
        log.info("Getting DB user data script ...");
        String userDataScript = null;
        InputStream inputStream = null;
        try {
            Resource resource = resourceLoader.getResource("classpath:db_user_data.sh");
            inputStream = resource.getInputStream();
            userDataScript = IOUtils.toString(inputStream, "UTF-8");
            userDataScript = userDataScript.replace(USER, user);
            userDataScript = userDataScript.replace(DeployCommonConstants.PASSWORD, password);
            userDataScript = userDataScript.replace(DeployCommonConstants.DB_NAME, getDbNormalizedName(tarFileName));
        } catch (Exception e){
            throw new RuntimeException("Unable to read user data");
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        log.info("Got DB user data script");
        log.debug("\n" + userDataScript);
        userDataScript = new String(Base64.encodeBase64(userDataScript.getBytes()));
        return userDataScript;
    }


}
