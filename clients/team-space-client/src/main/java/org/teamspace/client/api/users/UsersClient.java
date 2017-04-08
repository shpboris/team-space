package org.teamspace.client.api.users;

import org.teamspace.client.ApiException;
import org.teamspace.client.model.User;

/**
 * Created by shpilb on 08/04/2017.
 */
public interface UsersClient {
    public User getCurrentUser() throws ApiException;
}
