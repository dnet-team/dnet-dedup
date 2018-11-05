package eu.dnetlib.pace;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
//import eu.dnetlib.data.mapreduce.util.OafDecoder;
import eu.dnetlib.data.proto.DatasourceOrganizationProtos.DatasourceOrganization;
import eu.dnetlib.data.proto.DatasourceOrganizationProtos.DatasourceOrganization.Provision;
import eu.dnetlib.data.proto.DatasourceProtos.Datasource;
import eu.dnetlib.data.proto.DedupProtos.Dedup;
import eu.dnetlib.data.proto.FieldTypeProtos.*;
import eu.dnetlib.data.proto.FieldTypeProtos.StructuredProperty.Builder;
import eu.dnetlib.data.proto.KindProtos.Kind;
import eu.dnetlib.data.proto.OafProtos.Oaf;
import eu.dnetlib.data.proto.OafProtos.OafEntity;
import eu.dnetlib.data.proto.OafProtos.OafRel;
import eu.dnetlib.data.proto.OrganizationOrganizationProtos.OrganizationOrganization;
import eu.dnetlib.data.proto.OrganizationProtos.Organization;
import eu.dnetlib.data.proto.ProjectOrganizationProtos.ProjectOrganization;
import eu.dnetlib.data.proto.ProjectOrganizationProtos.ProjectOrganization.Participation;
import eu.dnetlib.data.proto.ProjectProtos.Project;
import eu.dnetlib.data.proto.RelMetadataProtos.RelMetadata;
import eu.dnetlib.data.proto.RelTypeProtos.RelType;
import eu.dnetlib.data.proto.RelTypeProtos.SubRelType;
import eu.dnetlib.data.proto.ResultProjectProtos.ResultProject;
import eu.dnetlib.data.proto.ResultProjectProtos.ResultProject.Outcome;
import eu.dnetlib.data.proto.ResultProtos.Result;
import eu.dnetlib.data.proto.ResultProtos.Result.Context;
import eu.dnetlib.data.proto.ResultProtos.Result.Instance;
import eu.dnetlib.data.proto.ResultResultProtos.ResultResult;
import eu.dnetlib.data.proto.ResultResultProtos.ResultResult.Similarity;
import eu.dnetlib.data.proto.TypeProtos.Type;

public class OafTest {

