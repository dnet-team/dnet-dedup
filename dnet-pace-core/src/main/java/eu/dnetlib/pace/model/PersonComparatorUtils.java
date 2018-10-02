package eu.dnetlib.pace.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class PersonComparatorUtils {

	private static final int MAX_FULLNAME_LENGTH = 50;

	public static Set<String> getNgramsForPerson(String fullname) {

		Set<String> set = Sets.newHashSet();

		if (fullname.length() > MAX_FULLNAME_LENGTH) {
			return set;
		}

		Person p = new Person(fullname, true);

		if (p.isAccurate()) {
			for (String name : p.getName()) {
				for (String surname : p.getSurname()) {
					set.add((name.charAt(0) + "_" + surname).toLowerCase());
				}
			}
		} else {
			List<String> list = p.getFullname();
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).length() > 1) {
					for (int j = 0; j < list.size(); j++) {
						if (i != j) {
							set.add((list.get(j).charAt(0) + "_" + list.get(i)).toLowerCase());
						}
					}
				}
			}
		}

		return set;
	}

	public static boolean areSimilar(String s1, String s2) {
		Person p1 = new Person(s1, true);
		Person p2 = new Person(s2, true);

		if (p1.isAccurate() && p2.isAccurate()) {
			return verifyNames(p1.getName(), p2.getName()) && verifySurnames(p1.getSurname(), p2.getSurname());
		} else {
			return verifyFullnames(p1.getFullname(), p2.getFullname());
		}
	}

	private static boolean verifyNames(List<String> list1, List<String> list2) {
		return verifySimilarity(extractExtendedNames(list1), extractExtendedNames(list2))
				&& verifySimilarity(extractInitials(list1), extractInitials(list2));
	}

	private static boolean verifySurnames(List<String> list1, List<String> list2) {
		if (list1.size() != list2.size()) {
			return false;
		}
		for (int i = 0; i < list1.size(); i++) {
			if (!list1.get(i).equalsIgnoreCase(list2.get(i))) {
				return false;
			}
		}
		return true;
	}

	private static boolean verifyFullnames(List<String> list1, List<String> list2) {
		Collections.sort(list1);
		Collections.sort(list2);
		return verifySimilarity(extractExtendedNames(list1), extractExtendedNames(list2))
				&& verifySimilarity(extractInitials(list1), extractInitials(list2));
	}

	private static List<String> extractExtendedNames(List<String> list) {
		ArrayList<String> res = Lists.newArrayList();
		for (String s : list) {
			if (s.length() > 1) {
				res.add(s.toLowerCase());
			}
		}
		return res;
	}

	private static List<String> extractInitials(List<String> list) {
		ArrayList<String> res = Lists.newArrayList();
		for (String s : list) {
			res.add(s.substring(0, 1).toLowerCase());
		}
		return res;
	}

	private static boolean verifySimilarity(List<String> list1, List<String> list2) {
		if (list1.size() > list2.size()) {
			return verifySimilarity(list2, list1);
		}

		// NB: List2 is greater than list1 (or equal)
		int pos = -1;
		for (String s : list1) {
			int curr = list2.indexOf(s);
			if (curr > pos) {
				list2.set(curr, "*"); // I invalidate the found element, example: "amm - amm" 
				pos = curr;
			} else {
				return false;
			}
		}
		return true;
	}
}
