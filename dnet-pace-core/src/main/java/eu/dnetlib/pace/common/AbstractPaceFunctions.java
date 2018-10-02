package eu.dnetlib.pace.common;

import java.text.Normalizer;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.dnetlib.pace.clustering.NGramUtils;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldList;
import eu.dnetlib.pace.model.FieldListImpl;

/**
 * Set of common functions
 *
 * @author claudio
 *
 */
public abstract class AbstractPaceFunctions {

	protected static Set<String> stopwords = loadFromClasspath("/eu/dnetlib/pace/config/stopwords_en.txt");

	protected static Set<String> ngramBlacklist = loadFromClasspath("/eu/dnetlib/pace/config/ngram_blacklist.txt");

	private static final String alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ";
	private static final String aliases_from = "⁰¹²³⁴⁵⁶⁷⁸⁹⁺⁻⁼⁽⁾ⁿ₀₁₂₃₄₅₆₇₈₉₊₋₌₍₎";
	private static final String aliases_to = "0123456789+-=()n0123456789+-=()";

	protected final static FieldList EMPTY_FIELD = new FieldListImpl();

	protected String concat(final List<String> l) {
		return Joiner.on(" ").skipNulls().join(l);
	}

	protected String cleanup(final String s) {
		final String s1 = nfd(s);
		final String s2 = fixAliases(s1);
		final String s3 = s2.replaceAll("&ndash;", " ");
		final String s4 = s3.replaceAll("&amp;", " ");
		final String s5 = s4.replaceAll("&quot;", " ");
		final String s6 = s5.replaceAll("&minus;", " ");
		final String s7 = s6.replaceAll("([0-9]+)", " $1 ");
		final String s8 = s7.replaceAll("[^\\p{ASCII}]|\\p{Punct}", " ");
		final String s9 = s8.replaceAll("\\n", " ");
		final String s10 = s9.replaceAll("(?m)\\s+", " ");
		final String s11 = s10.trim();
		return s11;
	}

	protected String finalCleanup(final String s) {
		return s.toLowerCase();
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
		return s.replaceAll("\\D", "");
	}

	protected String fixAliases(final String s) {
		final StringBuilder sb = new StringBuilder();
		for (final char ch : Lists.charactersOf(s)) {
			final int i = StringUtils.indexOf(aliases_from, ch);
			sb.append(i >= 0 ? aliases_to.charAt(i) : ch);
		}
		return sb.toString();
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

	// ///////////////////////

	protected String normalize(final String s) {
		return nfd(s).toLowerCase()
				// do not compact the regexes in a single expression, would cause StackOverflowError in case of large input strings
				.replaceAll("(\\W)+", " ")
				.replaceAll("(\\p{InCombiningDiacriticalMarks})+", " ")
				.replaceAll("(\\p{Punct})+", " ")
				.replaceAll("(\\d)+", " ")
				.replaceAll("(\\n)+", " ")
				.trim();
	}

	private String nfd(final String s) {
		return Normalizer.normalize(s, Normalizer.Form.NFD);
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

	protected Collection<String> filterBlacklisted(final Collection<String> set, final Set<String> ngramBlacklist) {
		final Set<String> newset = Sets.newLinkedHashSet();
		for (final String s : set) {
			if (!ngramBlacklist.contains(s)) {
				newset.add(s);
			}
		}
		return newset;
	}

	// ////////////////////

	public static Set<String> loadFromClasspath(final String classpath) {
		final Set<String> h = Sets.newHashSet();
		try {
			for (final String s : IOUtils.readLines(NGramUtils.class.getResourceAsStream(classpath))) {
				h.add(s);
			}
		} catch (final Throwable e) {
			return Sets.newHashSet();
		}
		return h;
	}

}
