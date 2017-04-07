package org.teamspace.main;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.*;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.servlets.assets.AssetServlet;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.rewrite.handler.RedirectPatternRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.teamspace.auth.auth.SimpleAuthenticator;
import org.teamspace.auth.auth.SimpleAuthorizer;
import org.teamspace.auth.dao.TokenProviderDAO;
import org.teamspace.auth.domain.User;
import org.teamspace.auth.resources.TokenProviderResource;
import org.teamspace.users.resources.UserResource;

import java.nio.charset.StandardCharsets;

/**
 * Created by shpilb on 01/04/2017.
 */
public class TeamSpaceApplication extends Application<Configuration> {

    public static final String SWAGGER_UI_VERSION = "2.2.10";
    public static final String SWAGGER_JSON_FILE = "swagger.json";
    public static final String SWAGGER_JSON_PATH = "/" + SWAGGER_JSON_FILE;
    public static final String SWAGGER_UI_PATH = "/";
    public static final String API_DOC_PATH = "/api-doc";

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
        addSwaggerUi(environment);
    }

    private void addSwaggerUi(Environment environment) {
        serveSwaggerUiStaticContent(environment);
        serveSwaggerJson(environment);
        redirectApiDocToSwaggerUiWithSwaggerDoc(environment);
    }

    //allows to access team-space UI under "/api-doc" path instead of "/?url=swagger.json"
    private void redirectApiDocToSwaggerUiWithSwaggerDoc(Environment environment) {
        RewriteHandler handler = new RewriteHandler();
        RedirectPatternRule redirectPatternRule = new RedirectPatternRule();
        redirectPatternRule.setPattern(API_DOC_PATH);
        redirectPatternRule.setLocation(SWAGGER_UI_PATH + "?url=" + SWAGGER_JSON_PATH);
        handler.addRule(redirectPatternRule);
        environment.getApplicationContext().insertHandler(handler);
    }

    //allows to server team-space specific swagger.json under "/swagger.json" path
    //so now making call like "/?url=swagger.json" serves custom team-space UI !
    private void serveSwaggerJson(Environment environment) {
        AssetServlet swaggerJsonServlet = new AssetServlet(SWAGGER_JSON_PATH, SWAGGER_JSON_PATH, null, StandardCharsets.UTF_8);
        environment.servlets().addServlet(SWAGGER_JSON_FILE, swaggerJsonServlet).addMapping(SWAGGER_JSON_PATH);
    }

    //allows to serve default swagger webjar UI (petstore) under "/" path
    private void serveSwaggerUiStaticContent(Environment environment) {
        AssetsBundle swaggerUiBundle = new AssetsBundle(getSwaggerUiResourcePath(), SWAGGER_UI_PATH, "index.html", "swaggerUiAssets");
        swaggerUiBundle.run(environment);
    }

    //location of swagger UI webjar
    private String getSwaggerUiResourcePath() {
        return "/META-INF/resources/webjars/swagger-ui/" + SWAGGER_UI_VERSION + "/";
    }
}
