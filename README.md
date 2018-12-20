## Dnet-Dedup

# Decision Tree for authors deduplication

The decision tree has to be defined into the json configuration. The field decisionTree of the JSON contains a map organized as follow:

<String nodeName, TreeNodeDef treeNodeDef>: the nodeName is the key, the treeNodeDef contains the definition of the node.

In particular the TreeNodeDef contains:
 - List<FieldConf> : list of fields processed by the node. Each field is associated to:
	 - field: name of the field
	 - comparator: name of the comparator to use for that particular field, it produces a similarity score, -1 if the comparison is not possible (missing field or few informations)
	 *Each FieldConf contains a comparator name which has to be defined. It is sufficient to implement the Comparator interface that exposes a "compare" method returning the similarity score. The new comparator must be annotated with @ComparatorClass("name") specifying the name used by the FieldConf to access to the right comparator.*
	 - weight: weight to assign to the similarity score of that comparator when aggregating
	 - params: list of parameters for the comparator
 - threshold: this threshold is applied to the resulting similarity score of the particular treeNode.
```
if score>=th --- positive result
if score==-1 --- undefined result
if score<\th  --- negative result
```
 - aggregation: defines the type of aggregation to apply to the similarity scores of the fields in the list of fields
	 - possible values: AVG(average), MAX, MIN, SUM
	e.g. the similarity scores are multiplied with the weight and then the defined aggregation is applied
 - arcs: define the next node of the tree depending on the result
		positive: specifies the key of the next node in case of positive result
		negative: specifies the key of the next node in case of negative result
		undefined: specifies the key of the next node in case of undefined result
 - ignoreMissing: defines the behavior of the treeNode in case of a missing field
		e.g. if a comparator on a particular field produces an undefined result (-1), if ignoreMissing=true that field is simply ignored, otherwise the entire treeNode score is considered to be -1

In order to make the decision tree work, the BlockProcessor has been modified with the following changes:
 - if the decision tree is defined into the JSON configuration the deduplication process relies on it
 - if the decision tree is not defined the deduplication process is exactly like before (strict conditions, conditions, dinstance algos etc.)


# Cities and Keyword identification for organization deduplication

A new comparator (JaroWinklerNormalizedName) has been implemented for the deduplication of the organizations. This comparator identifies keywords and cities on the organization name and substitute them with particular codes.
To this aim, two different translation maps have been defined:
 - translation_map.csv: contains keywords codes and the keyword in ~10 different languages
 - city_map.csv: contains cities codes and city names in many different languages
This csv files are placed into a map like that: <translation, code>.

The JaroWinklerNormalizedName comparator search for the keyword and the city name into the organization name, substitutes them with the code and then applies the JaroWinkler similarity function on the resulting strings removing identified codes.
The process to determine if two organization names are equal is the following:
```
        if (sameCity(ca,cb)){
           if (sameKeywords(ca,cb)){
               ca = removeCodes(ca);
               cb = removeCodes(cb);
               if (ca.isEmpty() && cb.isEmpty())
                   return 1.0;
               else
                   return normalize(ssalgo.score(ca,cb));
           }
        }
```

For the keyword replacement the process is simple: it is sufficient to divide the string into tokens (1 token=1 word) and search for that word into the translation map/
For the city name replacement the process is way more complicated: since we cannot know if the name of a particular city is composed by one or more words, we need to extract all the candidate names from the organization name.
The candidate city names are extracted basing on a window size (this is to limit the number of token extracted). All the candidates are composed by 4 or less adiacent words.
```
Example:
window = 4
organization name = University of technologies of New York
cleaned organization name (without stopwords and lowercased): university technologies new york
candidates = "university technologies new york", "university technologies new", "technologies new york", "university technologies", "technologies new", "new york", "university", "technologies", "new", "york"
```
These candidate names are searched into the city map starting from the longest until a name is found. When the name is present into the map, it is replaced with the city code.

powered by D-Net
