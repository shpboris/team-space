package org.teamspace.data_import.job_config.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.teamspace.auth.domain.User;
import org.teamspace.data_import.domain.DataImportRequest;
import org.teamspace.data_import.service.DatabaseImportService;
import org.teamspace.groups.domain.Group;
import org.teamspace.membership.domain.Membership;

import java.util.List;

import static org.teamspace.data_import.constants.DataImportConstants.*;

/**
 * Created by shpilb on 08/09/2017.
 */
@Slf4j
public class DataWriterTasklet extends AbstractTask {

    @Autowired
    private DatabaseImportService databaseImportService;

    @Override
    public RepeatStatus innerExecute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        log.info("Started import to database");
        JobExecution jobExecution = chunkContext.getStepContext().getStepExecution().getJobExecution();
        List<User> users = (List<User>)jobExecution.getExecutionContext().get(USERS_DATA_JOB_KEY);
        List<Group> groups = (List<Group>)jobExecution.getExecutionContext().get(GROUPS_DATA_JOB_KEY);
        List<Membership> memberships = (List<Membership>)jobExecution.getExecutionContext().get(MEMBERSHIP_DATA_JOB_KEY);
        DataImportRequest dataImportRequest = (DataImportRequest)jobExecution
                .getExecutionContext().get(CUSTOM_PARAMETERS_JOB_KEY);
        databaseImportService.importData(users, groups, memberships, dataImportRequest.isShouldPerformCleanup());
        log.info("Completed import to database");
        return RepeatStatus.FINISHED;
    }

}
