package eu.dnetlib.data.transform;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;

import eu.dnetlib.pace.config.Type;

/**
 * AbstractProtoMapper provide common navigation methods on the protocolbuffers Messages.
 *
 * @author claudio
 */
public abstract class AbstractProtoMapper {

    private static final String COND_WRAPPER = "\\{|\\}";
    private static final String COND_SEPARATOR = "#";
    /** The Constant PATH_SEPARATOR. */
    private static final String PATH_SEPARATOR = "/";

    /**
     * Process multi path.
     *
     * @param proto
     *            the proto
     * @param paths
     *            the paths
     * @return the list
     */
    protected List<Object> processMultiPath(final GeneratedMessage proto, final List<String> paths, final Type type) {
        final List<Object> response = Lists.newArrayList();
        for (final String pathElements : paths) {
            response.addAll(processPath(proto, pathElements, type));
        }
        return response;
    }

    /**
     * Process path.
     *
     * @param proto
     *            the proto
     * @param path
     *            the path
     * @return the list
     */
    protected List<Object> processPath(final GeneratedMessage proto, final String path, final Type type) {
        return processPath(proto, Lists.newLinkedList(Splitter.on(PATH_SEPARATOR).trimResults().split(path)), type);
    }

    /**
     * Process path.
     *
     * @param proto
     *            the proto
     * @param pathElements
     *            the list
     * @return the list
     */
    protected List<Object> processPath(final GeneratedMessage proto, final List<String> pathElements, final Type type) {

        final List<Object> response = Lists.newArrayList();

        if (pathElements.isEmpty()) throw new RuntimeException("ProtoBuf navigation path is empty");

        final String fieldPathCond = pathElements.get(0);

        final String fieldPath = StringUtils.substringBefore(fieldPathCond, "[");
        final String cond = getCondition(fieldPathCond);

        final FieldDescriptor fd = proto.getDescriptorForType().findFieldByName(fieldPath);
        if ((fd != null)) {
            if (fd.isRepeated()) {
                final int count = proto.getRepeatedFieldCount(fd);
                for (int i = 0; i < count; i++) {
                    final Object field = proto.getRepeatedField(fd, i);
                    response.addAll(generateFields(fd, field, pathElements, cond, type));
                }
            } else {
                final Object field = proto.getField(fd);
                response.addAll(generateFields(fd, field, pathElements, cond, type));
            }
        } else throw new IllegalArgumentException("Invalid protobuf path (field not found): " + StringUtils.join(pathElements, ">") + "\nMessage:\n" + proto);

        return response;
    }

    /**
     * Generate fields.
     *
     * @param fd
     *            the fd
     * @param field
     *            the field
     * @param list
     *            the list
     * @return the list
     */
    private List<Object> generateFields(final FieldDescriptor fd, final Object field, final List<String> list, final String cond, final Type type) {

        final List<Object> res = Lists.newArrayList();
        if (field instanceof GeneratedMessage) {
            if (list.size() > 1) {

                if (StringUtils.isBlank(cond)) return processPath((GeneratedMessage) field, list.subList(1, list.size()), type);
                else {

                    final List<String> condPath =
                            Lists.newLinkedList(Splitter.on(COND_SEPARATOR).trimResults().split(StringUtils.substringBefore(cond, "=")));

                    final String val = (String) Iterables.getOnlyElement(processPath((GeneratedMessage) field, condPath, type));
                    final String condVal = StringUtils.substringAfter(cond, "=").replaceAll(COND_WRAPPER, "").trim();

                    return val.equals(condVal) ? processPath((GeneratedMessage) field, list.subList(1, list.size()), type) : res;
                }
            }
            else if (Type.JSON.equals(type)) {
                res.add(JsonFormat.printToString((Message) field));
                return res;
            } else throw new RuntimeException("No primitive type found");
        } else {
            if (list.size() == 1) {

                switch (fd.getType()) {
                    case ENUM:
                        res.add(((EnumValueDescriptor) field).getName());
                        break;
                    default:
                        res.add(field);
                        break;
                }
                return res;
            }
            else throw new RuntimeException("Found a primitive type before the path end");
        }
    }

    private String getCondition(final String fieldPathCond) {
        return fieldPathCond.contains("[") ? StringUtils.substringAfter(fieldPathCond, "[").replace("]", "") : "";
    }
}