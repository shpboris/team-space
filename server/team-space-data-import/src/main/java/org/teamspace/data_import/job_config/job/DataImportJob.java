package org.teamspace.data_import.job_config.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.teamspace.batch.InfrastructureConfigurationImpl;
import org.teamspace.data_import.job_config.listener.DataImportJobListener;
import org.teamspace.data_import.job_config.tasks.*;

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


    @Bean(name = "dataImport")
    public Job dataImportJob(){
        return jobBuilders.get("dataImportJob")
                .listener(dataImportJobListener())
                .start(dataReaderStep())
                .next(dataWriterStep())
                .build();
    }

    @Bean
    public Flow dataReaderFlow(){
        Flow dataReaderSplitFlow = new FlowBuilder<Flow>("dataReaderFlow")
                .split(new SimpleAsyncTaskExecutor())
                .add(usersDataReaderFlow(), groupsDataReaderFlow(), membershipsDataReaderFlow()).build();
        return dataReaderSplitFlow;
    }

    @Bean
    public Flow usersDataReaderFlow(){
        Flow usersDataReaderFlow = new FlowBuilder<Flow>("userDataReaderFlow").from(usersDataReaderStep()).end();
        return usersDataReaderFlow;
    }

    @Bean
    public Flow groupsDataReaderFlow(){
        Flow groupsDataReaderFlow = new FlowBuilder<Flow>("groupsDataReaderFlow").from(groupsDataReaderStep()).end();
        return groupsDataReaderFlow;
    }

    @Bean
    public Flow membershipsDataReaderFlow(){
        Flow membershipsDataReaderFlow = new FlowBuilder<Flow>("membershipsDataReaderFlow").from(membershipsDataReaderStep()).end();
        return membershipsDataReaderFlow;
    }

    @Bean
    public Step dataReaderStep(){
        return stepBuilders.get("dataReaderStep")
                .flow(dataReaderFlow())
                .build();
    }

    @Bean
    public Step usersDataReaderStep(){
        return stepBuilders.get("usersDataReaderStep")
                .tasklet(usersDataReaderTasklet())
                .build();
    }

    @Bean
    public Step groupsDataReaderStep(){
        return stepBuilders.get("groupsDataReaderStep")
                .tasklet(groupsDataReaderTasklet())
                .build();
    }

    @Bean
    public Step membershipsDataReaderStep(){
        return stepBuilders.get("membershipsDataReaderStep")
                .tasklet(membershipDataReaderTasklet())
                .build();
    }

    @Bean
    public Step dataWriterStep(){
        return stepBuilders.get("dataWriterStep")
                .tasklet(dataWriterTasklet())
                .build();
    }

    @Bean
    public Tasklet usersDataReaderTasklet() {
        return new UsersDataReaderTasklet();
    }

    @Bean
    public Tasklet groupsDataReaderTasklet() {
        return new GroupsDataReaderTasklet();
    }

    @Bean
    public Tasklet membershipDataReaderTasklet() {
        return new MembershipDataReaderTasklet();
    }

    @Bean
    public Tasklet dataWriterTasklet() {
        return new DataWriterTasklet();
    }

    @Bean
    public DataImportJobListener dataImportJobListener(){
        return new DataImportJobListener();
    }

}
