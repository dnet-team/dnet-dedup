package eu.dnetlib.pace.tree;

import eu.dnetlib.pace.AbstractProtoPaceTest;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.tree.support.MatchType;
import eu.dnetlib.pace.util.BlockProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class TreeProcessingTest extends AbstractProtoPaceTest {

    private static final Log log = LogFactory.getLog(TreeProcessingTest.class);

    private DedupConfig config;

    @Before
    public void setup(){
        config = getAuthorsTestConf();
    }

    @Test
    public void testOrcidMatch (){

        final MapDocument authorA = author("id1", "1", "john", "smith", "smith, john", new Double[]{0.0,0.5,0.0,0.5}, "pubID1", "pubDOI1", 1, "0000-0000-0000-0000", Arrays.asList("coauthor1", "coauthor2", "coauthor3", "coauthor4", "coauthor5"));
        final MapDocument authorB = author("id2", "1", "john", "smith", "smith, john", new Double[]{0.0,0.5,0.0,0.5}, "pubID2", "pubDOI2", 1, "0000-0000-0000-0000", Arrays.asList("coauthor1", "coauthor2", "coauthor3", "coauthor4", "coauthor5"));
        final MapDocument authorC = author("id1", "1", "john", "smith", "smith, john", new Double[]{0.0,0.5,0.0,0.5}, "pubID1", "pubDOI1", 1, "0000-0000-0000-0001", Arrays.asList("coauthor1", "coauthor2", "coauthor3", "coauthor4", "coauthor5"));

        log.info("Author 1 = " + authorA);
        log.info("Author 2 = " + authorB);
        log.info("Author 3 = " + authorC);

        MatchType matchType1 = new BlockProcessor(config).navigateTree(authorA, authorB);
        MatchType matchType2 = new BlockProcessor(config).navigateTree(authorA, authorC);

        log.info("1 vs 2 Match Type = " + matchType1);
        log.info("1 vs 3 Match Type = " + matchType2);

        assertTrue(matchType1 == MatchType.ORCID_MATCH);
        assertTrue(matchType2 == MatchType.NO_MATCH);
    }

    @Test
    public void testCoauthorsMatch() {
        final MapDocument authorA = author("id1", "1", "john", "smith", "smith, john", new Double[]{0.0,0.5,0.0,0.5}, "pubID1", "pubDOI1", 1, "0000-0000-0000-0000", Arrays.asList("coauthor1", "coauthor2", "coauthor3", "coauthor4", "coauthor5", "coauthor6"));
        final MapDocument authorB = author("id2", "1", "john", "smith", "smith, john", new Double[]{0.0,0.5,0.0,0.5}, "pubID2", "pubDOI2", 1, "", Arrays.asList("coauthor1", "coauthor2", "coauthor3", "coauthor4", "coauthor5", "coauthor6"));

        log.info("Author 1 = " + authorA);
        log.info("Author 2 = " + authorB);

        MatchType matchType = new BlockProcessor(config).navigateTree(authorA, authorB);

        log.info("Match Type = " + matchType);

        assertTrue(matchType == MatchType.COAUTHORS_MATCH);
    }

    @Test
    public void testTopicsMatch() {
        final MapDocument authorA = author("id1", "1", "john", "smith", "smith, john", new Double[]{0.0,0.5,0.0,0.5}, "pubID1", "pubDOI1", 1, "0000-0000-0000-0000", Arrays.asList("coauthor1", "coauthor2", "coauthor3", "coauthor4", "coauthor5", "coauthor6"));
        final MapDocument authorB = author("id2", "1", "john", "smith", "smith, john", new Double[]{0.0,0.5,0.0,0.5}, "pubID2", "pubDOI2", 1, "", Arrays.asList("coauthor1", "coauthor2", "coauthor3", "coauthor4", "coauthor5"));

        log.info("Author 1 = " + authorA);
        log.info("Author 2 = " + authorB);

        MatchType matchType = new BlockProcessor(config).navigateTree(authorA, authorB);

        log.info("Match Type = " + matchType);

        assertTrue(matchType == MatchType.TOPICS_MATCH);
    }

    @Test
    public void testNoMatch() {

        final MapDocument authorA = author("id1", "1", "john", "smith", "smith, john", new Double[]{0.0,0.5,0.0,0.5}, "pubID1", "pubDOI1", 1, "0000-0000-0000-0000", Arrays.asList("coauthor1", "coauthor2", "coauthor3", "coauthor4", "coauthor5", "coauthor6"));
        final MapDocument authorB = author("id1", "1", "john", "smith", "smith, john", new Double[]{0.0,0.5,0.0,0.5}, "pubID1", "pubDOI1", 1, "0000-0000-0000-0000", Arrays.asList("coauthor1", "coauthor2", "coauthor3", "coauthor4", "coauthor5", "coauthor6"));
        final MapDocument authorC = author("id2", "1", "jesus f.", "smith", "smith, john", new Double[]{0.0,0.5,0.0,0.5}, "pubID1", "pubDOI1", 1, "", Arrays.asList("coauthor1", "coauthor2", "coauthor3", "coauthor4", "coauthor5", "coauthor6"));

        MatchType matchType1 = new BlockProcessor(config).navigateTree(authorA,authorB);
        MatchType matchType2 = new BlockProcessor(config).navigateTree(authorA,authorC);

        assertTrue(matchType1 == MatchType.NO_MATCH); //same identifier
        assertTrue(matchType2 == MatchType.NO_MATCH); //not similar firstname
    }

}
