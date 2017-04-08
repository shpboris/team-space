package org.teamspace.client.config;

import org.springframework.context.annotation.*;
import org.teamspace.client.api.users.UsersClientImpl;
import org.teamspace.client.auth.AuthRetryHandler;

/**
 * Created by shpilb on 08/04/2017.
 */
@Configuration
@EnableAspectJAutoProxy
public class ClientConfig {
    @Bean
    UsersClientImpl usersClientImpl(){
        return new UsersClientImpl();
    }

    @Bean
    AuthRetryHandler authHandler(){
        return new AuthRetryHandler();
    }
}
