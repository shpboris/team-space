package org.teamspace.lifecycle;

import io.dropwizard.lifecycle.Managed;
import org.h2.tools.Server;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.teamspace.persistence.common.schema.SchemaCreator;
import org.teamspace.users.service.UsersService;

/**
 * Created by shpilb on 28/04/2017.
 */
public class TeamSpaceLifecycleHook implements Managed {

    private AnnotationConfigApplicationContext annotationConfigApplicationContext;
    private Server server;

    public TeamSpaceLifecycleHook(AnnotationConfigApplicationContext annotationConfigApplicationContext) {
        this.annotationConfigApplicationContext = annotationConfigApplicationContext;
    }

    @Override
    public void start() throws Exception {
        server = Server.createTcpServer().start();
        SchemaCreator schemaCreator = annotationConfigApplicationContext.getBean(SchemaCreator.class);
        UsersService usersService = annotationConfigApplicationContext.getBean(UsersService.class);
        schemaCreator.createSchema();
        usersService.init();
    }

    @Override
    public void stop() throws Exception {
        server.stop();
    }
}
