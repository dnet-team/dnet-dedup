package eu.dnetlib.pace.clustering;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import eu.dnetlib.pace.config.Config;

@ClusteringClass("wordssuffixprefix")
public class WordsSuffixPrefix extends AbstractClusteringFunction {

    public WordsSuffixPrefix(Map<String, Integer> params) {
        super(params);
    }

    @Override
    protected Collection<String> doApply(Config conf, String s) {
        return suffixPrefix(s, param("len"), param("max"));
    }

    private Collection<String> suffixPrefix(String s, int len, int max) {

        final int words = s.split(" ").length;

        // adjust the token length according to the number of words
        switch (words) {
            case 1:
                return Sets.newLinkedHashSet();
            case 2:
                return doSuffixPrefix(s, len+2, max, words);
            case 3:
                return doSuffixPrefix(s, len+1, max, words);
            default:
                return doSuffixPrefix(s, len, max, words);
        }
    }

    private Collection<String> doSuffixPrefix(String s, int len, int max, int words) {
        final Set<String> bigrams = Sets.newLinkedHashSet();
        int i = 0;
        while (++i < s.length() && bigrams.size() < max) {
            int j = s.indexOf(" ", i);

            int offset = j + len + 1 < s.length() ? j + len + 1 : s.length();

            if (j - len > 0) {
                String bigram = s.substring(j - len, offset).replaceAll(" ", "").trim();
                if (bigram.length() >= 4) {
                    bigrams.add(words+bigram);
                }
            }
        }
        return bigrams;
    }

}