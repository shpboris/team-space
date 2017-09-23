package org.teamspace.data_import.job_config.deciders;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.teamspace.data_import.domain.DataImportRequest;

import static org.teamspace.data_import.constants.DataImportConstants.CUSTOM_PARAMETERS_JOB_KEY;
import static org.teamspace.data_import.constants.DataImportConstants.EXECUTE_REPORT_COMPLETION;
import static org.teamspace.data_import.constants.DataImportConstants.SKIP_REPORT_COMPLETION;

/**
 * Created by shpilb on 23/09/2017.
 */
public class ReportCompletionExecutionDecider implements JobExecutionDecider {
    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        DataImportRequest dataImportRequest = (DataImportRequest)jobExecution
                .getExecutionContext().get(CUSTOM_PARAMETERS_JOB_KEY);
        FlowExecutionStatus flowExecutionStatus = null;
        if(dataImportRequest.isShouldReportCompletion()){
            flowExecutionStatus = new FlowExecutionStatus(EXECUTE_REPORT_COMPLETION);
        } else {
            flowExecutionStatus = new FlowExecutionStatus(SKIP_REPORT_COMPLETION);
        }
        return flowExecutionStatus;
    }
}