	public static final String CITATION_JSON =
			"<citations>\n  <citation>\n    <rawText>[10] M. Foret et al., Phys. Rev. B 66, 024204 (2002).</rawText>\n  </citation>\n  <citation>\n    <rawText>[11] B. Ru\175404\264e et al., Phys. Rev. Lett. 90, 095502 (2003).</rawText>\n  </citation>\n  <citation>\n    <rawText>[12] U. Buchenau et al., Phys. Rev. B 34, 5665 (1986).</rawText>\n  </citation>\n  <citation>\n    <rawText>[13] S.N. Taraskin and S.R. Elliott, J. Phys.: Condens. Mat- ter 11, A219 (1999).</rawText>\n  </citation>\n  <citation>\n    <rawText>[14] B. Hehlen et al., Phys. Rev. Lett. 84, 5355 (2000).</rawText>\n  </citation>\n  <citation>\n    <rawText>[15] N.V. Surotsev et al., J. Phys.: Condens. Matter 10, L113 (1998).</rawText>\n  </citation>\n  <citation>\n    <rawText>[16] D.A. Parshin and C. Laermans, Phys. Rev. B 63, 132203 (2001).</rawText>\n  </citation>\n  <citation>\n    <rawText>[17] V.L. Gurevich et al., Phys. Rev. B 67, 094203 (2003).</rawText>\n  </citation>\n  <citation>\n    <rawText>[18] A. Matic et al., Phys. Rev. Lett. 86, 3803 (2001).</rawText>\n  </citation>\n  <citation>\n    <rawText>[19] E. Rat et al., arXiv:cond-mat/0505558, 23 May 2005.</rawText>\n  </citation>\n  <citation>\n    <rawText>[1] R.C. Zeller and R.O. Pohl, Phys. Rev. B 4, 2029 (1971).</rawText>\n  </citation>\n  <citation>\n    <rawText>[20] C.A. Angell, J. Non-Cryst. Solids 131\20023133, 13 (1991).</rawText>\n  </citation>\n  <citation>\n    <rawText>[21] A.P. Sokolov et al., Phys. Rev. Lett. 71, 2062 (1993).</rawText>\n  </citation>\n  <citation>\n    <rawText>[22] T. Matsuo et al., Solid State Ionics 154-155, 759 (2002).</rawText>\n  </citation>\n  <citation>\n    <rawText>[23] V.K. Malinovsky et al., Europhys. Lett. 11, 43 (1990).</rawText>\n  </citation>\n  <citation>\n    <rawText>[24] J. Lor\250osch et al., J. Non-Cryst. Solids 69, 1 (1984).</rawText>\n  </citation>\n  <citation>\n    <rawText>[25] U. Buchenau, Z. Phys. B 58, 181 (1985).</rawText>\n  </citation>\n  <citation>\n    <rawText>[26] A.F. Io\175400e and A.R. Regel, Prog. Semicond. 4, 237 (1960).</rawText>\n  </citation>\n  <citation>\n    <rawText>[27] R. Dell\20031Anna et al., Phys. Rev. Lett. 80, 1236 (1998).</rawText>\n  </citation>\n  <citation>\n    <rawText>[28] D. Fioretto et al., Phys. Rev. E 59, 4470 (1999).</rawText>\n  </citation>\n  <citation>\n    <rawText>[29] U. Buchenau et al., Phys. Rev. Lett. 77, 4035 (1996).</rawText>\n  </citation>\n  <citation>\n    <rawText>[2] M. Rothenfusser et al., Phys. Rev. B 27, 5196 (1983).</rawText>\n  </citation>\n  <citation>\n    <rawText>[30] J. Mattsson et al., J. Phys.: Condens. Matter 15, S1259 (2003).</rawText>\n  </citation>\n  <citation>\n    <rawText>[31] T. Scopigno et al., Phys. Rev. Lett. 92, 025503 (2004).</rawText>\n  </citation>\n  <citation>\n    <rawText>[32] M. Foret et al., Phys. Rev. Lett. 81, 2100 (1998).</rawText>\n  </citation>\n  <citation>\n    <rawText>[33] F. Sette et al., Science 280, 1550 (1998).</rawText>\n  </citation>\n  <citation>\n    <rawText>[34] J. Wuttke et al., Phys. Rev. E 52, 4026 (1995).</rawText>\n  </citation>\n  <citation>\n    <rawText>[35] M.A. Ramos et al., Phys. Rev. Lett. 78, 82 (1997).</rawText>\n  </citation>\n  <citation>\n    <rawText>[36] G. Monaco et al., Phys. Rev. Lett. 80, 2161 (1998).</rawText>\n  </citation>\n  <citation>\n    <rawText>[37] A. T\250olle, Rep. Prog. Phys. 64, 1473 (2001).</rawText>\n  </citation>\n  <citation>\n    <rawText>[38] As the straight lines do not cross the origin, this does not 2 imply \1623 \21035 \1651 .</rawText>\n  </citation>\n  <citation>\n    <rawText>[39] A. Matic et al., Europhys. Lett. 54, 77 (2001).</rawText>\n  </citation>\n  <citation>\n    <rawText>[3] S. Hunklinger and W. Arnold, in Physical Acoustics, Vol. XII, W.P. Mason and R.N. Thurston Eds. (Academic Press, N.Y. 1976), p. 155.</rawText>\n  </citation>\n  <citation>\n    <rawText>[40] IXS data are usually not available below \1651co, mostly for experimental reasons. E.g., that the rapid onset was not evidenced in vitreous silica [27], is not indicative of its absence but rather of a low qco \21074 1 nm\210221.</rawText>\n  </citation>\n  <citation>\n    <rawText>[41] G. Ruocco et al., Phys. Rev. Lett. 83, 5583 (1999).</rawText>\n  </citation>\n  <citation>\n    <rawText>[42] D. C\1307 iplys et al., J. Physique (Paris) 42, C6-184 (1981).</rawText>\n  </citation>\n  <citation>\n    <rawText>[43] R. Vacher et al., Rev. Sci. Instrum. 51, 288 (1980).</rawText>\n  </citation>\n  <citation>\n    <rawText>[44] R. Vacher et al., arXiv:cond-mat/0505560, 23 May 2005.</rawText>\n  </citation>\n  <citation>\n    <rawText>[45] T.N. Claytor et al., Phys. Rev. B 18, 5842 (1978).</rawText>\n  </citation>\n  <citation>\n    <rawText>[46] M. Arai et al., Physica B 263-264, 268 (1999).</rawText>\n  </citation>\n  <citation>\n    <rawText>[4] R. Vacher et al., J. Non-Cryst. Solids 45, 397 (1981); T.C. Zhu et al., Phys. Rev. B 44, 4281 (1991).</rawText>\n  </citation>\n  <citation>\n    <rawText>[5] J.E. Graebner et al., Phys. Rev. B 34, 5696 (1986).</rawText>\n  </citation>\n  <citation>\n    <rawText>[6] E. Duval and A. Mermet, Phys. Rev. B 58, 8159 (1998).</rawText>\n  </citation>\n  <citation>\n    <rawText>[7] A. Matic et al., Phys. Rev. Lett. 93, 145502 (2004).</rawText>\n  </citation>\n  <citation>\n    <rawText>[8] Often alluded to, e.g. in the Encyclopedia of Materials: Science and Technology, K.H.J. Buschow et al., Eds., Vol. 1 (Elsevier, Oxford, 2001), articles by S.R. Elliott on pp. 171-174 and U. Buchenau on pp. 212-215.</rawText>\n  </citation>\n  <citation>\n    <rawText>[9] E. Rat et al., Phys. Rev. Lett. 83, 1355 (1999).</rawText>\n  </citation>\n</citations>";

