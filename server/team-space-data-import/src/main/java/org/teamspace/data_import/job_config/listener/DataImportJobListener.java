package org.teamspace.data_import.job_config.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teamspace.data_import.domain.DataImportRequest;
import org.teamspace.data_import.job_config.parameters_registry.JobParametersRegistry;
import org.teamspace.data_import.job_config.progress_manager.JobProgressManager;

import static org.teamspace.data_import.constants.DataImportConstants.CUSTOM_PARAMETERS_JOB_KEY;

/**
 * Created by shpilb on 21/09/2017.
 */
@Component
@Slf4j
public class DataImportJobListener implements JobExecutionListener {

    @Autowired
    private JobParametersRegistry jobParametersRegistry;

    @Autowired
    private JobProgressManager jobProgressManager;

    @Autowired
    private JobRepository jobRepository;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String jobKey = jobExecution.getJobParameters().getString(CUSTOM_PARAMETERS_JOB_KEY);
        log.info("Starting a job with a key: {}", jobKey);
        DataImportRequest dataImportRequest = jobParametersRegistry.getParameter(jobKey);
        //exclude job restart
        if(dataImportRequest != null) {
            jobExecution.getExecutionContext().put(CUSTOM_PARAMETERS_JOB_KEY, dataImportRequest);
            jobRepository.updateExecutionContext(jobExecution);
            jobParametersRegistry.removeParameter(jobKey);
        } else {
            dataImportRequest = (DataImportRequest)jobExecution.getExecutionContext().get(CUSTOM_PARAMETERS_JOB_KEY);
        }
        if(dataImportRequest.isForceControlledProgress()) {
            jobProgressManager.registerJob(jobKey);
        }
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobKey = jobExecution.getJobParameters().getString(CUSTOM_PARAMETERS_JOB_KEY);
        log.info("Job: {} with parameter: {} finished with status: {}",
                jobExecution.getJobInstance().getJobName(), jobKey, jobExecution.getStatus());
        DataImportRequest dataImportRequest =
                (DataImportRequest)jobExecution.getExecutionContext().get(CUSTOM_PARAMETERS_JOB_KEY);
        if(dataImportRequest.isForceControlledProgress()){
            jobProgressManager.unregisterJob(jobKey);
        }
        if(jobExecution.getStatus().equals(BatchStatus.FAILED)){
            if(jobExecution.getAllFailureExceptions() != null){
                log.error("Logging all failures");
                jobExecution.getAllFailureExceptions().forEach(e -> {
                    log.error(e.getMessage(), e);
                });
            }
        }
    }
}
