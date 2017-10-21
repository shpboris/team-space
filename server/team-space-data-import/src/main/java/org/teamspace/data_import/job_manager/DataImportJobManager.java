package org.teamspace.data_import.job_manager;

import org.teamspace.data_import.domain.DataImportRequest;
import org.teamspace.data_import.domain.DataImportResult;

import java.util.List;

/**
 * Created by shpilb on 21/10/2017.
 */
public interface DataImportJobManager {
    DataImportResult create(DataImportRequest dataImportRequest);
    List<DataImportResult> findAll();
    DataImportResult stop(Long executionId);
    DataImportResult restart(Long executionId);
    DataImportResult allowProgress(Long executionId);
}
