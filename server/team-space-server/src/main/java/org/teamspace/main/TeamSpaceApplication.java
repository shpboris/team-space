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
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.teamspace.auth.auth.GenericAuthorizer;
import org.teamspace.auth.auth.OAuth2Authenticator;
import org.teamspace.auth.domain.User;
import org.teamspace.bundler.Bundler;
import org.teamspace.lifecycle.TeamSpaceLifecycleHook;

import java.nio.charset.StandardCharsets;

/**
 * Created by shpilb on 01/04/2017.
 */
public class TeamSpaceApplication extends Application<Configuration> {

    private AnnotationConfigApplicationContext annotationConfigApplicationContext = null;

    public static final String SWAGGER_UI_VERSION = "2.2.10";
    public static final String SWAGGER_JSON_FILE = "swagger.json";
    public static final String SWAGGER_JSON_PATH = "/" + SWAGGER_JSON_FILE;
    public static final String SWAGGER_UI_PATH = "/";
    public static final String API_DOC_PATH = "/api-doc";
    public static final String TEAM_SPACE_BASE_PACKAGE = "org.teamspace";


    public static void main(String[] args) throws Exception {
        new TeamSpaceApplication().run(args);
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        annotationConfigApplicationContext =
                new AnnotationConfigApplicationContext(TEAM_SPACE_BASE_PACKAGE);
        Bundler bundler = annotationConfigApplicationContext.getBean(Bundler.class);
        registerRestResources(bundler, environment);
        registerSecurityAssets(environment);
        addSwaggerUi(environment);
        environment.lifecycle().manage(new TeamSpaceLifecycleHook(annotationConfigApplicationContext));
    }

    private void registerSecurityAssets(Environment environment){

        OAuth2Authenticator oauth2Authenticator = annotationConfigApplicationContext.getBean(OAuth2Authenticator.class);
        GenericAuthorizer genericAuthorizer = annotationConfigApplicationContext.getBean(GenericAuthorizer.class);

        AuthFilter<String, User> oauthCredentialAuthFilter = new OAuthCredentialAuthFilter.Builder<User>()
                .setAuthenticator(oauth2Authenticator)
                .setAuthorizer(genericAuthorizer)
                .setPrefix("Bearer")
                .buildAuthFilter();

        environment.jersey().register(new AuthDynamicFeature(oauthCredentialAuthFilter));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new AuthValueFactoryProvider.Binder<User>(User.class));
    }

    private void registerRestResources(Bundler bundler, Environment environment){
        Object [] resources = bundler.getAllResources();
        for(Object resource : resources){
            environment.jersey().register(resource);
        }
    }

    //main orchestrator method to configure Swagger UI
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
