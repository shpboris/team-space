package org.teamspace.data_import.job_config.parameters_registry;

import org.springframework.stereotype.Component;
import org.teamspace.data_import.domain.DataImportRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shpilb on 21/09/2017.
 */
@Component
public class JobParametersRegistry {

    private Map<String, DataImportRequest> jobParameters = new HashMap<>();

    public synchronized void addParameter(String jobKey, DataImportRequest dataImportRequest){
        jobParameters.put(jobKey, dataImportRequest);
    }

    public synchronized DataImportRequest getParameter(String jobKey){
        return jobParameters.get(jobKey);
    }

    public synchronized void removeParameter(String jobKey){
        jobParameters.remove(jobKey);
    }
}
