package org.teamspace.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.teamspace.client.api.users.UsersClient;
import org.teamspace.client.model.User;

/**
 * Created by shpilb on 08/04/2017.
 */
@Slf4j
public class TeamSpaceClient {

    private static AnnotationConfigApplicationContext annotationConfigApplicationContext;
    public static final String TEAM_SPACE_CLIENT_BASE_PACKAGE = "org.teamspace.client";

    public static void main(String[] args) {

        annotationConfigApplicationContext =
                new AnnotationConfigApplicationContext(TEAM_SPACE_CLIENT_BASE_PACKAGE);

        UsersClient usersClient = annotationConfigApplicationContext.getBean(UsersClient.class);
        try {
            User currentUser = usersClient.getCurrentUser();
            log.info("user name is - " + currentUser.getUsername());
        } catch (ApiException e) {
            log.error(e.getMessage(), e);
        }
    }

}
