package eu.dnetlib.pace.model;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.protobuf.GeneratedMessage;

import eu.dnetlib.data.transform.AbstractProtoMapper;

public class ProtoDocumentBuilder extends AbstractProtoMapper {

    public static MapDocument newInstance(final String id, final GeneratedMessage proto, final List<FieldDef> fields) {
        final Map<String, Field> fieldMap = new ProtoDocumentBuilder().generateFieldMap(proto, fields);
        return new MapDocument(id, fieldMap);
    }

    private Map<String, Field> generateFieldMap(final GeneratedMessage proto, final List<FieldDef> fields) {
        final Map<String, Field> fieldMap = Maps.newHashMap();

        for (final FieldDef fd : fields) {

            final FieldList fl = new FieldListImpl(fd.getName(), fd.getType());

            for (final Object o : processPath(proto, fd.getPathList(), fd.getType())) {

                fl.add(new FieldValueImpl(fd.getType(), fd.getName(), o));
            }

            fieldMap.put(fd.getName(), fl);
        }

        return fieldMap;
    }

}