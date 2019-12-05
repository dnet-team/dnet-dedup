package eu.dnetlib.pace.config;

import com.arakelian.jq.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dnetlib.pace.AbstractPaceTest;
import eu.dnetlib.pace.model.FieldList;
import eu.dnetlib.pace.model.FieldListImpl;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.util.MapDocumentUtil;
import org.apache.commons.io.IOUtils;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigTest extends AbstractPaceTest {

	@Test
	public void dedupConfigSerializationTest() {
		final DedupConfig cfgFromClasspath = DedupConfig.load(readFromClasspath("org.curr.conf"));

		final String conf = cfgFromClasspath.toString();

//		System.out.println("*****SERIALIZED*****");
//		System.out.println(conf);
//		System.out.println("*****FROM CLASSPATH*****");
//		System.out.println(readFromClasspath("result.pace.conf.json"));

		final DedupConfig cfgFromSerialization = DedupConfig.load(conf);

		assertEquals(cfgFromClasspath.toString(), cfgFromSerialization.toString());

		assertNotNull(cfgFromClasspath);
		assertNotNull(cfgFromSerialization);


	}

	@Test
	public void dedupConfigTest() {

		DedupConfig load = DedupConfig.load(readFromClasspath("org.curr.conf"));

		System.out.println(load.toString());
	}

	@Test
	public void translationMapTest() {

		DedupConfig load = DedupConfig.load(readFromClasspath("org.curr.conf"));

		System.out.println("translationMap = " + load.getPace().translationMap().toString());
	}

	@Test
	public void emptyTranslationMapTest() {

		DedupConfig load = DedupConfig.load(readFromClasspath("org.test.conf"));

		assertEquals(0, load.getPace().translationMap().keySet().size());
	}


	@Test
	public void testAsMapDocument() throws  Exception {

		DedupConfig load = DedupConfig.load(readFromClasspath("result.pace.conf.json"));


		System.out.println(load.getWf().getIdPath());

		final String result =IOUtils.toString(this.getClass().getResourceAsStream("result.json"));

		System.out.println(result);
		final MapDocument mapDocument = MapDocumentUtil.asMapDocument(load, result);

		System.out.println(mapDocument.getFieldMap().get("dateofacceptance").stringValue());

	}


	@Test
	public void testAsMapDocumentJPath() throws  Exception {

		DedupConfig load = DedupConfig.load(readFromClasspath("result.pace.conf_jpath.json"));


		System.out.println(load.getWf().getIdPath());

		final String result =IOUtils.toString(this.getClass().getResourceAsStream("result.json"));

		System.out.println(result);
		final MapDocument mapDocument = MapDocumentUtil.asMapDocumentWithJPath(load, result);

		System.out.println(mapDocument.getFieldMap());

	}


	@Test
	public void testJQ() throws Exception {
		final String result =IOUtils.toString(this.getClass().getResourceAsStream("result.json"));
		System.out.println(result);
		final JqLibrary library = ImmutableJqLibrary.of();
		final JqRequest request = ImmutableJqRequest.builder() //
        .lib(library) //
        .input(result) //
        .filter("[.entity.result.metadata.author[]]") //
        .build();
		final JqResponse response = request.execute();
		ObjectMapper mapper = new ObjectMapper();
		final String output = response.getOutput();
		System.out.println(output);
		final JsonNode root = mapper.readTree(output);

		System.out.println("root"+root);

		final Iterator<JsonNode> elements = root.elements();
		while (elements.hasNext()){
			System.out.println(elements.next().toString());
		}



	}



}
