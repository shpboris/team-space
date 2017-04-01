package org.teamspace.main;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.auth.*;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.teamspace.auth.auth.SimpleAuthenticator;
import org.teamspace.auth.auth.SimpleAuthorizer;
import org.teamspace.auth.dao.TokenProviderDAO;
import org.teamspace.auth.domain.User;
import org.teamspace.auth.resources.TokenProviderResource;
import org.teamspace.users.resources.UserResource;

/**
 * Created by shpilb on 01/04/2017.
 */
public class TeamSpaceApplication extends Application<Configuration> {
    public static void main(String[] args) throws Exception {
        new TeamSpaceApplication().run(args);
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        environment.jersey().register(new UserResource());
        environment.jersey().register(new TokenProviderResource());
        TokenProviderDAO accessTokenDAO = new TokenProviderDAO();

        AuthFilter<String, User> oauthCredentialAuthFilter = new OAuthCredentialAuthFilter.Builder<User>()
                .setAuthenticator(new SimpleAuthenticator(accessTokenDAO))
                .setAuthorizer(new SimpleAuthorizer())
                .setPrefix("Bearer")
                .buildAuthFilter();

        environment.jersey().register(new AuthDynamicFeature(oauthCredentialAuthFilter));

        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
    }
}