	public static final String STATISTICS_JSON =
			"[{ \"citationsPerYear\": \"many\", \"anotherCoolStatistic\": \"WoW\", \"nestedStat\": { \"firstNestedStat\" : \"value 1\", \"secondNestedStat\" : \"value 2\"}, \"listingStat\" : [ \"one\", \"two\" ] }]";

	public static Builder getStructuredproperty(final String value, final String classname, final String schemename) {
		return getStructuredproperty(value, classname, schemename, null);
	}

	public static Builder getStructuredproperty(final String value, final String classname, final String schemename, final DataInfo dataInfo) {
		final Builder sp = StructuredProperty.newBuilder().setValue(value).setQualifier(getQualifier(classname, schemename));
		if (dataInfo != null) {
			sp.setDataInfo(dataInfo);
		}
		return sp;
	}

	public static Qualifier.Builder getQualifier(final String classname, final String schemename) {
		return Qualifier.newBuilder().setClassid(classname).setClassname(classname).setSchemeid(schemename).setSchemename(schemename);
	}

	public static KeyValue getKV(final String id, final String name) {
		return KeyValue.newBuilder().setKey(id).setValue(name).build();
	}

	public static OafEntity getDatasource(final String datasourceId) {
		return OafEntity
				.newBuilder()
				.setType(Type.datasource)
				.setId(datasourceId)
				.setDatasource(
						Datasource.newBuilder().setMetadata(
								Datasource.Metadata.newBuilder().setOfficialname(sf("officialname")).setEnglishname(sf("englishname"))
										.setWebsiteurl(sf("websiteurl")).setContactemail(sf("contactemail")).addAccessinfopackage(sf("accessinforpackage"))
										.setNamespaceprefix(sf("namespaceprofix")).setDescription(sf("description")).setOdnumberofitems(sf("numberofitems"))
										.setOdnumberofitemsdate(sf("numberofitems date"))
										// .addOdsubjects("subjects")
										.setOdpolicies(sf("policies")).addOdlanguages(sf("languages")).addOdcontenttypes(sf("contenttypes"))
										.setDatasourcetype(getQualifier("type class", "type scheme")))).build();
	}

	public static OafEntity getResult(final String id) {
		return getResultBuilder(id).build();
	}

