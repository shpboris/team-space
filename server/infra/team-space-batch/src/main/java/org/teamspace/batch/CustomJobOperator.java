package org.teamspace.batch;

import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by shpilb on 16/09/2017.
 */
public class CustomJobOperator extends SimpleJobOperator {
    @Transactional("transactionManager")
    public boolean stop(long executionId) throws NoSuchJobExecutionException, JobExecutionNotRunningException {
        return  super.stop(executionId);
    }
}
