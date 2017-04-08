package org.teamspace.client.api.users;

import org.teamspace.client.ApiException;
import org.teamspace.client.api.UserApi;
import org.teamspace.client.common.BaseClient;
import org.teamspace.client.auth.Retryable;
import org.teamspace.client.model.User;

/**
 * Created by shpilb on 18/02/2017.
 */
public class UsersClientImpl extends BaseClient implements UsersClient{

    private UserApi userApi;

    public UsersClientImpl(){
        userApi = new UserApi();
        userApi.setApiClient(getApiClient());
    }

    @Override
    @Retryable
    public User getCurrentUser() throws ApiException {
        return userApi.getCurrentUser();
    }
}