	public static OafEntity.Builder getResultBuilder(final String id) {
		return OafEntity
				.newBuilder()
				.setType(Type.result)
				.setId(id)
				.setResult(
						Result.newBuilder()
								.setMetadata(
										Result.Metadata
												.newBuilder()
												.addTitle(
														getStructuredproperty(
																"Analysis of cell viability in intervertebral disc: Effect of endplate permeability on cell population",
																"main title", "dnet:result_titles", getDataInfo()))
												.addTitle(getStructuredproperty("Another title", "alternative title", "dnet:result_titles", getDataInfo()))
												.addSubject(getStructuredproperty("Biophysics", "subject", "dnet:result_sujects"))
												.setDateofacceptance(sf("2010-01-01")).addSource(sf("sourceA")).addSource(sf("sourceB"))
												.addContext(Context.newBuilder().setId("egi::virtual::970"))
												.addContext(Context.newBuilder().setId("egi::classification::natsc::math::applied"))
												.addContext(Context.newBuilder().setId("egi::classification::natsc::math"))
												.addContext(Context.newBuilder().setId("egi::classification::natsc"))
												.addContext(Context.newBuilder().setId("egi::classification")).addContext(Context.newBuilder().setId("egi"))
												.addDescription(sf("Responsible for making and maintaining the extracellular matrix ..."))
												.addDescription(sf("Another description ...")).setPublisher(sf("ELSEVIER SCI LTD"))
												.setResulttype(getQualifier("publication", "dnet:result_types"))
												.setLanguage(getQualifier("eng", "dnet:languages"))).addInstance(getInstance("10|od__10", "Uk pubmed"))
								.addInstance(getInstance("10|od__10", "arxiv")))
				.addCollectedfrom(getKV("opendoar____::1064", "Oxford University Research Archive"))
				.addPid(getStructuredproperty("doi:74293", "doi", "dnet:pids")).addPid(getStructuredproperty("oai:74295", "oai", "dnet:pids"))
				.setDateofcollection("");
	}

	public static DataInfo getDataInfo() {
		return getDataInfo("0.4");
	}

	public static DataInfo getDataInfo(final String trust) {
		return DataInfo.newBuilder().setDeletedbyinference(false).setTrust("0.4").setInferenceprovenance("algo").setProvenanceaction(getQualifier("xx", "yy"))
				.build();
	}

	public static Instance.Builder getInstance(final String hostedbyId, final String hostedbyName) {
		return Instance.newBuilder().setHostedby(getKV(hostedbyId, hostedbyName)).setAccessright(getQualifier("OpenAccess", "dnet:access_modes"))
				.setInstancetype(getQualifier("publication", "dnet:result_typologies")).addUrl("webresource url");

	}

	public static OafRel getDedupRel(final String source, final String target, final RelType relType, final String relClass) {
		return OafRel.newBuilder().setSource(source).setTarget(target).setRelType(relType).setSubRelType(SubRelType.dedup).setRelClass(relClass)
				.setChild(false).setCachedTarget(getResult(target))
				.setResultResult(ResultResult.newBuilder().setDedup(Dedup.newBuilder().setRelMetadata(RelMetadata.getDefaultInstance())))
				.build();
	}

	public static OafRel getProjectOrganization(final String source, final String target, final String relClass) throws InvalidProtocolBufferException {
		final OafRel.Builder oafRel = OafRel
				.newBuilder()
				.setSource(source)
				.setTarget(target)
				.setRelType(RelType.projectOrganization)
				.setSubRelType(SubRelType.participation)
				.setRelClass(relClass)
				.setChild(false)
				.setProjectOrganization(
						ProjectOrganization.newBuilder().setParticipation(
								Participation.newBuilder().setParticipantnumber("" + 1)
										.setRelMetadata(relMetadata(relClass, "dnet:project_organization_relations"))));
		switch (Participation.RelName.valueOf(relClass)) {
		case hasParticipant:
			oafRel.setCachedTarget(getProjectFP7(target, "SP3"));
			break;
		case isParticipant:
			oafRel.setCachedTarget(getOrganization(target));
			break;
		default:
			break;
		}
		return oafRel.build();
	}

