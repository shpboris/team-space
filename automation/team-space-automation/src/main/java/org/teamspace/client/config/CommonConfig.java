package org.teamspace.client.config;

import org.teamspace.client.common.config.annotations.PropertiesSource;
import org.teamspace.client.common.config.annotations.PropertyLocation;

/**
 * Created by shpilb on 01/05/2017.
 */
@PropertiesSource("/config/tests-common.properties")
public interface CommonConfig {

    @PropertyLocation("server.base-path")
    public String getBasePath();

    @PropertyLocation("auth.grant-type")
    public String getGrantType();

    @PropertyLocation("auth.user")
    public String getUser();

    @PropertyLocation("auth.password")
    public String getPassword();

    @PropertyLocation("api-client.timeout-sec")
    public Integer getTimeout();

    @PropertyLocation("roles.admin")
    public String getAdminRole();

    @PropertyLocation("roles.user")
    public String getUserRole();
}
