package eu.dnetlib.pace.config;

import java.io.IOException;

import eu.dnetlib.pace.AbstractPaceTest;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.model.MapDocumentSerializer;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigTest extends AbstractPaceTest {

	@Test
	public void dedupConfigSerializationTest() throws IOException {
		final DedupConfig cfgFromClasspath = DedupConfig.load(readFromClasspath("result.pace.conf.json"));

		assertNotNull(cfgFromClasspath);

		String conf = "{ \n" +
				"wf\" : { " +
				"        \"threshold\" : \"0.99\", " +
				"        \"run\" : \"001\", " +
				"        \"entityType\" : \"result\", " +
				"        \"orderField\" : \"title\", " +
				"        \"queueMaxSize\" : \"2000\"," +
				"        \"groupMaxSize\" : \"10\"," +
				"        \"slidingWindowSize\" : \"200\"," +
				"        \"rootBuilder\" : [ \"result\" ]," +
				"        \"includeChildren\" : \"true\" " +
				"    }," +
				"\t\"pace\" : {\t\t\n" +
				"\t\t\"clustering\" : [\n" +
				"\t\t\t{ \"name\" : \"acronyms\", \"fields\" : [ \"title\" ], \"params\" : { \"max\" : \"1\", \"minLen\" : \"2\", \"maxLen\" : \"4\"} },\n" +
				"\t\t\t{ \"name\" : \"ngrampairs\", \"fields\" : [ \"title\" ], \"params\" : { \"max\" : \"1\", \"ngramLen\" : \"3\"} },\n" +
				"\t\t\t{ \"name\" : \"suffixprefix\", \"fields\" : [ \"title\" ], \"params\" : { \"max\" : \"1\", \"len\" : \"3\" } } \n" +
				"\t\t],\t\t\n" +
				"\t\t\"strictConditions\" : [\n" +
				"  \t\t\t{ \"name\" : \"exactMatch\", \"fields\" : [ \"pid\" ] }\n" +
				"  \t\t], \n" +
				"  \t\t\"conditions\" : [ \n" +
				"  \t\t\t{ \"name\" : \"yearMatch\", \"fields\" : [ \"dateofacceptance\" ] },\n" +
				"  \t\t\t{ \"name\" : \"titleVersionMatch\", \"fields\" : [ \"title\" ] },\n" +
				"  \t\t\t{ \"name\" : \"sizeMatch\", \"fields\" : [ \"authors\" ] } \n" +
				"  \t\t],\t\t\n" +
				"\t\t\"model\" : [\n" +
				"\t\t\t{ \"name\" : \"pid\", \"algo\" : \"Null\", \"type\" : \"String\", \"weight\" : \"0.0\", \"ignoreMissing\" : \"true\", \"path\" : \"pid[qualifier#classid = {doi}]/value\", \"overrideMatch\" : \"true\" }, \t\n" +
				"\t\t\t{ \"name\" : \"title\", \"algo\" : \"JaroWinkler\", \"type\" : \"String\", \"weight\" : \"1.0\", \"ignoreMissing\" : \"false\", \"path\" : \"result/metadata/title[qualifier#classid = {main title}]/value\" },\n" +
				"\t\t\t{ \"name\" : \"dateofacceptance\", \"algo\" : \"Null\", \"type\" : \"String\", \"weight\" : \"0.0\", \"ignoreMissing\" : \"true\", \"path\" : \"result/metadata/dateofacceptance/value\" } ,\n" +
				"\t\t\t{ \"name\" : \"authors\", \"algo\" : \"Null\", \"type\" : \"List\", \"weight\" : \"0.0\", \"ignoreMissing\" : \"true\", \"path\" : \"result/author/metadata/fullname/value\" }\n" +
				"\t\t],\n" +
				"\t\t\"blacklists\" : {\n" +
				"\t\t\t\"title\" : [\n" +
				"\t\t\t\t\"^(Corpus Oral Dialectal \\\\(COD\\\\)\\\\.).*$\",\n" +
				"\t\t\t\t\"^(Kiri Karl Morgensternile).*$\",\n" +
				"\t\t\t\t\"^(\\\\[Eksliibris Aleksandr).*\\\\]$\",\n" +
				"\t\t\t\t\"^(\\\\[Eksliibris Aleksandr).*$\",\n" +
				"\t\t\t\t\"^(Eksliibris Aleksandr).*$\",\n" +
				"\t\t\t\t\"^(Kiri A\\\\. de Vignolles).*$\",\n" +
				"\t\t\t\t\"^(2 kirja Karl Morgensternile).*$\",\n" +
				"\t\t\t\t\"^(Pirita kloostri idaosa arheoloogilised).*$\",\n" +
				"\t\t\t\t\"^(Kiri tundmatule).*$\",\n" +
				"\t\t\t\t\"^(Kiri Jenaer Allgemeine Literaturzeitung toimetusele).*$\",\n" +
				"\t\t\t\t\"^(Eksliibris Nikolai Birukovile).*$\",\n" +
				"\t\t\t\t\"^(Eksliibris Nikolai Issakovile).*$\",\n" +
				"\t\t\t\t\"^(WHP Cruise Summary Information of section).*$\",\n" +
				"\t\t\t\t\"^(Measurement of the top quark\\\\-pair production cross section with ATLAS in pp collisions at).*$\",\n" +
				"\t\t\t\t\"^(Measurement of the spin\\\\-dependent structure function).*\"\n" +
				"\t\t\t] } \t\t\n" +
				"\t}\n" +
				"\n" +
				"}";

		final DedupConfig cfgFromSerialization = DedupConfig.load(cfgFromClasspath.toString());
		String params = "\"params\":{\"limit\":-1,\"weight\":0.0}";
		//verify if the serialization produces the same result of the input json
		assertEquals(cfgFromSerialization.toString().replaceAll("[\n\t\r ]", "").replaceAll("\"params\":null", params), cfgFromClasspath.toString().replaceAll("[\n\t\r ]", ""));

	}

	@Test
	public void dedupConfigTest() {

		DedupConfig load = DedupConfig.load(readFromClasspath("result.pace.conf.json"));

		System.out.println(load.toString());
	}

}
