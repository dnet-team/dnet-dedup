package eu.dnetlib.pace.clustering;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import eu.dnetlib.pace.common.AbstractPaceFunctions;

public class NGramUtils extends AbstractPaceFunctions {

	private static final int SIZE = 100;

	private static Set<String> stopwords = AbstractPaceFunctions.loadFromClasspath("/eu/dnetlib/pace/config/stopwords_en.txt");

	public static String cleanupForOrdering(String s) {
		NGramUtils utils = new NGramUtils();
		return (utils.filterStopWords(utils.normalize(s), stopwords) +  StringUtils.repeat(" ", SIZE)).substring(0, SIZE).replaceAll(" ", "");
	}

}
