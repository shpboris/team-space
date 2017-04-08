package org.teamspace.auth.auth;

import io.dropwizard.auth.Authorizer;
import org.springframework.stereotype.Component;
import org.teamspace.auth.domain.User;
@Component
public class GenericAuthorizer implements Authorizer<User> {
	public boolean authorize(User user, String role) {
        return true;
    }
}
