package org.teamspace.data_import.job_config.tasks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.teamspace.auth.domain.User;
import org.teamspace.data_import.domain.ReportCompletionSummary;
import org.teamspace.data_import.service.DatabaseImportService;
import org.teamspace.groups.domain.Group;
import org.teamspace.membership.domain.Membership;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.teamspace.data_import.constants.DataImportConstants.*;

/**
 * Created by shpilb on 08/09/2017.
 */
@Slf4j
public class ReportCompletionTasklet implements Tasklet {

    @Value("${dataImportDir}")
    private String dataImportDir;

    @Autowired
    private DatabaseImportService databaseImportService;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        log.info("Started report completion");
        JobExecution jobExecution = chunkContext.getStepContext().getStepExecution().getJobExecution();
        Instant now = Instant.now();
        Long jobId = jobExecution.getId();
        List<User> users = (List<User>)jobExecution.getExecutionContext().get(USERS_DATA_JOB_KEY);
        List<Group> groups = (List<Group>)jobExecution.getExecutionContext().get(GROUPS_DATA_JOB_KEY);
        List<Membership> memberships = (List<Membership>)jobExecution.getExecutionContext().get(MEMBERSHIP_DATA_JOB_KEY);
        logReportData(now, jobId, users, groups, memberships);
        log.info("Finished report completion");
        return RepeatStatus.FINISHED;
    }

    private synchronized void logReportData(Instant now, Long jobId, List<User> users, List<Group> groups, List<Membership> memberships) throws Exception{
        ReportCompletionSummary reportCompletionSummary = new ReportCompletionSummary(jobId, now.toString(), users, groups, memberships);
        List<ReportCompletionSummary> reportCompletionSummaryList = new ArrayList<>();
        Path importReportDataFilePath = Paths.get(dataImportDir, IMPORT_REPORT_FILE_NAME);
        String importReportDataStr = null;
        ObjectMapper mapper = new ObjectMapper();
        if(Files.exists(importReportDataFilePath)) {
            importReportDataStr = new String(Files.readAllBytes(importReportDataFilePath), StandardCharsets.UTF_8);
            reportCompletionSummaryList = mapper.readValue(importReportDataStr, new TypeReference<List<ReportCompletionSummary>>() {});
        } else {
            Files.createFile(importReportDataFilePath);
        }
        reportCompletionSummaryList.add(reportCompletionSummary);
        importReportDataStr = mapper.writeValueAsString(reportCompletionSummaryList);
        Files.write(importReportDataFilePath, importReportDataStr.getBytes(StandardCharsets.UTF_8));
    }

}
