package org.teamspace.bundler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teamspace.auth.resources.TokenProviderResource;
import org.teamspace.users.resources.UserResource;

/**
 * Created by shpilb on 08/04/2017.
 */
@Component
public class Bundler {
    @Autowired
    private TokenProviderResource tokenProviderResource;
    @Autowired
    private UserResource userResource;

    public Object[] getAllResources() {
        return new Object[]{
                tokenProviderResource,
                userResource
        };
    }
}
