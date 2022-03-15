package eu.dnetlib.pace.common;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.dnetlib.pace.clustering.NGramUtils;
import eu.dnetlib.pace.config.Type;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldList;
import eu.dnetlib.pace.model.FieldListImpl;
import eu.dnetlib.pace.model.FieldValueImpl;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.ibm.icu.text.Transliterator;

/**
 * Set of common functions for the framework
 *
 * @author claudio
 */
public abstract class AbstractPaceFunctions {

    //city map to be used when translating the city names into codes
    private static Map<String, String> cityMap = AbstractPaceFunctions.loadMapFromClasspath("/eu/dnetlib/pace/config/city_map.csv");

    //list of stopwords in different languages
    protected static Set<String> stopwords_gr = loadFromClasspath("/eu/dnetlib/pace/config/stopwords_gr.txt");
    protected static Set<String> stopwords_en = loadFromClasspath("/eu/dnetlib/pace/config/stopwords_en.txt");
    protected static Set<String> stopwords_de = loadFromClasspath("/eu/dnetlib/pace/config/stopwords_de.txt");
    protected static Set<String> stopwords_es = loadFromClasspath("/eu/dnetlib/pace/config/stopwords_es.txt");
    protected static Set<String> stopwords_fr = loadFromClasspath("/eu/dnetlib/pace/config/stopwords_fr.txt");
    protected static Set<String> stopwords_it = loadFromClasspath("/eu/dnetlib/pace/config/stopwords_it.txt");
    protected static Set<String> stopwords_pt = loadFromClasspath("/eu/dnetlib/pace/config/stopwords_pt.txt");

    //transliterator
    protected static Transliterator transliterator = Transliterator.getInstance("Any-Eng");

    //blacklist of ngrams: to avoid generic keys
    protected static Set<String> ngramBlacklist = loadFromClasspath("/eu/dnetlib/pace/config/ngram_blacklist.txt");

    //html regex for normalization
    public final String HTML_REGEX = "<[^>]*>";

    private static final String alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ";
    private static final String aliases_from = "⁰¹²³⁴⁵⁶⁷⁸⁹⁺⁻⁼⁽⁾ⁿ₀₁₂₃₄₅₆₇₈₉₊₋₌₍₎àáâäæãåāèéêëēėęəîïíīįìôöòóœøōõûüùúūßśšłžźżçćčñń";
    private static final String aliases_to = "0123456789+-=()n0123456789+-=()aaaaaaaaeeeeeeeeiiiiiioooooooouuuuussslzzzcccnn";

    //doi prefix for normalization
    public final String DOI_PREFIX = "(https?:\\/\\/dx\\.doi\\.org\\/)|(doi:)";

    private Pattern numberPattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    private Pattern hexUnicodePattern = Pattern.compile("\\\\u(\\p{XDigit}{4})");

    protected final static FieldList EMPTY_FIELD = new FieldListImpl();

    protected String concat(final List<String> l) {
        return Joiner.on(" ").skipNulls().join(l);
    }

    protected String cleanup(final String s) {

        final String s1 = s.replaceAll(HTML_REGEX, "");
        final String s2 = unicodeNormalization(s1.toLowerCase());
        final String s3 = nfd(s2);
        final String s4 = fixXML(s3);
        final String s5 = s4.replaceAll("([0-9]+)", " $1 ");
        final String s6 = transliterate(s5);
        final String s7 = fixAliases(s6);
        final String s8 = s7.replaceAll("[^\\p{ASCII}]", "");
        final String s9 = s8.replaceAll("[\\p{Punct}]", " ");
        final String s10 = s9.replaceAll("\\n", " ");
        final String s11 = s10.replaceAll("(?m)\\s+", " ");
        final String s12 = s11.trim();
        return s12;
    }

    protected String fixXML(final String a){

        return a.replaceAll("&ndash;", " ")
                .replaceAll("&amp;", " ")
                .replaceAll("&quot;", " ")
                .replaceAll("&minus;", " ");
    }

