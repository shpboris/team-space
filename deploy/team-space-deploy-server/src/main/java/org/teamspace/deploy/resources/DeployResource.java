package org.teamspace.deploy.resources;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.teamspace.commons.constants.DeploymentConstants;
import org.teamspace.deploy.domain.*;
import org.teamspace.deploy.service.DeployService;

@Api(value = "Deploy", tags = "Deploy", description = "The API for deployment")
@Controller
public class DeployResource {

	@Autowired
	private DeployService deployService;

	@ApiOperation(value = "execute deploy", response = DeployResponse.class)
	@RequestMapping(value = "/deploy", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DeployResponse> deploy(@ApiParam(name = "deployRequest", required = true)
													 @RequestBody DeployRequest deployRequest) {
		if(deployRequest.getRegion() == null){
			deployRequest.setRegion(DeploymentConstants.DEFAULT_REGION_NAME);
		}
		DeployResponse deployResponse = deployService.deploy(deployRequest);
		HttpHeaders httpHeaders = new HttpHeaders();
		return new ResponseEntity<>(deployResponse, httpHeaders, HttpStatus.CREATED);
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