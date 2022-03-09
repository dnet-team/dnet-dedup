package eu.dnetlib.pace.tree;

import com.google.common.collect.Iterables;
import eu.dnetlib.pace.config.Config;
import eu.dnetlib.pace.model.Field;
import eu.dnetlib.pace.model.FieldList;
import eu.dnetlib.pace.model.Person;
import eu.dnetlib.pace.tree.support.AbstractComparator;
import eu.dnetlib.pace.tree.support.ComparatorClass;
import com.wcohen.ss.AbstractStringDistance;

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
    private int SIZE_THRESHOLD;
    private int common;

    public AuthorsMatch(Map<String, String> params){
        super(params, new com.wcohen.ss.JaroWinkler());
        this.params = params;

        MODE = params.getOrDefault("mode", "full");
        SURNAME_THRESHOLD = Double.parseDouble(params.getOrDefault("surname_th", "0.95"));
        NAME_THRESHOLD = Double.parseDouble(params.getOrDefault("name_th", "0.95"));
        FULLNAME_THRESHOLD = Double.parseDouble(params.getOrDefault("fullname_th", "0.9"));
        SIZE_THRESHOLD = Integer.parseInt(params.getOrDefault("size_th", "20"));
        common = 0;
    }

    protected AuthorsMatch(double w, AbstractStringDistance ssalgo) {
        super(w, ssalgo);
    }

    @Override
    public double compare(final Field a, final Field b, final Config conf) {

        if (a.isEmpty() || b.isEmpty())
            return -1;

        if (((FieldList) a).size() > SIZE_THRESHOLD || ((FieldList) a).size() > SIZE_THRESHOLD)
            return 1.0;

        List<Person> aList = ((FieldList) a).stringList().stream().map(author -> new Person(author, false)).collect(Collectors.toList());
        List<Person> bList = ((FieldList) b).stringList().stream().map(author -> new Person(author, false)).collect(Collectors.toList());

        common = 0;
        //compare each element of List1 with each element of List2
        for (Person p1 : aList)

            for (Person p2 : bList) {

                //both persons are inaccurate
                if (!p1.isAccurate() && !p2.isAccurate()) {
                    //compare just normalized fullnames
                    String fullname1 = normalization(p1.getNormalisedFullname().isEmpty()? p1.getOriginal() : p1.getNormalisedFullname());
                    String fullname2 = normalization(p2.getNormalisedFullname().isEmpty()? p2.getOriginal() : p2.getNormalisedFullname());

                    if (ssalgo.score(fullname1, fullname2) > FULLNAME_THRESHOLD) {
                        common += 1;
                        break;
                    }
                }

                //one person is inaccurate
                if (p1.isAccurate() ^ p2.isAccurate()) {
                    //prepare data
                    //data for the accurate person
                    String name = normalization(p1.isAccurate()? p1.getNormalisedFirstName() : p2.getNormalisedFirstName());
                    String surname = normalization(p1.isAccurate()? p1.getNormalisedSurname() : p2.getNormalisedSurname());

                    //data for the inaccurate person
                    String fullname = normalization(
                            p1.isAccurate() ? ((p2.getNormalisedFullname().isEmpty()) ? p2.getOriginal() : p2.getNormalisedFullname()) : (p1.getNormalisedFullname().isEmpty() ? p1.getOriginal() : p1.getNormalisedFullname())
                    );

                    if (fullname.contains(surname)) {
                        if (MODE.equals("full")) {
                            if (fullname.contains(name)) {
                                common += 1;
                                break;
                            }
                        }
                        else { //MODE equals "surname"
                            common += 1;
                            break;
                        }
                    }
                }

                //both persons are accurate
                if (p1.isAccurate() && p2.isAccurate()) {

                    if (compareSurname(p1, p2)) {
                        if (MODE.equals("full")) {
                            if(compareFirstname(p1, p2)) {
                                common += 1;
                                break;
                            }
                        }
                        else { //MODE equals "surname"
                            common += 1;
                            break;
                        }
                    }

                }

            }

        //normalization factor to compute the score
        int normFactor = aList.size() == bList.size() ? aList.size() : (aList.size() + bList.size() - common);

        return (double)common / normFactor;
    }

    public boolean compareSurname(Person p1, Person p2) {
        return ssalgo.score(normalization(p1.getNormalisedSurname()), normalization(p2.getNormalisedSurname())) > SURNAME_THRESHOLD;
    }

    public boolean compareFirstname(Person p1, Person p2) {

        if(p1.getNormalisedFirstName().length()<=2 || p2.getNormalisedFirstName().length()<=2) {
            if (firstLC(p1.getNormalisedFirstName()).equals(firstLC(p2.getNormalisedFirstName())))
                return true;
        }

        return ssalgo.score(normalization(p1.getNormalisedFirstName()), normalization(p2.getNormalisedFirstName())) > NAME_THRESHOLD;
    }

    public String normalization(String s) {
        return normalize(utf8(cleanup(s)));
    }

}