    protected boolean checkNumbers(final String a, final String b) {
        final String numbersA = getNumbers(a);
        final String numbersB = getNumbers(b);
        final String romansA = getRomans(a);
        final String romansB = getRomans(b);
        return !numbersA.equals(numbersB) || !romansA.equals(romansB);
    }

    protected String getRomans(final String s) {
        final StringBuilder sb = new StringBuilder();
        for (final String t : s.split(" ")) {
            sb.append(isRoman(t) ? t : "");
        }
        return sb.toString();
    }

    protected boolean isRoman(final String s) {
        return s.replaceAll("^M{0,4}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$", "qwertyuiop").equals("qwertyuiop");
    }

    protected String getNumbers(final String s) {
        final StringBuilder sb = new StringBuilder();
        for (final String t : s.split(" ")) {
            sb.append(isNumber(t) ? t : "");
        }
        return sb.toString();
    }

    public boolean isNumber(String strNum) {
        if (strNum == null) {
            return false;
        }
        return numberPattern.matcher(strNum).matches();
    }

    protected static String fixAliases(final String s) {
        final StringBuilder sb = new StringBuilder();
        for (final char ch : Lists.charactersOf(s)) {
            final int i = StringUtils.indexOf(aliases_from, ch);
            sb.append(i >= 0 ? aliases_to.charAt(i) : ch);
        }
        return sb.toString();
    }

    protected static String transliterate(final String s) {
        try {
            return transliterator.transliterate(s);
        }
        catch(Exception e) {
            return s;
        }
    }

    protected String removeSymbols(final String s) {
        final StringBuilder sb = new StringBuilder();

        for (final char ch : Lists.charactersOf(s)) {
            sb.append(StringUtils.contains(alpha, ch) ? ch : " ");
        }
        return sb.toString().replaceAll("\\s+", " ");
    }

    protected String getFirstValue(final Field values) {
        return (values != null) && !Iterables.isEmpty(values) ? Iterables.getFirst(values, EMPTY_FIELD).stringValue() : "";
    }

    protected boolean notNull(final String s) {
        return s != null;
    }

    protected String normalize(final String s) {
        return fixAliases(transliterate(nfd(unicodeNormalization(s))))
                .toLowerCase()
                // do not compact the regexes in a single expression, would cause StackOverflowError in case of large input strings
                .replaceAll("[^ \\w]+", "")
                .replaceAll("(\\p{InCombiningDiacriticalMarks})+", "")
                .replaceAll("(\\p{Punct})+", " ")
                .replaceAll("(\\d)+", " ")
                .replaceAll("(\\n)+", " ")
                .trim();
    }

