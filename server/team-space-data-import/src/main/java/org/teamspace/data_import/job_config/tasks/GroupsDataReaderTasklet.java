package org.teamspace.data_import.job_config.tasks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.teamspace.groups.domain.Group;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import static org.teamspace.data_import.constants.DataImportConstants.GROUPS_DATA_JOB_KEY;
import static org.teamspace.data_import.constants.DataImportConstants.GROUPS_FILE_NAME;

/**
 * Created by shpilb on 08/09/2017.
 */
@Slf4j
public class GroupsDataReaderTasklet extends AbstractTask {

    @Value("${dataImportDir}")
    private String dataImportDir;

    @Override
    public RepeatStatus innerExecute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        log.info("Started reading groups data from file");
        Path groupsDataFilePath = Paths.get(dataImportDir, GROUPS_FILE_NAME);
        String groupsDataStr = new String(Files.readAllBytes(groupsDataFilePath), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        List<Group> groupsList = mapper.readValue(groupsDataStr, new TypeReference<List<Group>>(){});
        JobExecution jobExecution = chunkContext.getStepContext().getStepExecution().getJobExecution();
        jobExecution.getExecutionContext().put(GROUPS_DATA_JOB_KEY, groupsList);
        log.info("Finished reading groups data from file");
        return RepeatStatus.FINISHED;
    }
}
