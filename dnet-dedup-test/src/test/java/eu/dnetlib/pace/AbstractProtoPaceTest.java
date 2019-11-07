package eu.dnetlib.pace;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import eu.dnetlib.data.proto.FieldTypeProtos.Author;
import eu.dnetlib.data.proto.FieldTypeProtos.Qualifier;
import eu.dnetlib.data.proto.FieldTypeProtos.StructuredProperty;
import eu.dnetlib.data.proto.FieldTypeProtos.StructuredProperty.Builder;
import eu.dnetlib.data.proto.OafProtos.Oaf;
import eu.dnetlib.data.proto.OafProtos.OafEntity;
import eu.dnetlib.data.proto.OrganizationProtos.Organization;
import eu.dnetlib.data.proto.ResultProtos.Result;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.config.Type;
import eu.dnetlib.pace.model.*;
import eu.dnetlib.pace.model.gt.GTAuthor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractProtoPaceTest extends OafTest {

	protected DedupConfig getOrganizationCurrentConf() {
		return DedupConfig.load(readFromClasspath("/eu/dnetlib/pace/config/organization.current.conf"));
	}

    protected DedupConfig getOrganizationTestConf() {
        return DedupConfig.load(readFromClasspath("/eu/dnetlib/pace/config/organization.test.conf"));
    }

	protected MapDocument author(final Config conf, final String id, final Oaf oaf) {
		return ProtoDocumentBuilder.newInstance(id, oaf.getEntity(), conf.model());
	}

	protected GTAuthor getGTAuthor(final String path) {

		final Gson gson = new Gson();

		final String json = readFromClasspath(path);

		final GTAuthor gta = gson.fromJson(json, GTAuthor.class);

		return gta;
	}

	protected String readFromClasspath(final String filename) {
		final StringWriter sw = new StringWriter();
		try {
			IOUtils.copy(getClass().getResourceAsStream(filename), sw);
			return sw.toString();
		} catch (final IOException e) {
			throw new RuntimeException("cannot load resource from classpath: " + filename);
		}
	}

	protected MapDocument result(final Config config, final String id, final String title) {
		return result(config, id, title, null, new ArrayList<>(), null);
	}

	protected MapDocument result(final Config config, final String id, final String title, final String date) {
		return result(config, id, title, date, new ArrayList<>(), null);
	}

	protected MapDocument result(final Config config, final String id, final String title, final String date, final List<String> pid) {
		return result(config, id, title, date, pid, null);
	}

	protected MapDocument result(final Config config, final String id, final String title, final String date, final String pid) {
		return result(config, id, title, date, pid, null);
	}

	protected MapDocument result(final Config config, final String id, final String title, final String date, final String pid, final List<String> authors) {
		return result(config, id, title, date, Lists.newArrayList(pid), authors);
	}

	protected MapDocument author(final String identifier, final String area, final String firstname, final String lastname, final String fullname, final Double[] topics, final String pubID, final String pubDOI, final int rank, final String orcid, final List<String> coauthors) {
		Map<String, Field> fieldMap = new HashMap<>();

		fieldMap.put("area", new FieldValueImpl(Type.String, "area", area));
		fieldMap.put("firstname", new FieldValueImpl(Type.String, "firstname", firstname));
		fieldMap.put("lastname", new FieldValueImpl(Type.String, "lastname", lastname));
		fieldMap.put("fullname", new FieldValueImpl(Type.String, "fullname", fullname));
		fieldMap.put("pubID", new FieldValueImpl(Type.String, "pubID", pubID));
		fieldMap.put("pubDOI", new FieldValueImpl(Type.String, "pubDOI", pubDOI));
		fieldMap.put("rank", new FieldValueImpl(Type.Int, "rank", rank));
		fieldMap.put("orcid", new FieldValueImpl(Type.String, "orcid", orcid));

		FieldListImpl ca = new FieldListImpl("coauthors", Type.String);
		ca.addAll(coauthors.stream().map(s -> new FieldValueImpl(Type.String, "coauthors", s)).collect(Collectors.toList()));
		fieldMap.put("coauthors", ca);

		FieldListImpl t = new FieldListImpl("topics", Type.String);
		t.addAll(Arrays.asList(topics).stream().map(d -> new FieldValueImpl(Type.String, "topics", d.toString())).collect(Collectors.toList()));
		fieldMap.put("topics", t);

		return new MapDocument(identifier, fieldMap);
	}

	static List<String> pidTypes = Lists.newArrayList();
	static {
		pidTypes.add("doi");
		//pidTypes.add("oai");
		//pidTypes.add("pmid");
	}

	protected MapDocument result(final Config config, final String id, final String title, final String date, final List<String> pid, final List<String> authors) {
		final Result.Metadata.Builder metadata = Result.Metadata.newBuilder();
		if (!StringUtils.isBlank(title)) {
			metadata.addTitle(getStruct(title, getQualifier("main title", "dnet:titles")));
			metadata.addTitle(getStruct(RandomStringUtils.randomAlphabetic(10), getQualifier("alternative title", "dnet:titles")));
		}
		if (!StringUtils.isBlank(date)) {
			metadata.setDateofacceptance(sf(date));
		}

		final OafEntity.Builder entity = oafEntity(id, eu.dnetlib.data.proto.TypeProtos.Type.result);
		final Result.Builder result = Result.newBuilder().setMetadata(metadata);

		if (authors != null) {
			result.getMetadataBuilder().addAllAuthor(
					IntStream.range(0, authors.size())
							.mapToObj(i -> author(authors.get(i), i))
							.collect(Collectors.toCollection(LinkedList::new)));
		}

		entity.setResult(result);

		if (pid != null) {
			for(String p : pid) {
				if (!StringUtils.isBlank(p)) {
					entity.addPid(sp(p, pidTypes.get(RandomUtils.nextInt(0, pidTypes.size() - 1))));
					//entity.addPid(sp(RandomStringUtils.randomAlphabetic(10), "oai"));
				}
			}
		}

		final OafEntity build = entity.build();
		return ProtoDocumentBuilder.newInstance(id, build, config.model());
	}

	private Author author(final String s, int rank) {
		final eu.dnetlib.pace.model.Person p = new eu.dnetlib.pace.model.Person(s, false);
		final Author.Builder author = Author.newBuilder();
		if (p.isAccurate()) {
			author.setName(p.getNormalisedFirstName());
			author.setSurname(p.getNormalisedSurname());
		}
		author.setFullname(p.getNormalisedFullname());
		author.setRank(rank);

		return author.build();
	}

	private OafEntity.Builder oafEntity(final String id, final eu.dnetlib.data.proto.TypeProtos.Type type) {
		final OafEntity.Builder entity = OafEntity.newBuilder().setId(id).setType(type);
		return entity;
	}

	protected MapDocument organization(final Config config, final String id, final String legalName) {
		return organization(config, id, legalName, null);
	}

	protected MapDocument organization(final Config config, final String id, final String legalName, final String legalShortName) {
		final Organization.Metadata.Builder metadata = Organization.Metadata.newBuilder();
		if (legalName != null) {
			metadata.setLegalname(sf(legalName));
		}
		if (legalShortName != null) {
			metadata.setLegalshortname(sf(legalShortName));
		}

		final OafEntity.Builder entity = oafEntity(id, eu.dnetlib.data.proto.TypeProtos.Type.result);
		entity.setOrganization(Organization.newBuilder().setMetadata(metadata));

		return ProtoDocumentBuilder.newInstance(id, entity.build(), config.model());
	}

	private StructuredProperty sp(final String pid, final String type) {
		final Builder pidSp =
				StructuredProperty.newBuilder().setValue(pid)
						.setQualifier(Qualifier.newBuilder().setClassid(type).setClassname(type).setSchemeid("dnet:pid_types").setSchemename("dnet:pid_types"));
		return pidSp.build();
	}

	protected Field title(final String s) {
		return new FieldValueImpl(Type.String, "title", s);
	}

	protected static Builder getStruct(final String value, final Qualifier.Builder qualifier) {
		return StructuredProperty.newBuilder().setValue(value).setQualifier(qualifier);
	}

	/*
	 * protected static StringField.Builder sf(final String s) { return StringField.newBuilder().setValue(s); }
	 *
	 * protected static Qualifier.Builder getQualifier(final String classname, final String schemename) { return
	 * Qualifier.newBuilder().setClassid(classname).setClassname(classname).setSchemeid(schemename).setSchemename(schemename); }
	 */

}
