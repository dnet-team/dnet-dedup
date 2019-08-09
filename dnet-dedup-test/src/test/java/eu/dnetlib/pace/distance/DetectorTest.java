//package eu.dnetlib.pace.distance;
//
//import com.google.common.collect.Lists;
//import com.google.common.collect.Sets;
//import com.googlecode.protobuf.format.JsonFormat;
//import eu.dnetlib.data.proto.OafProtos;
//import eu.dnetlib.pace.AbstractProtoPaceTest;
//import eu.dnetlib.pace.clustering.BlacklistAwareClusteringCombiner;
//import eu.dnetlib.pace.config.Config;
//import eu.dnetlib.pace.config.DedupConfig;
//import eu.dnetlib.pace.distance.eval.ScoreResult;
//import eu.dnetlib.pace.model.MapDocument;
//import eu.dnetlib.pace.model.ProtoDocumentBuilder;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.junit.Ignore;
//import org.junit.Test;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Set;
//
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//
//public class DetectorTest extends AbstractProtoPaceTest {
//
//    private static final Log log = LogFactory.getLog(DetectorTest.class);
//
//    @Test
//    public void testDistanceResultSimple() {
//        final Config config = getResultSimpleConf();
//        final MapDocument resA = result(config, "A", "Recent results from CDF");
//        final MapDocument resB = result(config, "B", "Recent results from CDF");
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        final double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//        assertTrue(d == 1.0);
//    }
//
//    @Test
//    public void testDistanceResultSimpleMissingDates() {
//        final Config config = getResultSimpleConf();
//        final MapDocument resA = result(config, "A", "Recent results from BES");
//        final MapDocument resB = result(config, "A", "Recent results from CES");
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        final double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//        assertTrue(d > 0.97);
//    }
//
//    @Test
//    public void testDistanceResultInvalidDate() {
//        final Config config = getResultConf();
//        final MapDocument resA = result(config, "A", "title title title 6BESR", "2013-01-05");
//        final MapDocument resB = result(config, "B", "title title title 6BESR", "qwerty");
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        final double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//        assertTrue(d == 1.0);
//    }
//
//    @Ignore
//    @Test
//    public void testDistanceResultMissingOneDate() {
//        final Config config = getResultConf();
//        final MapDocument resA = result(config, "A", "title title title 6BESR", null);
//        final MapDocument resB = result(config, "B", "title title title 6CLER", "2012-02");
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//        assertTrue((d > 0.9) && (d < 1.0));
//    }
//
//    @Ignore
//    @Test
//    public void testDistanceResult() {
//        final Config config = getResultConf();
//        final MapDocument resA = result(config, "A", "title title title BES", "");
//        final MapDocument resB = result(config, "B", "title title title CLEO");
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//        assertTrue((d > 0.9) && (d < 1.0));
//    }
//
//    @Ignore
//    @Test
//    public void testDistanceResultMissingTwoDate() {
//        final Config config = getResultConf();
//        final MapDocument resA = result(config, "A", "bellaciao");
//        final MapDocument resB = result(config, "B", "bellocioa");
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//        assertTrue((d > 0.9) && (d < 1.0));
//    }
//
//    @Ignore
//    @Test
//    public void testDistanceOrganizationIgnoreMissing() {
//        final Config config = getOrganizationSimpleConf();
//        final MapDocument orgA = organization(config, "A", "CONSIGLIO NAZIONALE DELLE RICERCHE");
//        final MapDocument orgB = organization(config, "B", "CONSIGLIO NAZIONALE DELLE RICERCHE", "CNR");
//        final ScoreResult sr = new PaceDocumentDistance().between(orgA, orgB, config);
//        final double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//        assertTrue(d > 0.99);
//    }
//
//
//    @Test
//    public void testDistanceOrganizations() {
//        final Config config = getOrganizationTestConf();
//        final MapDocument orgA = organization(config, "A", "UNIVERSITA DEGLI STUDI DI VERONA");
//        final MapDocument orgB = organization(config, "B", "UNIVERSITY OF GENOVA");
//        final ScoreResult sr = new PaceDocumentDistance().between(orgA, orgB, config);
//        final double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//    }
//
//
//    @Test
//    public void testDistanceResultCase1() {
//        final Config config = getResultConf();
//        final MapDocument resA = result(config, "A", "Search the Standard Model Higgs boson", "2003");
//        final MapDocument resB = result(config, "B", "Search for the Standard Model Higgs Boson", "2003");
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//        System.out.println("d = " + d);
//        assertTrue((d >= 0.9) && (d <= 1.0));
//    }
//
//    @Test
//    public void testDistanceResultCaseDoiMatch1() {
//        final Config config = getResultConf();
//        final MapDocument resA = result(config, "A", "Search the Standard Model Higgs boson", "2003", "10.1594/PANGAEA.726855");
//        final MapDocument resB = result(config, "B", "Search the Standard Model Higgs Boson", "2003", "10.1594/PANGAEA.726855");
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//        assertTrue("exact DOIs will produce an exact match", d == 1.0);
//    }
//
//    @Test
//    public void testDistanceResultCaseDoiMatch2() {
//        final Config config = getResultConf();
//        final MapDocument resA = result(config, "A", "Conference proceedings on X. Appendix", "2003", "10.1594/PANGAEA.726855");
//        final MapDocument resB = result(config, "B", "Search the Standard Model Higgs Boson", "2005", "10.1594/PANGAEA.726855");
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//        assertTrue("exact DOIs will produce an exact match, regardless of different titles or publication years", d == 1.0);
//    }
//
//    @Test
//    public void testDistanceResultCaseDoiMatch3() {
//        final Config config = getResultConf();
//        final MapDocument resA = result(config, "A", "Conference proceedings on X. Appendix", "2003", "10.1016/j.jmb.2010.12.024");
//        final MapDocument resB = result(config, "B", "Conference proceedings on X. Appendix", "2003");
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//        assertTrue("a missing DOI will casue the comparsion to continue with the following necessaryConditions", d == 1.0);
//    }
//
//    @Test
//    public void testDistanceResultCaseDoiMatch4() {
//        final Config config = getResultConf();
//        final MapDocument resA = result(config, "A", "Conference proceedings on X. Appendix", "2003", "10.1016/j.jmb.2010.12.024");
//        final MapDocument resB = result(config, "B", "Conference proceedings on X. Appendix", "2005");
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//        assertTrue("a missing DOI, comparsion continues with the following necessaryConditions, different publication years will drop the score to 0", d == 0.0);
//    }
//
//    @Test
//    public void testDistanceResultCaseDoiMatch5() {
//        final Config config = getResultConf();
//        final MapDocument resA = result(config, "A", "Search for the Standard Model Higgs Boson", "2003", "10.1016/j.jmb.2010.12.020");
//        final MapDocument resB = result(config, "B", "Search the Standard Model Higgs Boson", "2003");
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//        assertTrue("a missing DOI, comparsion continues with the following necessaryConditions", (d > 0.9) && (d < 1.0));
//    }
//
//    @Test
//    public void testDistanceResultCaseDoiMatch6() {
//        final Config config = getResultConf();
//        final MapDocument resA = result(config, "A", "Conference proceedings on X. Appendix", "2003", "10.1016/j.jmb.2010.12.024");
//        final MapDocument resB = result(config, "B", "Conference proceedings on X. Appendix", "2003", "anotherDifferentDOI");
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//        assertTrue("different DOIs will NOT drop the score to 0, then evaluate other fields", d == 1.0);
//    }
//
//    @Test
//    public void testDistanceResultCaseDoiMatch7() {
//        final Config config = getResultConf();
//        final MapDocument resA = result(config, "A", "Adrenal Insufficiency asd asd", "1951", Lists.newArrayList("PMC2037944", "axdsds"));
//        final MapDocument resB = result(config, "B", "Adrenal Insufficiency", "1951", "PMC2037944");
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//        assertTrue("different DOIs will drop the score to 0, regardless of the other fields", d > 0.89 & d < 1);
//    }
//
//    // http://dx.doi.org/10.1594/PANGAEA.726855 doi:10.1594/PANGAEA.726855
//    @Test
//    public void testDistanceResultCaseAuthor1() {
//        final Config config = getResultAuthorsConf();
//        final List<String> authorsA = Lists.newArrayList("a", "b", "c", "d");
//        final List<String> authorsB = Lists.newArrayList("a", "b", "c");
//        final List<String> pid = Lists.newArrayList();
//        final MapDocument resA = result(config, "A", "Search the Standard Model Higgs Boson", "2003", pid, authorsA);
//        final MapDocument resB = result(config, "B", "Search the Standard Model Higgs Boson", "2003", pid, authorsB);
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        final double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//        assertTrue(d == 0.0);
//    }
//
//    @Test
//    public void testDistanceResultCaseAuthor2() {
//        final Config config = getResultAuthorsConf();
//        final List<String> authorsA = Lists.newArrayList("a", "b", "c");
//        final List<String> authorsB = Lists.newArrayList("a", "b", "c");
//        final List<String> pid = Lists.newArrayList();
//        final MapDocument resA = result(config, "A", "Search the Standard Model Higgs Boson", "2003", pid, authorsA);
//        final MapDocument resB = result(config, "B", "Search the Standard Model Higgs Boson", "2003", pid, authorsB);
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        final double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//        assertTrue(d == 1.0);
//    }
//
//    @Test
//    public void testDistanceResultCaseAuthor3() {
//        final Config config = getResultAuthorsConf();
//        final List<String> authorsA = Lists.newArrayList("Bardi, A.", "Manghi, P.", "Artini, M.");
//        final List<String> authorsB = Lists.newArrayList("Bardi Alessia", "Manghi Paolo", "Artini Michele");
//        final List<String> pid = Lists.newArrayList();
//        final MapDocument resA = result(config, "A", "Search the Standard Model Higgs Boson", "2003", pid, authorsA);
//        final MapDocument resB = result(config, "B", "Search the Standard Model Higgs Boson", "2003", pid, authorsB);
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//        assertTrue((d > 0.9) && (d < 1.0));
//    }
//
//    @Test
//    public void testDistanceResultCaseAuthor4() {
//        final Config config = getResultAuthorsConf();
//        final List<String> authorsA = Lists.newArrayList("Bardi, Alessia", "Manghi, Paolo", "Artini, Michele", "a");
//        final List<String> authorsB = Lists.newArrayList("Bardi Alessia", "Manghi Paolo", "Artini Michele");
//        final List<String> pid = Lists.newArrayList();
//        final MapDocument resA = result(config, "A", "Search the Standard Model Higgs Boson", "2003", pid, authorsA);
//        final MapDocument resB = result(config, "B", "Search the Standard Model Higgs Boson", "2003", pid, authorsB);
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        final double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//        // assertTrue(d.getScore() == 0.0);
//    }
//
//    @Test
//    public void testDistanceResultNoPidsConf() {
//        final Config config = getResultFullConf();
//        final MapDocument resA =
//                result(config, "A", "Presentations of perforated colonic pathology in patients with polymyalgia rheumatica: two case reports", "2010");
//        final MapDocument resB =
//                result(config, "B", "Presentations of perforated colonic pathology in patients with polymyalgia rheumatica: two case reportsX", "2010");
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        final double s = sr.getScore();
//        log.info(sr.toString());
//        log.info(String.format(" s ---> %s", s));
//        // assertTrue(d.getScore() == 0.0);
//    }
//
//    @Test
//    public void testDistanceResultPidsConf() {
//        final Config config = getResultFullConf();
//        final List<String> authorsA = Lists.newArrayList("Nagarajan Pranesh", "Guy Vautier", "Punyanganie de Silva");
//        final List<String> authorsB = Lists.newArrayList("Pranesh Nagarajan", "Vautier Guy", "de Silva Punyanganie");
//        final List<String> pidA = Lists.newArrayList("10.1186/1752-1947-4-299", "a", "b");
//        final MapDocument resA =
//                result(config, "A", "Presentations of perforated colonic pathology in patients with polymyalgia rheumatica: two case reports", "2010",
//                        pidA, authorsA);
//        final List<String> pidB = Lists.newArrayList("c", "a", "10.1186/1752-1947-4-299", "d");
//        final MapDocument resB =
//                result(config, "B", "Presentations of perforated colonic pathology in patients with polymyalgia rheumatica: two case reportsX", "2010",
//                        pidB, authorsB);
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        final double s = sr.getScore();
//        log.info(sr.toString());
//        log.info(String.format(" s ---> %s", s));
//        // assertTrue(d.getScore() == 0.0);
//    }
//
//    @Test
//    public void testDistanceResultFullConf() {
//        final Config config = getResultFullConf();
//        final List<String> authorsA = Lists.newArrayList("Nagarajan Pranesh", "Guy Vautier", "Punyanganie de Silva");
//        final List<String> authorsB = Lists.newArrayList("Pranesh Nagarajan", "Vautier Guy", "de Silva Punyanganie");
//        final MapDocument resA =
//                result(config, "A", "Presentations of perforated colonic pathology in patients with polymyalgia rheumatica: two case reports", "2010",
//                        "10.1186/1752-1947-4-299", authorsA);
//        final MapDocument resB =
//                result(config, "B", "Presentations of perforated colonic pathology in patients with polymyalgia rheumatica: two case reports", "2010",
//                        "10.1186/1752-1947-4-299", authorsB);
//        final ScoreResult sr = new PaceDocumentDistance().between(resA, resB, config);
//        final double d = sr.getScore();
//        log.info(String.format(" d ---> %s", d));
//        // assertTrue(d.getScore() == 0.0);
//    }
//
//    @Ignore
//    @Test
//    public void testDistance() throws IOException {
//        final DedupConfig conf = DedupConfig.load(readFromClasspath("/eu/dnetlib/pace/result.prod.pace.json"));
//        final MapDocument crossref = asMapDocument(conf, "/eu/dnetlib/pace/crossref.json");
//        final MapDocument alicante = asMapDocument(conf, "/eu/dnetlib/pace/alicante.json");
//        final ScoreResult result = new PaceDocumentDistance().between(crossref, alicante, conf);
//        log.info("score = " + result);
//    }
//
//    @Ignore
//    @Test
//    public void testDistanceOrgs() throws IOException {
//        final DedupConfig conf = DedupConfig.load(readFromClasspath("/eu/dnetlib/pace/organization.pace.conf"));
//        final MapDocument orgA = asMapDocument(conf, readFromClasspath("/eu/dnetlib/pace/organization1.json"));
//        final MapDocument orgB = asMapDocument(conf, readFromClasspath("/eu/dnetlib/pace/organization2.json"));
//        Set<String> keysA = getGroupingKeys(conf, orgA);
//        Set<String> keysB = getGroupingKeys(conf, orgB);
//        assertFalse(String.format("A: %s\nB: %s", keysA, keysB), Sets.intersection(keysA, keysB).isEmpty());
//        log.info("clustering keys A = " + getGroupingKeys(conf, orgA));
//        log.info("clustering keys B = " + getGroupingKeys(conf, orgB));
//        final ScoreResult result = new PaceDocumentDistance().between(orgA, orgB, conf);
//        log.info("score = " + result);
//        log.info("compare = " + result.getScore());
//    }
//
//    private Set<String> getGroupingKeys(DedupConfig conf, MapDocument doc) {
//        return Sets.newHashSet(BlacklistAwareClusteringCombiner.filterAndCombine(doc, conf));
//    }
//
//    private MapDocument asMapDocument(DedupConfig conf, final String json) {
//        OafProtos.OafEntity.Builder b = OafProtos.OafEntity.newBuilder();
//        try {
//            JsonFormat.merge(json, b);
//        } catch (JsonFormat.ParseException e) {
//            throw new IllegalArgumentException(e);
//        }
//        return ProtoDocumentBuilder.newInstance(b.getId(), b.build(), conf.getPace().getModel());
//
//    }
//}
