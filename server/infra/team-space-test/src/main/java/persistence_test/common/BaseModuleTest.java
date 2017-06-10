package persistence_test.common;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.teamspace.persistence.common.schema.SchemaCreator;

/**
 * Created by shpilb on 10/06/2017.
 */
public class BaseModuleTest {

    @Autowired
    private SchemaCreator schemaCreator;

    @Before
    public void setup() {
        schemaCreator.createSchema();
    }

    @After
    public void tearDown() {
    }
}
