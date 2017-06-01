package cn.yyx.research.program.analysis.fulltrace.storage.helper;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.analysis.fulltrace.storage.node.DynamicNode;

public class MapSetUtil {
	
	public static void HandleTwoMapSet(Map<IJavaElement, Set<DynamicNode>> merge_main, Map<IJavaElement, Set<DynamicNode>> to_merge) {
		Set<IJavaElement> tkeys = to_merge.keySet();
		Iterator<IJavaElement> titr = tkeys.iterator();
		while (titr.hasNext()) {
			IJavaElement ije = titr.next();
			Set<DynamicNode> to_merge_set = to_merge.get(ije);
			Set<DynamicNode> merge_main_set = merge_main.get(ije);
			if (merge_main_set == null) {
				merge_main_set = new HashSet<DynamicNode>();
			}
			merge_main_set.addAll(to_merge_set);
			merge_main.put(ije, merge_main_set);
		}
	}
	
}
