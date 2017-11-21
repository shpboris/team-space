package org.teamspace.commons.components;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;

import static org.teamspace.commons.constants.DeploymentConstants.*;
import static org.teamspace.commons.utils.DbHelperUtil.getDbNormalizedName;
import static org.teamspace.commons.utils.DbHelperUtil.getDbUrl;

@Component
@Slf4j
public class UserDataHelper {

    @Autowired
    private ResourceLoader resourceLoader;

    public String getUserDataScript(String tarFileName, String regionName, String bucketName,
                                     String dbMode, String dbInstancePrivateDns, String user,
                                     String password, boolean isEncodingRequired){
        log.info("Getting user data script ...");
        String userDataScript = null;
        InputStream inputStream = null;
        try {
            Resource resource = resourceLoader.getResource("classpath:user_data.sh");
            inputStream = resource.getInputStream();
            userDataScript = IOUtils.toString(inputStream, "UTF-8");
            userDataScript = userDataScript.replace(TAR_FILE_NAME, tarFileName);
            userDataScript = userDataScript.replace(REGION_NAME, regionName);
            userDataScript = userDataScript.replace(BUCKET_NAME, bucketName);
            userDataScript = userDataScript.replace(USER, user);
            userDataScript = userDataScript.replace(PASSWORD, password);
            userDataScript = userDataScript.replace(DB_MODE, dbMode);
            if(dbMode.equals(DB_MODE_MYSQL) || dbMode.equals(DB_MODE_RDS)) {
                userDataScript = userDataScript.replace(DB_HOST, dbInstancePrivateDns);
                String dbUrl = getDbUrl(dbInstancePrivateDns, tarFileName);
                userDataScript = userDataScript.replace(DB_URL, dbUrl);
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
            userDataScript = userDataScript.replace(PASSWORD, password);
            userDataScript = userDataScript.replace(DB_NAME, getDbNormalizedName(tarFileName));
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
