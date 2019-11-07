package eu.dnetlib.pace.model;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import eu.dnetlib.pace.AbstractProtoPaceTest;
import eu.dnetlib.pace.config.Config;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProtoDocumentBuilderTest extends AbstractProtoPaceTest {

	private static final Log log = LogFactory.getLog(ProtoDocumentBuilderTest.class);

	@Test
	public void test_serialise1() {

		final String id = "12345";

		final Config config = getOrganizationTestConf();

		final MapDocument document = ProtoDocumentBuilder.newInstance(id, getOrganization(id), config.model());

		assertFalse(document.fieldNames().isEmpty());
		assertFalse(Iterables.isEmpty(document.fields()));

		log.info("original:\n" + document);

		final String stringDoc = MapDocumentSerializer.toString(document);

		log.info("serialization:\n" + stringDoc);

		final MapDocument decoded = MapDocumentSerializer.decode(stringDoc.getBytes());

		final SetView<String> diff = Sets.difference(document.fieldNames(), decoded.fieldNames());

		assertTrue(diff.isEmpty());

		log.info("decoded:\n" + decoded);
	}

}
