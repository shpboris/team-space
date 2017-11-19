package org.teamspace.enterprise_setup.service.impl;

import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.teamspace.cloud_formation.service.StackCreatorService;
import org.teamspace.commons.utils.AwsEntitiesHelperUtil;
import org.teamspace.deploy.domain.DeployEnterpriseModeRequest;
import org.teamspace.deploy.domain.DeployResponse;
import org.teamspace.deploy.domain.UndeployRequest;
import org.teamspace.enterprise_setup.service.EnterpriseDeployManager;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.teamspace.commons.constants.DeploymentConstants.*;

@Service
@Slf4j
public class EnterpriseDeployManagerImpl implements EnterpriseDeployManager {

    public static final String ENTERPRISE_MODE_CF_TEMPLATE_CLASSPATH_LOCATION = "classpath:enterprise-mode.template";
    public static final String FULL_ENV_STACK_NAME = "FULL-ENV-STACK";

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private StackCreatorService stackCreatorService;

    @Override
    public DeployResponse createEnvironment(DeployEnterpriseModeRequest deployEnterpriseModeRequest) {
        String stackName = getStackName(deployEnterpriseModeRequest);
        List<Parameter> parameters = getStackParameters(deployEnterpriseModeRequest);
        stackCreatorService.createStack(stackName, ENTERPRISE_MODE_CF_TEMPLATE_CLASSPATH_LOCATION, parameters);
        Stack stack = stackCreatorService.waitForStackCreation(stackName, ENTERPRISE_MODE_FULL_STACK_CF_MAX_RETRIES);
        String sitePublicDns = stackCreatorService.getStackOutput(stack, STACK_OUTPUT_SITE_PUBLIC_DNS);
        DeployResponse deployResponse = new DeployResponse(sitePublicDns, null);
        return deployResponse;
    }

    @Override
    public void destroyEnvironment(UndeployRequest undeployRequest) {

    }

    private String getStackName(DeployEnterpriseModeRequest deployEnterpriseModeRequest){
        return String.join("", deployEnterpriseModeRequest.getEnvTag(), "-", FULL_ENV_STACK_NAME);
    }

    private List<Parameter> getStackParameters(DeployEnterpriseModeRequest deployEnterpriseModeRequest){
        Parameter envTag = new Parameter()
                .withParameterKey(STACK_PARAMS_ENV_TAG_KEY).withParameterValue(deployEnterpriseModeRequest.getEnvTag());
        Parameter instanceType = new Parameter()
                .withParameterKey(STACK_PARAMS_INSTANCE_TYPE_KEY).withParameterValue(INSTANCE_TYPE);
        Parameter instanceCount = new Parameter()
                .withParameterKey(STACK_PARAMS_INSTANCE_COUNT_KEY).withParameterValue(deployEnterpriseModeRequest.getInstancesCount().toString());
        String bucketName = AwsEntitiesHelperUtil
                .getEntityName(deployEnterpriseModeRequest.getEnvTag(), BUCKET_ENTITY_TYPE).toLowerCase();
        String userDataStr = getUserDataScript(deployEnterpriseModeRequest.getArtifactName(),
                deployEnterpriseModeRequest.getRegion(), bucketName, DB_MODE_H2,
                deployEnterpriseModeRequest.getUser(), deployEnterpriseModeRequest.getPassword());
        Parameter userData = new Parameter()
                .withParameterKey(STACK_PARAMS_USER_DATA_KEY).withParameterValue(userDataStr);
        return Arrays.asList(envTag, instanceType, instanceCount, userData);
    }

    private String getUserDataScript(String tarFileName, String regionName, String bucketName, String dbMode, String user, String password){
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
        } catch (Exception e){
            log.error("Unable to read user data", e);
            throw new RuntimeException("Unable to read user data");
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        log.info("Got user data script");
        log.debug("\n" + userDataScript);
        return userDataScript;
    }
}
