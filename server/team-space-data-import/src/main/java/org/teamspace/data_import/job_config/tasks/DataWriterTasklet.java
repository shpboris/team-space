package org.teamspace.data_import.job_config.tasks;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * Created by shpilb on 08/09/2017.
 */
public class DataWriterTasklet implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        System.out.println("Finished Data Writer !!!!!!!!!!!!!");
        return RepeatStatus.FINISHED;
    }
}
