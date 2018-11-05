package de.redsix.pdfcompare;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import de.redsix.junitextensions.TempDirectory;
import de.redsix.junitextensions.TempDirectoryExtension;

@ExtendWith(TempDirectoryExtension.class)
public class IntegrationTest {

    private String testName;
    private Path outDir;

    @BeforeEach
    public void before(TestInfo testInfo, @TempDirectory(parentPath = ".") Path outDir) throws IOException, InterruptedException {
        testName = testInfo.getTestMethod().get().getName();
        this.outDir = outDir;
    }

    @Test
    public void differingDocumentsAreNotEqual() throws IOException {
        final CompareResult result = new PdfComparator(r("expected.pdf"), r("actual.pdf")).compare();
        assertThat(result.isNotEqual(), is(true));
        assertThat(result.isEqual(), is(false));
        assertThat(result.hasOnlyExpected(), is(false));
        assertThat(result.hasOnlyOneDoc(), is(false));
        assertThat(result.hasOnlyActual(), is(false));
        writeAndCompare(result);
    }

    @Test
    public void differingDocumentsAreNotEqualUsingPageOverflow() throws IOException {
        final CompareResult result = new PdfComparator(f("expected.pdf"), f("actual.pdf"), new CompareResultWithPageOverflow()).compare();
        assertThat(result.isNotEqual(), is(true));
        assertThat(result.isEqual(), is(false));
        writeAndCompare(result);
    }

    @Test
    public void differingDocumentsAreNotEqualUsingPageOverflowWithOverflow() throws IOException {
        final CompareResult result = new PdfComparator(p("expected.pdf"), p("actual.pdf"), new CompareResultWithPageOverflow(1)).compare();
        assertThat(result.isNotEqual(), is(true));
        assertThat(result.isEqual(), is(false));
        writeAndCompare(result);
    }

    @Test
    public void differingDocumentsAreNotEqualUsingMemoryOverflow() throws IOException {
        final CompareResult result = new PdfComparator(r("expected.pdf"), r("actual.pdf"), new CompareResultWithMemoryOverflow()).compare();
        assertThat(result.isNotEqual(), is(true));
        assertThat(result.isEqual(), is(false));
        writeAndCompare(result);
    }

    @Test
    public void differingDocumentsWithIgnoreAreEqual() throws IOException {
        final CompareResult result = new PdfComparator(r("expected.pdf"), r("actual.pdf")).withIgnore("ignore.conf").compare();
        assertThat(result.isEqual(), is(true));
        assertThat(result.isNotEqual(), is(false));
        assertThat(result.hasDifferenceInExclusion(), is(true));
        writeAndCompare(result);
    }

    @Test
    public void differingDocumentsWithFullPageIgnoreAreEqual() throws IOException {
        final ByteArrayInputStream ignoreIS = new ByteArrayInputStream("exclusions: [{page:1}, {page:2}]".getBytes());
        final CompareResult result = new PdfComparator(r("expected.pdf"), r("actual.pdf")).withIgnore(ignoreIS).compare();
        assertThat(result.isEqual(), is(true));
        assertThat(result.hasDifferenceInExclusion(), is(true));
        writeAndCompare(result);
    }

    @Test
    public void exclusionsCanBeAddedViaAPI() throws IOException {
        final CompareResult result = new PdfComparator(r("expected.pdf"), r("actual.pdf"))
                .with(new Exclusion(1, 230, 350, 450, 420))
                .with(new Exclusion(2, 1750, 240, 2000, 300))
                .compare();
        assertThat(result.isEqual(), is(true));
        assertThat(result.hasDifferenceInExclusion(), is(true));
        writeAndCompare(result);
    }

    @Test
    public void aShorterDocumentActualIsNotEqual() throws IOException {
        final CompareResult result = new PdfComparator(r("expected.pdf"), r("short.pdf")).compare();
        assertThat(result.isNotEqual(), is(true));
        assertThat(result.isEqual(), is(false));
        writeAndCompare(result);
    }

    @Test
    public void aShorterDocumentExpectedIsNotEqual() throws IOException {
        final CompareResult result = new PdfComparator(r("short.pdf"), r("actual.pdf")).compare();
        assertThat(result.isNotEqual(), is(true));
        assertThat(result.isEqual(), is(false));
        writeAndCompare(result);
    }

    @Test
    public void missingActualIsNotEqual() throws IOException {
        final Path target = outDir.resolve("expected.pdf");
        Files.copy(r("expected.pdf"), target);
        final CompareResult result = new PdfComparator(target.toString(), "doesNotExist.pdf").compare();
        assertThat(result.isNotEqual(), is(true));
        assertThat(result.isEqual(), is(false));
        assertThat(result.hasOnlyExpected(), is(true));
        assertThat(result.hasOnlyOneDoc(), is(true));
        assertThat(result.hasOnlyActual(), is(false));
        writeAndCompare(result);
    }

    @Test
    public void missingExpectedIsNotEqual() throws IOException {
        final Path target = outDir.resolve("actual.pdf");
        Files.copy(r("actual.pdf"), target);
        final CompareResult result = new PdfComparator("doesNotExist.pdf", target.toString()).compare();
        assertThat(result.isNotEqual(), is(true));
        assertThat(result.isEqual(), is(false));
        assertThat(result.hasOnlyExpected(), is(false));
        assertThat(result.hasOnlyOneDoc(), is(true));
        assertThat(result.hasOnlyActual(), is(true));
        writeAndCompare(result);
    }

    @Test
    public void bothFilesMissingIsNotEqual() throws IOException {
        final CompareResult result = new PdfComparator("doesNotExist.pdf", "doesNotExistAsWell.pdf").compare();
        assertThat(result.isNotEqual(), is(true));
        assertThat(result.isEqual(), is(false));
        writeAndCompare(result);
    }

    @Test
    public void identicalFilenamesAreEqual() throws IOException {
        final CompareResult result = new PdfComparator("whatever.pdf", "whatever.pdf").compare();
        assertThat(result.isEqual(), is(true));
        assertThat(result.isNotEqual(), is(false));
        writeAndCompare(result);
    }

    private InputStream r(final String s) {
        return getClass().getResourceAsStream(s);
    }

    private File f(final String s) {
        return new File(getClass().getResource(s).getFile());
    }

    private Path p(final String s) {
        try {
            return Paths.get(getClass().getResource(s).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeAndCompare(final CompareResult result) throws IOException {
        if (System.getenv().get("pdfCompareInTest") != null || System.getProperty("pdfCompareInTest") != null) {
            final String filename = outDir.resolve(testName).toString();
            result.writeTo(filename);
            try (final InputStream expectedPdf = getClass().getResourceAsStream(testName + ".pdf")) {
                if (expectedPdf != null) {
                    assertTrue(new PdfComparator(expectedPdf, new FileInputStream(filename + ".pdf")).compare().isEqual());
                } else {
                    assertFalse(Files.exists(Paths.get(filename + ".pdf")));
                }
            }
        }
    }
}