	public static GeneratedMessage getOrganizationOrganization(final String source, final String target, final String relClass) {
		final OafRel.Builder oafRel = OafRel
				.newBuilder()
				.setSource(source)
				.setTarget(target)
				.setRelType(RelType.organizationOrganization)
				.setSubRelType(SubRelType.dedup)
				.setRelClass(relClass)
				.setChild(true)
				.setOrganizationOrganization(
						OrganizationOrganization.newBuilder().setDedup(
								Dedup.newBuilder().setRelMetadata(relMetadata(relClass, "dnet:organization_organization_relations"))));

		switch (Dedup.RelName.valueOf(relClass)) {
		case isMergedIn:
			oafRel.setCachedTarget(getOrganization(source));
			break;
		case merges:
			oafRel.setCachedTarget(getOrganization(target));
			break;
		default:
			break;
		}
		return oafRel.build();
	}

	public static OafRel getDatasourceOrganization(final String source, final String target, final String relClass) throws InvalidProtocolBufferException {
		final OafRel.Builder oafRel = OafRel
				.newBuilder()
				.setSource(source)
				.setTarget(target)
				.setRelType(RelType.datasourceOrganization)
				.setSubRelType(SubRelType.provision)
				.setRelClass(relClass)
				.setChild(false)
				.setDatasourceOrganization(
						DatasourceOrganization.newBuilder().setProvision(
								Provision.newBuilder().setRelMetadata(relMetadata(relClass, "dnet:datasource_organization_relations"))));
		switch (Provision.RelName.valueOf(relClass)) {
		case isProvidedBy:
			oafRel.setCachedTarget(getOrganization(target));
			break;
		case provides:
			oafRel.setCachedTarget(getDatasource(target));
			break;
		default:
			break;
		}
		return oafRel.build();
	}

	public static OafRel getSimilarityRel(final String sourceId, final String targetId, final OafEntity result, final String relClass) {
		return OafRel
				.newBuilder()
				.setSource(sourceId)
				.setTarget(targetId)
				.setRelType(RelType.resultResult)
				.setSubRelType(SubRelType.similarity)
				.setRelClass(relClass)
				.setChild(false)
				.setCachedTarget(result)
				.setResultResult(
						ResultResult.newBuilder().setSimilarity(
								Similarity.newBuilder().setRelMetadata(relMetadata(relClass, "dnet:resultResult_relations")).setSimilarity(.4f)
										.setType(Similarity.Type.STANDARD))).build();
	}

	public static RelMetadata.Builder relMetadata(final String classname, final String schemename) {
		return RelMetadata.newBuilder().setSemantics(getQualifier(classname, schemename));
	}

	public static OafEntity getOrganization(final String orgId) {
		return OafEntity
				.newBuilder()
				.setType(Type.organization)
				.setId(orgId)
				.addCollectedfrom(getKV("opendoar_1234", "UK pubmed"))
				.setOrganization(
						Organization.newBuilder().setMetadata(
								Organization.Metadata.newBuilder().setLegalname(sf("CENTRE D'APPUI A LA RECHERCHE ET A LA FORMATION GIE"))
										.setLegalshortname(sf("CAREF")).setWebsiteurl(sf("www.caref-mali.org"))
										.setCountry(getQualifier("ML", "dnet:countries")))).build();
	}

	public static OafRel getResultProject(final String from, final String to, final OafEntity project, final String relClass)
			throws InvalidProtocolBufferException {
		return OafRel
				.newBuilder()
				.setSource(from)
				.setTarget(to)
				.setRelType(RelType.resultProject)
				.setSubRelType(SubRelType.outcome)
				.setRelClass(relClass)
				.setChild(false)
				.setResultProject(
						ResultProject.newBuilder().setOutcome(Outcome.newBuilder().setRelMetadata(relMetadata(relClass, "dnet:result_project_relations"))))
				.setCachedTarget(project).build();
	}

