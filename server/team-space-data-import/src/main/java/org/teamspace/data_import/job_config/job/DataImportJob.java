package org.teamspace.data_import.job_config.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.teamspace.batch.InfrastructureConfigurationImpl;
import org.teamspace.data_import.job_config.tasks.DataReaderTasklet;
import org.teamspace.data_import.job_config.tasks.DataWriterTasklet;

/**
 * Created by shpilb on 08/09/2017.
 */
@Configuration
@Import(InfrastructureConfigurationImpl.class)
public class DataImportJob {
    @Autowired
    private JobBuilderFactory jobBuilders;

    @Autowired
    private StepBuilderFactory stepBuilders;

    @Autowired
    private InfrastructureConfigurationImpl infrastructureConfiguration;

    @Bean(name = "dataImport")
    public Job dataImportJob(){
        return jobBuilders.get("dataImportJob")
                .start(dataReaderStep())
                .next(dataWriterStep())
                .build();
    }

    @Bean
    public Step dataReaderStep(){
        return stepBuilders.get("dataReaderStep")
                .tasklet(dataReaderTasklet())
                .build();
    }

    @Bean
    public Step dataWriterStep(){
        return stepBuilders.get("dataWriterStep")
                .tasklet(dataWriterTasklet())
                .build();
    }

    @Bean
    public Tasklet dataReaderTasklet() {
        return new DataReaderTasklet();
    }

    @Bean
    public Tasklet dataWriterTasklet() {
        return new DataWriterTasklet();
    }
}
