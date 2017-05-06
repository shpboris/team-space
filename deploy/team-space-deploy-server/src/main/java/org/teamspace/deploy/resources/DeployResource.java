package org.teamspace.deploy.resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.teamspace.deploy.service.DeployService;

@RestController
public class DeployResource {

	@Autowired
	private DeployService deployService;

	@RequestMapping("/deploy")
	public String hello(@RequestParam String artifactLocation) {
		return deployService.deploy(artifactLocation);
	}
}