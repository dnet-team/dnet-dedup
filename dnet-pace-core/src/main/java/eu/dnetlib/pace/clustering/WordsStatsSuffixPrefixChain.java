package eu.dnetlib.pace.clustering;

import com.google.common.collect.Sets;
import eu.dnetlib.pace.config.Config;

import java.util.*;
import java.util.stream.Collectors;

@ClusteringClass("wordsStatsSuffixPrefixChain")
public class WordsStatsSuffixPrefixChain extends AbstractClusteringFunction {

        public WordsStatsSuffixPrefixChain(Map<String, Integer> params) {
            super(params);
        }

        @Override
        protected Collection<String> doApply(Config conf, String s) {
            return suffixPrefixChain(s, param("mod"));
        }

        private Collection<String> suffixPrefixChain(String s, int mod) {

            //create the list of words from the string (remove short words)
            List<String> wordsList =
                    Arrays.stream(s.split(" "))
                    .filter(si -> si.length() > 3)
                    .collect(Collectors.toList());

            final int words = wordsList.size();
            final int letters = s.length();

            //create the prefix: number of words + number of letters/mod
            String prefix = words + "-" + letters/mod + "-";

            return doSuffixPrefixChain(wordsList, prefix);

        }

        private Collection<String> doSuffixPrefixChain(List<String> wordsList, String prefix) {

            Set<String> set = Sets.newLinkedHashSet();
            switch(wordsList.size()){
                case 0:
                case 1:
                    break;
                case 2:
                    set.add(
                            prefix +
                            suffix(wordsList.get(0), 3) +
                            prefix(wordsList.get(1), 3)
                    );

                    set.add(
                            prefix +
                            prefix(wordsList.get(0), 3) +
                            suffix(wordsList.get(1), 3)
                    );

                    break;
                default:
                    set.add(
                            prefix +
                            suffix(wordsList.get(0), 3) +
                            prefix(wordsList.get(1), 3) +
                            suffix(wordsList.get(2), 3)
                    );

                    set.add(
                            prefix +
                            prefix(wordsList.get(0), 3) +
                            suffix(wordsList.get(1), 3) +
                            prefix(wordsList.get(2), 3)
                    );
                    break;
            }

            return set;

        }


        private String suffix(String s, int len) {
            return s.substring(s.length()-len);
        }

        private String prefix(String s, int len) {
            return s.substring(0, len);
        }

}
