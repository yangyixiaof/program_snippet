package cn.yyx.research.program.analysis.cache;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import org.eclipse.jdt.core.dom.CompilationUnit;

public class ModifiedCompilationUnitCache {
	
	private Queue<String> full_qualified_class_queue = new LinkedList<String>();
	private Map<String, CompilationUnit> modified_compilation_unit = new TreeMap<String, CompilationUnit>();
	private Map<String, Integer> full_qualified_class_count = new TreeMap<String, Integer>();
	private int cache_capacity = 25;
	
	public ModifiedCompilationUnitCache(int cache_capacity) {
		this.cache_capacity = cache_capacity;
	}
	
	public void AddToCache(String full_qualified_class, CompilationUnit unit)
	{
		modified_compilation_unit.put(full_qualified_class, unit);
		if (modified_compilation_unit.size() > cache_capacity)
		{
			full_qualified_class_queue.add(full_qualified_class);
			String poll_qualified_class = full_qualified_class_queue.poll();
			Integer poll_count = full_qualified_class_count.get(poll_qualified_class);
			poll_count -= 1;
			if (poll_count <= 0)
			{
				modified_compilation_unit.remove(poll_qualified_class);
			}
		}
	}
	
	public CompilationUnit HitCache(String full_qualified_class)
	{
		Integer count = full_qualified_class_count.get(full_qualified_class);
		if (count == null) {
			count = 0;
		}
		count++;
		full_qualified_class_count.put(full_qualified_class, count);
		return modified_compilation_unit.get(full_qualified_class);
	}
	
}
