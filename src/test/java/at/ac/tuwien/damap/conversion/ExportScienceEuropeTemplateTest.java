package at.ac.tuwien.damap.conversion;

import at.ac.tuwien.damap.rest.dmp.domain.DmpDO;
import at.ac.tuwien.damap.util.TestDOFactory;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest

public class ExportScienceEuropeTemplateTest {

    @Inject
    ExportScienceEuropeTemplate exportScienceEuropeTemplate;

    @Inject
    TestDOFactory testDOFactory;

    @Test
    @TestSecurity(authorizationEnabled = false)
    void testEmptyFWFTemplateDmp() {
        final DmpDO emptyDmpDO = testDOFactory.getOrCreateTestDmpDOEmpty();

        //testing the export document return not a null document
        XWPFDocument document = null;
        try {
            document = exportScienceEuropeTemplate.exportTemplate(emptyDmpDO.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assertions.assertNotNull(document);
    }
}
