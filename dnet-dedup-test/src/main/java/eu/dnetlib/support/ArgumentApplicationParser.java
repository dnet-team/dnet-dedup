package eu.dnetlib.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ArgumentApplicationParser implements Serializable {

    private final Options options = new Options();
    private final Map<String, String> objectMap = new HashMap<>();

    private final List<String> compressedValues = new ArrayList<>();

    public ArgumentApplicationParser(final String json_configuration) throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        final OptionsParameter[] configuration = mapper.readValue(json_configuration, OptionsParameter[].class);
        createOptionMap(configuration);
    }

    public ArgumentApplicationParser(final OptionsParameter[] configuration) {
        createOptionMap(configuration);
    }

    private void createOptionMap(final OptionsParameter[] configuration) {

        Arrays
                .stream(configuration)
                .map(
                        conf -> {
                            final Option o = new Option(conf.getParamName(), true, conf.getParamDescription());
                            o.setLongOpt(conf.getParamLongName());
                            o.setRequired(conf.isParamRequired());
                            if (conf.isCompressed()) {
                                compressedValues.add(conf.getParamLongName());
                            }
                            return o;
                        })
                .forEach(options::addOption);

        // HelpFormatter formatter = new HelpFormatter();
        // formatter.printHelp("myapp", null, options, null, true);

    }

    public static String decompressValue(final String abstractCompressed) {
        try {
            byte[] byteArray = org.apache.commons.codec.binary.Base64.decodeBase64(abstractCompressed.getBytes());
            GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(byteArray));
            final StringWriter stringWriter = new StringWriter();
            IOUtils.copy(gis, stringWriter);
            return stringWriter.toString();
        } catch (Throwable e) {
            System.out.println("Wrong value to decompress:" + abstractCompressed);
            throw new RuntimeException(e);
        }
    }

    public static String compressArgument(final String value) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(value.getBytes());
        gzip.close();
        return java.util.Base64.getEncoder().encodeToString(out.toByteArray());
    }

    public void parseArgument(final String[] args) throws Exception {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args);
        Arrays
                .stream(cmd.getOptions())
                .forEach(
                        it -> objectMap
                                .put(
                                        it.getLongOpt(),
                                        compressedValues.contains(it.getLongOpt())
                                                ? decompressValue(it.getValue())
                                                : it.getValue()));
    }

    public String get(final String key) {
        return objectMap.get(key);
    }

    public Map<String, String> getObjectMap() {
        return objectMap;
    }
}
