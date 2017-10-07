package org.teamspace.data_import.job_config.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.teamspace.data_import.domain.DataImportRequest;
import org.teamspace.data_import.job_config.progress_manager.JobProgressManager;

import static org.teamspace.data_import.constants.DataImportConstants.CUSTOM_PARAMETERS_JOB_KEY;

/**
 * Created by shpilb on 06/10/2017.
 */
@Slf4j
public abstract class AbstractTask implements Tasklet{
    @Autowired
    private JobProgressManager jobProgressManager;

    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        log.info("Started task: {}", getClass().getSimpleName());
        JobExecution jobExecution = chunkContext.getStepContext().getStepExecution().getJobExecution();
        DataImportRequest dataImportRequest =
                (DataImportRequest)jobExecution.getExecutionContext().get(CUSTOM_PARAMETERS_JOB_KEY);
        if(dataImportRequest.isForceControlledProgress()){
            String jobKey = jobExecution.getJobParameters().getString(CUSTOM_PARAMETERS_JOB_KEY);
            jobProgressManager.awaitProgressPermission(jobKey);
        }
        innerExecute(stepContribution, chunkContext);
        log.info("Completed task: {}", getClass().getSimpleName());
        return RepeatStatus.FINISHED;
    }

    public abstract RepeatStatus innerExecute(StepContribution stepContribution,
                                              ChunkContext chunkContext) throws Exception;
}
