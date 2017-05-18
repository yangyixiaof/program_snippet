package cn.yyx.research.jdkutil;

import java.util.Iterator;
import java.util.List;

public class ListCompare {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean TwoListEqual(List list1, List list2) {
		if (list1.size() != list2.size()) {
			return false;
		}
		Iterator<Object> l1itr = list1.iterator();
		Iterator<Object> l2itr = list2.iterator();
		while (l1itr.hasNext()) {
			Object obj1 = l1itr.next();
			Object obj2 = l2itr.next();
			if (!(obj1.equals(obj2))) {
				return false;
			}
		}
		return true;
	}
	
}