	public static OafEntity getProjectFP7(final String projectId, final String fundingProgram) throws InvalidProtocolBufferException {
		return OafEntity
				.newBuilder()
				.setType(Type.project)
				.setId(projectId)
				.addCollectedfrom(getKV("opendoar_1234", "UK pubmed"))
				.setProject(
						Project.newBuilder()
								.setMetadata(
										Project.Metadata
												.newBuilder()
												.setAcronym(sf("5CYRQOL"))
												.setTitle(sf("Cypriot Researchers Contribute to our Quality of Life"))
												.setStartdate(sf("2007-05-01"))
												.setEnddate(sf("2007-10-31"))
												.setEcsc39(sf("false"))
												.setContracttype(getQualifier("CSA", "ec:FP7contractTypes"))
												.addFundingtree(
														sf("<fundingtree><funder><id>ec__________::EC</id><shortname>EC</shortname><name>European Commission</name></funder><funding_level_2><id>ec__________::EC::FP7::"
																+ fundingProgram
																+ "::PEOPLE</id><description>Marie-Curie Actions</description><name>PEOPLE</name><class>ec:program</class><parent><funding_level_1><id>ec__________::EC::FP7::"
																+ fundingProgram
																+ "</id><description>"
																+ fundingProgram
																+ "-People</description><name>"
																+ fundingProgram
																+ "</name><class>ec:specificprogram</class><parent><funding_level_0><id>ec__________::EC::FP7</id><description>SEVENTH FRAMEWORK PROGRAMME</description><name>FP7</name><parent/><class>ec:frameworkprogram</class></funding_level_0></parent></funding_level_1></parent></funding_level_2></fundingtree>"))))
				.build();
	}

	public static OafEntity getProjectWT() throws InvalidProtocolBufferException {
		return OafEntity
				.newBuilder()
				.setType(Type.project)
				.setId("project|wt::087536")
				.addCollectedfrom(getKV("wellcomeTrust", "wellcome trust"))
				.setProject(
						Project.newBuilder()
								.setMetadata(
										Project.Metadata
												.newBuilder()
												.setAcronym(sf("UNKNOWN"))
												.setTitle(sf("Research Institute for Infectious Diseases of Poverty (IIDP)."))
												.setStartdate(sf("2007-05-01"))
												.setEnddate(sf("2007-10-31"))
												.setEcsc39(sf("false"))
												.setContracttype(getQualifier("UNKNOWN", "wt:contractTypes"))
												.addFundingtree(
														sf("<fundingtree><funder><id>wt__________::WT</id><shortname>WT</shortname><name>Wellcome Trust</name></funder><funding_level_0><id>wt__________::WT::UNKNOWN</id><description>UNKNOWN</description><name>UNKNOWN</name><class>wt:fundingStream</class><parent/></funding_level_0></fundingtree>"))
												.addFundingtree(
														sf("<fundingtree><funder><id>wt__________::WT</id><shortname>WT</shortname><name>Wellcome Trust</name></funder><funding_level_0><id>wt__________::WT::Technology Transfer</id><description>Technology Transfer</description><name>Technology Transfer</name><class>wt:fundingStream</class><parent/></funding_level_0></fundingtree>"))))
				.build();
	}

	public static ExtraInfo extraInfo(final String name, final String provenance, final String trust, final String typology, final String value) {
		final ExtraInfo.Builder e = ExtraInfo.newBuilder().setName(name).setProvenance(provenance).setTrust(trust).setTypology(typology).setValue(value);
		return e.build();
	}

