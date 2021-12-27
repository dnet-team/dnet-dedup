package eu.dnetlib.pace.tree;

import com.google.common.collect.Iterables;
import com.wcohen.ss.JaroWinkler;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldList;
import eu.dnetlib.pace.model.Person;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ComparatorClass("authorsMatch")
public class AuthorsMatch extends AbstractComparator {

    Map<String, String> params;

    private double SURNAME_THRESHOLD;
    private double NAME_THRESHOLD;
    private double FULLNAME_THRESHOLD;
    private String MODE; //full or surname

    public AuthorsMatch(Map<String, String> params){
        super(params, new com.wcohen.ss.JaroWinkler());
        this.params = params;

        MODE = params.getOrDefault("mode", "full");
        SURNAME_THRESHOLD = Double.parseDouble(params.getOrDefault("surname_th", "0.95"));
        NAME_THRESHOLD = Double.parseDouble(params.getOrDefault("name_th", "0.95"));
        FULLNAME_THRESHOLD = Double.parseDouble(params.getOrDefault("fullname_th", "0.9"));
    }

    @Override
    public double compare(final Field a, final Field b, final Config conf) {

        if (a.isEmpty() || b.isEmpty())
            return -1;

        List<Person> aList = ((FieldList) a).stringList().stream().map(author -> new Person(author, false)).collect(Collectors.toList());
        List<Person> bList = ((FieldList) b).stringList().stream().map(author -> new Person(author, false)).collect(Collectors.toList());

        int common = 0;
        for (Person p1 : aList)
            for (Person p2 : bList)
                if(MODE.equals("full")) {
                    if (personComparator(p1, p2))
                        common += 1;
                }
                else {
                    if (surnameComparator(p1, p2))
                        common += 1;
                }

        return (double)common / (aList.size() + bList.size() - common);
    }

    public boolean personComparator(Person p1, Person p2) {

        if(!p1.isAccurate() || !p2.isAccurate())
            return ssalgo.score(p1.getOriginal(), p2.getOriginal()) > FULLNAME_THRESHOLD;

        if(ssalgo.score(p1.getSurnameString(),p2.getSurnameString()) > SURNAME_THRESHOLD)
            if(p1.getNameString().length()<=2 || p2.getNameString().length()<=2)
                return firstLC(p1.getNameString()).equals(firstLC(p2.getNameString()));
            else
                return ssalgo.score(p1.getNameString(), p2.getNameString()) > NAME_THRESHOLD;
        else
            return false;
    }

    public boolean surnameComparator(Person p1, Person p2) {

        if(!p1.isAccurate() || !p2.isAccurate())
            return ssalgo.score(p1.getOriginal(), p2.getOriginal()) > FULLNAME_THRESHOLD;

        return ssalgo.score(p1.getSurnameString(), p2.getSurnameString()) > SURNAME_THRESHOLD;
    }

}
