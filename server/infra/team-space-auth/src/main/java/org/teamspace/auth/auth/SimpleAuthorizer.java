package org.teamspace.auth.auth;

import io.dropwizard.auth.Authorizer;
import org.teamspace.auth.domain.User;

public class SimpleAuthorizer implements Authorizer<User> {
	public boolean authorize(User user, String role) {
        return true;
    }
}
