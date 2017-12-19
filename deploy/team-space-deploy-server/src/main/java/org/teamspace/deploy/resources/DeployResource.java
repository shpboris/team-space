package org.teamspace.deploy.resources;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.teamspace.commons.constants.DeploymentConstants;
import org.teamspace.deploy.service.DeployService;
import org.teamspace.deploy_azure.service.AzureDeployService;
import org.teamspace.deploy_common.domain.*;

import static org.teamspace.deploy_common.constants.DeployCommonConstants.AWS_CLOUD_TYPE;
import static org.teamspace.deploy_common.constants.DeployCommonConstants.AZURE_CLOUD_TYPE;

@Api(value = "Deploy", tags = "Deploy", description = "The API for deployment")
@Controller
public class DeployResource {

	@Autowired
	private DeployService deployService;

	@Autowired
	private AzureDeployService azureDeployService;

	@ApiOperation(value = "execute enterprise mode deploy", response = DeployResponse.class)
	@RequestMapping(value = "/deployEnterpriseMode", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DeployResponse> deploy(@ApiParam(name = "deployEnterpriseModeRequest", required = true)
												 @RequestBody DeployEnterpriseModeRequest deployEnterpriseModeRequest) {
		if(deployEnterpriseModeRequest.getRegion() == null){
			deployEnterpriseModeRequest.setRegion(DeploymentConstants.DEFAULT_REGION_NAME);
		}
		DeployResponse deployResponse = deployService.deploy(deployEnterpriseModeRequest);
		HttpHeaders httpHeaders = new HttpHeaders();
		return new ResponseEntity<>(deployResponse, httpHeaders, HttpStatus.CREATED);
	}

	@ApiOperation(value = "execute deploy", response = DeployResponse.class)
	@RequestMapping(value = "/deploy", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DeployResponse> deploy(@ApiParam(name = "deployRequest", required = true)
													 @RequestBody DeployRequest deployRequest) {
		DeployResponse deployResponse = null;
		if(deployRequest.getCloudType().equals(AWS_CLOUD_TYPE)) {
			deployResponse = deployService.deploy(deployRequest);
		} else if(deployRequest.getCloudType().equals(AZURE_CLOUD_TYPE)) {
			deployResponse = azureDeployService.deploy(deployRequest);
		}
		HttpHeaders httpHeaders = new HttpHeaders();
		return new ResponseEntity<>(deployResponse, httpHeaders, HttpStatus.CREATED);
	}

	@ApiOperation(value = "execute enterprise mode undeploy", response = HttpStatus.class)
	@RequestMapping(value = "/undeployEnterpriseMode", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HttpStatus> undeploy(@ApiParam(name = "undeployRequest", required = true)
											   @RequestBody UndeployEnterpriseModeRequest undeployEnterpriseModeRequest) {
		if(undeployEnterpriseModeRequest.getRegion() == null){
			undeployEnterpriseModeRequest.setRegion(DeploymentConstants.DEFAULT_REGION_NAME);
		}
		deployService.undeploy(undeployEnterpriseModeRequest);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@ApiOperation(value = "execute undeploy", response = HttpStatus.class)
	@RequestMapping(value = "/undeploy", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HttpStatus> undeploy(@ApiParam(name = "undeployRequest", required = true)
											   @RequestBody UndeployRequest undeployRequest) {
		if(undeployRequest.getRegion() == null){
			undeployRequest.setRegion(DeploymentConstants.DEFAULT_REGION_NAME);
		}
		deployService.undeploy(undeployRequest);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}