    public String nfd(final String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD);
    }

    public String utf8(final String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public String unicodeNormalization(final String s) {

        Matcher m = hexUnicodePattern.matcher(s);
        StringBuffer buf = new StringBuffer(s.length());
        while (m.find()) {
            String ch = String.valueOf((char) Integer.parseInt(m.group(1), 16));
            m.appendReplacement(buf, Matcher.quoteReplacement(ch));
        }
        m.appendTail(buf);
        return buf.toString();
    }

    protected String filterStopWords(final String s, final Set<String> stopwords) {
        final StringTokenizer st = new StringTokenizer(s);
        final StringBuilder sb = new StringBuilder();
        while (st.hasMoreTokens()) {
            final String token = st.nextToken();
            if (!stopwords.contains(token)) {
                sb.append(token);
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    public String filterAllStopWords(String s) {

        s = filterStopWords(s, stopwords_en);
        s = filterStopWords(s, stopwords_de);
        s = filterStopWords(s, stopwords_it);
        s = filterStopWords(s, stopwords_fr);
        s = filterStopWords(s, stopwords_pt);
        s = filterStopWords(s, stopwords_es);
        s = filterStopWords(s, stopwords_gr);

        return s;
    }

    protected Collection<String> filterBlacklisted(final Collection<String> set, final Set<String> ngramBlacklist) {
        final Set<String> newset = Sets.newLinkedHashSet();
        for (final String s : set) {
            if (!ngramBlacklist.contains(s)) {
                newset.add(s);
            }
        }
        return newset;
    }

    public static Set<String> loadFromClasspath(final String classpath) {

        Transliterator transliterator = Transliterator.getInstance("Any-Eng");

        final Set<String> h = Sets.newHashSet();
        try {
            for (final String s : IOUtils.readLines(NGramUtils.class.getResourceAsStream(classpath))) {
                h.add(fixAliases(transliterator.transliterate(s))); //transliteration of the stopwords
            }
        } catch (final Throwable e) {
            return Sets.newHashSet();
        }
        return h;
    }

    public static Map<String, String> loadMapFromClasspath(final String classpath) {

        Transliterator transliterator = Transliterator.getInstance("Any-Eng");

        final Map<String, String> m = new HashMap<>();
        try {
            for (final String s : IOUtils.readLines(AbstractPaceFunctions.class.getResourceAsStream(classpath))) {
                //string is like this: code;word1;word2;word3
                String[] line = s.split(";");
                String value = line[0];
                for (int i = 1; i < line.length; i++) {
                    m.put(fixAliases(transliterator.transliterate(line[i].toLowerCase())), value);
                }
            }
        } catch (final Throwable e) {
            return new HashMap<>();
        }
        return m;
    }

    public String removeKeywords(String s, Set<String> keywords) {

        s = " " + s + " ";
        for (String k : keywords) {
            s = s.replaceAll(k.toLowerCase(), "");
        }

        return s.trim();
    }

    public double commonElementsPercentage(Set<String> s1, Set<String> s2) {

        double longer = Math.max(s1.size(), s2.size());
        return (double) s1.stream().filter(s2::contains).count() / longer;
    }

    //convert the set of keywords to codes
    public Set<String> toCodes(Set<String> keywords, Map<String, String> translationMap) {
        return keywords.stream().map(s -> translationMap.get(s)).collect(Collectors.toSet());
    }

    public Set<String> keywordsToCodes(Set<String> keywords, Map<String, String> translationMap) {
        return toCodes(keywords, translationMap);
    }

    public Set<String> citiesToCodes(Set<String> keywords) {
        return toCodes(keywords, cityMap);
    }

    protected String firstLC(final String s) {
        return StringUtils.substring(s, 0, 1).toLowerCase();
    }

    protected Iterable<String> tokens(final String s, final int maxTokens) {
        return Iterables.limit(Splitter.on(" ").omitEmptyStrings().trimResults().split(s), maxTokens);
    }

    public String normalizePid(String pid) {
        return pid.toLowerCase().replaceAll(DOI_PREFIX, "");
    }

    //get the list of keywords into the input string
    public Set<String> getKeywords(String s1, Map<String, String> translationMap, int windowSize) {

        String s = s1;

        List<String> tokens = Arrays.asList(s.toLowerCase().split(" "));

        Set<String> codes = new HashSet<>();

        if (tokens.size() < windowSize)
            windowSize = tokens.size();

        int length = windowSize;

        while (length != 0) {

            for (int i = 0; i <= tokens.size() - length; i++) {
                String candidate = concat(tokens.subList(i, i + length));
                if (translationMap.containsKey(candidate)) {
                    codes.add(candidate);
                    s = s.replace(candidate, "").trim();
                }
            }

            tokens = Arrays.asList(s.split(" "));
            length -= 1;
        }

        return codes;
    }

    public Set<String> getCities(String s1, int windowSize) {
        return getKeywords(s1, cityMap, windowSize);
    }

    public static <T> String readFromClasspath(final String filename, final Class<T> clazz) {
        final StringWriter sw = new StringWriter();
        try {
            IOUtils.copy(clazz.getResourceAsStream(filename), sw);
            return sw.toString();
        } catch (final IOException e) {
            throw new RuntimeException("cannot load resource from classpath: " + filename);
        }
    }

}
