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
import org.teamspace.membership.domain.Membership;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import static org.teamspace.data_import.constants.DataImportConstants.MEMBERSHIP_DATA_JOB_KEY;
import static org.teamspace.data_import.constants.DataImportConstants.MEMBERSHIP_FILE_NAME;

/**
 * Created by shpilb on 08/09/2017.
 */
@Slf4j
public class MembershipDataReaderTasklet implements Tasklet {

    @Value("${dataImportDir}")
    private String dataImportDir;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        log.info("Started reading membership data from file");
        Path membershipDataFilePath = Paths.get(dataImportDir, MEMBERSHIP_FILE_NAME);
        String membershipDataStr = new String(Files.readAllBytes(membershipDataFilePath), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        List<Membership> membershipList = mapper.readValue(membershipDataStr, new TypeReference<List<Membership>>(){});
        JobExecution jobExecution = chunkContext.getStepContext().getStepExecution().getJobExecution();
        jobExecution.getExecutionContext().put(MEMBERSHIP_DATA_JOB_KEY, membershipList);
        log.info("Finished reading membership data from file");
        return RepeatStatus.FINISHED;
    }
}
