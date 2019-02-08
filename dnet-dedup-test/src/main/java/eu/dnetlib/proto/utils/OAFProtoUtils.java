package eu.dnetlib.proto.utils;

import eu.dnetlib.data.proto.FieldTypeProtos;
import eu.dnetlib.data.proto.OafProtos;

public class OAFProtoUtils {


    public static FieldTypeProtos.Author author(final String s, int rank) {
        final eu.dnetlib.pace.model.Person p = new eu.dnetlib.pace.model.Person(s, false);
        final FieldTypeProtos.Author.Builder author = FieldTypeProtos.Author.newBuilder();
        if (p.isAccurate()) {
            author.setName(p.getNormalisedFirstName());
            author.setSurname(p.getNormalisedSurname());
        }
        author.setFullname(p.getNormalisedFullname());
        author.setRank(rank);

        return author.build();
    }

    public static FieldTypeProtos.StructuredProperty sp(final String pid, final String type) {
        FieldTypeProtos.StructuredProperty.Builder pidSp = FieldTypeProtos.StructuredProperty.newBuilder().setValue(pid)
                .setQualifier(FieldTypeProtos.Qualifier.newBuilder().setClassid(type).setClassname(type).setSchemeid("dnet:pid_types").setSchemename("dnet:pid_types"));
        return pidSp.build();
    }

    public static FieldTypeProtos.StringField.Builder sf(final String s) { return FieldTypeProtos.StringField.newBuilder().setValue(s); }

    public static FieldTypeProtos.StructuredProperty.Builder getStruct(final String value, final FieldTypeProtos.Qualifier.Builder qualifier) {
        return FieldTypeProtos.StructuredProperty.newBuilder().setValue(value).setQualifier(qualifier);
    }

    public static FieldTypeProtos.Qualifier.Builder getQualifier(final String classname, final String schemename) {
        return FieldTypeProtos.Qualifier.newBuilder().setClassid(classname).setClassname(classname).setSchemeid(schemename).setSchemename(schemename);
    }

    public static OafProtos.OafEntity.Builder oafEntity(final String id, final eu.dnetlib.data.proto.TypeProtos.Type type) {
        final OafProtos.OafEntity.Builder entity = OafProtos.OafEntity.newBuilder().setId(id).setType(type);
        return entity;
    }

}
