package org.teamspace.commons.components;

import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.Tag;
import org.springframework.stereotype.Component;
import org.teamspace.aws.client.context.AwsContext;

import static org.teamspace.commons.constants.DeploymentConstants.TAG_NAME;

/**
 * Created by shpilb on 20/05/2017.
 */
@Component
public class TagCreator {


    public void createTag(String entityId, String entityType, String tagValue){
        CreateTagsRequest createTagsRequest = new CreateTagsRequest();
        createTagsRequest.withResources(entityId)
                .withTags(new Tag(TAG_NAME, getTagValue(entityType, tagValue)));
        AwsContext.getEc2Client().createTags(createTagsRequest);
    }

    private String getTagValue(String entityType, String tagValue){
        return tagValue + "-" + entityType;
    }
}
