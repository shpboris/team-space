package org.teamspace.data_import.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by shpilb on 08/09/2017.
 */
@Data
@NoArgsConstructor
public class DataImportResult {
    Long jobInstanceId;
    private Long jobExecutionId;
    private List<String> runningSteps;
    private String status;
}
