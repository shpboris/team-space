package org.teamspace.data_import.job_config.tasks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.teamspace.auth.domain.User;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import static org.teamspace.data_import.constants.DataImportConstants.USERS_DATA_JOB_KEY;
import static org.teamspace.data_import.constants.DataImportConstants.USERS_FILE_NAME;

/**
 * Created by shpilb on 08/09/2017.
 */
@Slf4j
public class UsersDataReaderTasklet implements Tasklet {

    @Value("${dataImportDir}")
    private String dataImportDir;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        log.info("Started reading users data from file");
        Path usersDataFilePath = Paths.get(dataImportDir, USERS_FILE_NAME);
        String usersDataStr = new String(Files.readAllBytes(usersDataFilePath), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        List<User> usersList = mapper.readValue(usersDataStr, new TypeReference<List<User>>(){});
        JobExecution jobExecution = chunkContext.getStepContext().getStepExecution().getJobExecution();
        jobExecution.getExecutionContext().put(USERS_DATA_JOB_KEY, usersList);
        log.info("Finished reading users data from file");
        return RepeatStatus.FINISHED;
    }
}
