package org.teamspace.data_import.domain;

import lombok.Data;

/**
 * Created by shpilb on 08/09/2017.
 */
@Data
public class DataImportRequest {
    private boolean shouldPerformCleanup;
    private Long jobId;
}
