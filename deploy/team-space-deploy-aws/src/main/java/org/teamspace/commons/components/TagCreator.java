package org.teamspace.commons.components;

import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.teamspace.aws.client.context.AwsContext;

import static org.teamspace.commons.constants.DeploymentConstants.MAX_RETRIES;
import static org.teamspace.commons.constants.DeploymentConstants.TAG_NAME;
import static org.teamspace.commons.constants.DeploymentConstants.WAIT_TIME_MILLISEC;

/**
 * Created by shpilb on 20/05/2017.
 */
@Slf4j
@Component
public class TagCreator {


    public void createTag(String entityId, String entityType, String tagValue){
        boolean isTagCreated = false;
        int retriesNum = 0;
        while(!isTagCreated && retriesNum < MAX_RETRIES) {
            retriesNum++;
            try {
                CreateTagsRequest createTagsRequest = new CreateTagsRequest();
                createTagsRequest.withResources(entityId)
                        .withTags(new Tag(TAG_NAME, getTagValue(entityType, tagValue)));
                AwsContext.getEc2Client().createTags(createTagsRequest);
                isTagCreated = true;
            } catch (Exception e){
                log.warn("Attempt #" + retriesNum + " to create tag for entity " + entityType);
            }
            if(!isTagCreated) {
                try {
                    Thread.sleep(WAIT_TIME_MILLISEC);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Unable to wait for route creation");
                }
            }
        }
    }

    private String getTagValue(String entityType, String tagValue){
        return tagValue + "-" + entityType;
    }
}