	// public static DocumentClasses documentClasses() {
	// DocumentClasses.Builder builder = DocumentClasses.newBuilder();
	// for (int i = 0; i < RandomUtils.nextInt(N_DOCUMENT_CLASSES) + 1; i++) {
	// builder.addArXivClasses(getDocumentClass()).addDdcClasses(getDocumentClass()).addWosClasses(getDocumentClass())
	// .addMeshEuroPMCClasses(getDocumentClass());
	// }
	// return builder.build();
	// }
	//
	// private static DocumentClass getDocumentClass() {
	// DocumentClass.Builder builder = DocumentClass.newBuilder();
	// for (int i = 0; i < RandomUtils.nextInt(N_DOCUMENT_CLASS_LABELS) + 1; i++) {
	// builder.addClassLabels("test_class_" + i);
	// }
	// return builder.setConfidenceLevel(0.5F).build();
	// }
	//
	// public static DocumentStatistics documentStatistics() {
	// return
	// DocumentStatistics.newBuilder().setCitationsFromAllPapers(basicCitationStatistics()).setCitationsFromPublishedPapers(basicCitationStatistics())
	// .build();
	// }
	//
	// private static BasicCitationStatistics basicCitationStatistics() {
	// BasicCitationStatistics.Builder builder = BasicCitationStatistics.newBuilder();
	// for (int i = 0; i < N_CITATION_STATS; i++) {
	// builder.addNumberOfCitationsPerYear(statisticsKeyValue());
	// builder.setNumberOfCitations(RandomUtils.nextInt(5) + 1);
	// }
	// return builder.build();
	// }
	//
	// private static StatisticsKeyValue statisticsKeyValue() {
	// return StatisticsKeyValue.newBuilder().setKey((RandomUtils.nextInt(30) + 1980) + "").setValue(RandomUtils.nextInt(5) + 1).build();
	// }
	//
	// public static AuthorStatistics authorStatistics() {
	// AuthorStatistics.Builder builder = AuthorStatistics.newBuilder();
	// builder.setCore(commonCoreStatistics());
	// for (int i = 0; i < N_COAUTHORS; i++) {
	// builder.addCoAuthors(coAuthor());
	// }
	// return builder.build();
	// }
	//
	// private static CoAuthor coAuthor() {
	// CoAuthor.Builder builder = CoAuthor.newBuilder();
	// builder.setId("30|od______2345::" + Hashing.md5(RandomStringUtils.random(10)));
	// builder.setCoauthoredPapersCount(RandomUtils.nextInt(5) + 1);
	// return builder.build();
	// }
	//
	// public static CommonCoreStatistics commonCoreStatistics() {
	// CommonCoreStatistics.Builder builder = CommonCoreStatistics.newBuilder();
	//
	// builder.setAllPapers(coreStatistics());
	// builder.setPublishedPapers(coreStatistics());
	//
	// return builder.build();
	// }
	//
	// private static CoreStatistics coreStatistics() {
	// CoreStatistics.Builder builder = CoreStatistics.newBuilder();
	//
	// builder.setNumberOfPapers(RandomUtils.nextInt(10));
	// builder.setCitationsFromAllPapers(extendedStatistics());
	// builder.setCitationsFromPublishedPapers(extendedStatistics());
	//
	// return builder.build();
	// }
	//
	// private static ExtendedStatistics extendedStatistics() {
	// ExtendedStatistics.Builder builder = ExtendedStatistics.newBuilder();
	//
	// builder.setBasic(basicCitationStatistics());
	// builder.setAverageNumberOfCitationsPerPaper(RandomUtils.nextFloat());
	// for (int i = 0; i < N_CITATION_STATS; i++) {
	// builder.addNumberOfPapersCitedAtLeastXTimes(statisticsKeyValue());
	// }
	//
	// return builder.build();
	// }

	public static StringField sf(final String s) {
		return sf(s, null);
	}

	public static StringField sf(final String s, final DataInfo dataInfo) {
		final StringField.Builder sf = StringField.newBuilder().setValue(s);
		if (dataInfo != null) {
			sf.setDataInfo(dataInfo);
		}
		return sf.build();
	}

//	public static OafDecoder embed(final GeneratedMessage msg,
//	   final Kind kind,
//	   final boolean deletedByInference,
//	   final boolean inferred,
//	   final String provenance,
//	   final String action) {
//
//		final Oaf.Builder oaf = Oaf
//				.newBuilder()
//				.setKind(kind)
//				.setLastupdatetimestamp(System.currentTimeMillis())
//				.setDataInfo(
//						DataInfo.newBuilder().setDeletedbyinference(deletedByInference).setInferred(inferred).setTrust("0.5")
//								.setInferenceprovenance(provenance).setProvenanceaction(getQualifier(action, action)));
//		switch (kind) {
//		case entity:
//			oaf.setEntity((OafEntity) msg);
//			break;
//		case relation:
//			oaf.setRel((OafRel) msg);
//			break;
//		default:
//			break;
//		}
//
//		return OafDecoder.decode(oaf.build());
//	}
//
//	public static OafDecoder embed(final GeneratedMessage msg, final Kind kind) {
//		return embed(msg, kind, false, false, "inference_provenance", "provenance_action");
//	}

}
