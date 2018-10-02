package eu.dnetlib.pace.utils;

import com.google.common.collect.Lists;
import com.googlecode.protobuf.format.JsonFormat;
import eu.dnetlib.data.proto.OafProtos;
import eu.dnetlib.data.proto.ResultProtos;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.config.DedupConfig;
import eu.dnetlib.pace.model.MapDocument;
import eu.dnetlib.pace.model.ProtoDocumentBuilder;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static eu.dnetlib.proto.utils.OAFProtoUtils.*;
import static eu.dnetlib.proto.utils.OAFProtoUtils.author;
import static eu.dnetlib.proto.utils.OAFProtoUtils.sp;

public class PaceUtils {

    public static MapDocument result(final Config config, final String id, final String title) {
        return result(config, id, title, null, new ArrayList<>(), null);
    }

    public static MapDocument result(final Config config, final String id, final String title, final String date) {
        return result(config, id, title, date, new ArrayList<>(), null);
    }

    public static MapDocument result(final Config config, final String id, final String title, final String date, final List<String> pid) {
        return result(config, id, title, date, pid, null);
    }

    public static MapDocument result(final Config config, final String id, final String title, final String date, final String pid) {
        return result(config, id, title, date, pid, null);
    }

    public static MapDocument result(final Config config, final String id, final String title, final String date, final String pid, final List<String> authors) {
        return result(config, id, title, date, Lists.newArrayList(pid), authors);
    }

    public static MapDocument result(final Config config, final String id, final String title, final String date, final List<String> pid, final List<String> authors) {
        final ResultProtos.Result.Metadata.Builder metadata = ResultProtos.Result.Metadata.newBuilder();
        if (!StringUtils.isBlank(title)) {
            metadata.addTitle(getStruct(title, getQualifier("main title", "dnet:titles")));
            metadata.addTitle(getStruct(RandomStringUtils.randomAlphabetic(10), getQualifier("alternative title", "dnet:titles")));
        }
        if (!StringUtils.isBlank(date)) {
            metadata.setDateofacceptance(sf(date));
        }

        final OafProtos.OafEntity.Builder entity = oafEntity(id, eu.dnetlib.data.proto.TypeProtos.Type.result);
        final ResultProtos.Result.Builder result = ResultProtos.Result.newBuilder().setMetadata(metadata);

        if (authors != null) {
            result.getMetadataBuilder().addAllAuthor(
                    IntStream.range(0, authors.size())
                            .mapToObj(i -> author(authors.get(i), i))
                            .collect(Collectors.toCollection(LinkedList::new)));
        }

        entity.setResult(result);

        if (pid != null) {
            for (String p : pid) {
                if (!StringUtils.isBlank(p)) {
                    entity.addPid(sp(p, "doi"));
                    //entity.addPid(sp(RandomStringUtils.randomAlphabetic(10), "oai"));
                }
            }
        }

        final OafProtos.OafEntity build = entity.build();
        return ProtoDocumentBuilder.newInstance(id, build, config.model());
    }

    public static MapDocument asMapDocument(DedupConfig conf, final String json) {
        OafProtos.OafEntity.Builder b = OafProtos.OafEntity.newBuilder();
        try {
            JsonFormat.merge(json, b);
        } catch (JsonFormat.ParseException e) {
            throw new IllegalArgumentException(e);
        }
        return ProtoDocumentBuilder.newInstance(b.getId(), b.build(), conf.getPace().getModel());
    }
}
