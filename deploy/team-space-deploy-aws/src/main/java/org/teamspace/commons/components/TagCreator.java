package org.teamspace.commons.components;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teamspace.aws.client.AwsClientFactory;

import javax.annotation.PostConstruct;

import static org.teamspace.commons.constants.DeploymentConstants.TAG_NAME;

/**
 * Created by shpilb on 20/05/2017.
 */
@Component
public class TagCreator {

    @Autowired
    private AwsClientFactory awsClientFactory;

    private AmazonEC2 ec2Client;


    @PostConstruct
    private void initClients(){
        ec2Client = awsClientFactory.getEc2Client();
    }

    public void createTag(String entityId, String entityType, String tagValue){
        CreateTagsRequest createTagsRequest = new CreateTagsRequest();
        createTagsRequest.withResources(entityId)
                .withTags(new Tag(TAG_NAME, getTagValue(entityType, tagValue)));
        ec2Client.createTags(createTagsRequest);
    }

    private String getTagValue(String entityType, String tagValue){
        return tagValue + "-" + entityType;
    }
}
