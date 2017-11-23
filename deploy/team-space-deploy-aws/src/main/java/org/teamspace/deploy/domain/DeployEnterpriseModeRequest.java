package org.teamspace.deploy.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * Created by shpilb on 06/05/2017.
 */
@Data
public class DeployEnterpriseModeRequest extends DeployRequest{

    private Integer instancesCount;
    private boolean singleStackDeployment = true;

    @JsonIgnore
    public String getDbMode() {
        return dbMode;
    }

    @JsonIgnore
    public void setDbMode(String dbMode) {
        this.dbMode = dbMode;
    }
}
