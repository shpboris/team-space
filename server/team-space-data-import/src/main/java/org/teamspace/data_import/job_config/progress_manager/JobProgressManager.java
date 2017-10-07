package org.teamspace.data_import.job_config.progress_manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by shpilb on 06/10/2017.
 */
@Component
@Slf4j
public class JobProgressManager {
    private Map<String, CyclicBarrier> jobParameterToBarrierMap = new ConcurrentHashMap<>();

    public synchronized void registerJob(String jobKey){
        jobParameterToBarrierMap.put(jobKey, new CyclicBarrier(10));
    }

    public synchronized void unregisterJob(String jobKey){
        jobParameterToBarrierMap.remove(jobKey);
    }

    public void awaitProgressPermission(String jobKey){
        CyclicBarrier cyclicBarrier = getJobBarrier(jobKey);
        try {
            cyclicBarrier.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to wait for progress permission");
        } catch (BrokenBarrierException e) {
            log.debug("Allowed progress for job: {}", jobKey);
        }
    }

    public void provideProgressPermission(String jobKey){
        CyclicBarrier cyclicBarrier = getJobBarrier(jobKey);
        cyclicBarrier.reset();
    }

    private synchronized CyclicBarrier getJobBarrier(String jobKey){
        return jobParameterToBarrierMap.get(jobKey);
    }
}
