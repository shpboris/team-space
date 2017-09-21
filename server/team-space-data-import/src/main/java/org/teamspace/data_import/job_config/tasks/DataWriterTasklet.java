package org.teamspace.data_import.job_config.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.teamspace.auth.domain.User;
import org.teamspace.data_import.domain.DataImportRequest;
import org.teamspace.users.service.UsersService;

import java.util.List;

import static org.teamspace.data_import.constants.DataImportConstants.CUSTOM_PARAMETERS_JOB_KEY;
import static org.teamspace.data_import.constants.DataImportConstants.USERS_DATA_JOB_KEY;

/**
 * Created by shpilb on 08/09/2017.
 */
@Slf4j
public class DataWriterTasklet implements Tasklet {

    @Autowired
    private UsersService usersService;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        log.info("Started import users to database");
        JobExecution jobExecution = chunkContext.getStepContext().getStepExecution().getJobExecution();
        List<User> users = (List<User>)jobExecution.getExecutionContext().get(USERS_DATA_JOB_KEY);
        DataImportRequest dataImportRequest = (DataImportRequest)jobExecution
                .getExecutionContext().get(CUSTOM_PARAMETERS_JOB_KEY);
        if(dataImportRequest.isShouldPerformCleanup()) {
            usersService.deleteNonAdminUsers();
        }
        usersService.importUsers(users);
        log.info("Completed import users to database");
        return RepeatStatus.FINISHED;
    }
}
