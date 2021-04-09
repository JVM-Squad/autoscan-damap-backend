package at.ac.tuwien.conversion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import at.ac.tuwien.damap.domain.Dmp;
import at.ac.tuwien.damap.repo.DmpRepo;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DocumentConversionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentConversionService.class);

    @Inject
    DmpRepo dmpRepo;

    public XWPFDocument getFWFTemplate(long dmpId) throws IOException {

        Dmp dmp = dmpRepo.findById(dmpId);

        String template = "..\\src\\template\\template.docx";
        XWPFDocument document = new XWPFDocument(Files.newInputStream(Paths.get(template)));

        Map<String, String> map = new HashMap<String, String>();
        if (dmp.getProject() != null) {
            if (dmp.getProject().getTitle() != null)
                map.put("projectname", dmp.getProject().getTitle());
            if (dmp.getProject().getDescription() != null)
                map.put("projectacronym", dmp.getProject().getDescription());
            if (dmp.getProject().getStart() != null)
                map.put("startdate", dmp.getProject().getStart().toString());
            if (dmp.getProject().getEnd() != null)
                map.put("enddate", dmp.getProject().getEnd().toString());
        }
        if (dmp.getContact() != null) {
            if (dmp.getContact().getFirstName() != null && dmp.getContact().getFirstName() != null)
                map.put("projectconame", dmp.getContact().getFirstName() + " " + dmp.getContact().getLastName());
            if (dmp.getContact().getMbox() != null)
                map.put("projectcoemail", dmp.getContact().getMbox());
            if (dmp.getContact().getPersonIdentifier() != null)
                map.put("projectcoorcidId", dmp.getContact().getPersonIdentifier().getIdentifier());
        }

        List<XWPFParagraph> xwpfParagraphs = document.getParagraphs();
        replaceInParagraphs(xwpfParagraphs, map);

        List<XWPFTable> tables = document.getTables();
        for (XWPFTable xwpfTable : tables) {
            List<XWPFTableRow> tableRows = xwpfTable.getRows();
            for (XWPFTableRow xwpfTableRow : tableRows) {
                List<XWPFTableCell> tableCells = xwpfTableRow
                        .getTableCells();
                for (XWPFTableCell xwpfTableCell : tableCells) {
                    xwpfParagraphs = xwpfTableCell.getParagraphs();
                    replaceInParagraphs(xwpfParagraphs, map);
                }
            }
        }

        return document;
    }

    private void replaceInParagraphs(List<XWPFParagraph> xwpfParagraphs, Map<String, String> replacements) {
        for (XWPFParagraph xwpfParagraph : xwpfParagraphs) {
            List<XWPFRun> xwpfRuns = xwpfParagraph.getRuns();
            for (XWPFRun xwpfRun : xwpfRuns) {
                String xwpfRunText = xwpfRun.getText(xwpfRun
                        .getTextPosition());
                for (Map.Entry<String, String> entry : replacements
                        .entrySet()) {
                    if (xwpfRunText != null
                            && xwpfRunText.contains(entry.getKey())) {
                        xwpfRunText = xwpfRunText.replaceAll(
                                entry.getKey(), entry.getValue());
                    }
                }
                xwpfRun.setText(xwpfRunText, 0);
            }
        }
    }
}
