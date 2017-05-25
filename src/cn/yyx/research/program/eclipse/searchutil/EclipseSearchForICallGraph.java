package cn.yyx.research.program.eclipse.searchutil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

import cn.yyx.research.program.ir.util.IMethodDotPermissionHelper;

@SuppressWarnings("restriction")
public class EclipseSearchForICallGraph {
	
	public static Set<IMethod> GetWholeCallInfo(IMember[] members)
	{
		Set<IMethod> caller_roots = new HashSet<IMethod>();
		CallHierarchy callHierarchy = CallHierarchy.getDefault();
		MethodWrapper[] callers = callHierarchy.getCallerRoots(members);
		for (MethodWrapper mw : callers)
		{
			IMember im = mw.getMember();
			if (!(im instanceof IMethod))
			{
				System.err.println("Strange! why not IMethod.");
				System.exit(1);
			}
			IMethod imd = (IMethod) im;
			caller_roots.add(imd);
		}
		return caller_roots;
	}
	
	public static Set<IMethod> GetRootCallEntries(Map<IMethod, Set<IMethod>> inverse_call_graph)
	{
		Set<IMethod> roots = new HashSet<IMethod>();
		EclipseSearchForICallGraph search = new EclipseSearchForICallGraph();
		Set<IMethod> keys = inverse_call_graph.keySet();
		Iterator<IMethod> kitr = keys.iterator();
		while (kitr.hasNext())
		{
			IMethod im = kitr.next();
			boolean permit = IMethodDotPermissionHelper.GainPermissionToGenerateDot(im);
			if (permit) {
				roots.addAll(search.FindRootEntry(im, inverse_call_graph));
			}
		}
		return roots;
	}
	
	private Set<IMethod> visited = new HashSet<IMethod>();
	private Map<IMethod, Set<IMethod>> root_memory = new HashMap<IMethod, Set<IMethod>>();
	
	private Set<IMethod> FindRootEntry(IMethod leaf, Map<IMethod, Set<IMethod>> inverse_call_graph)
	{
		if (visited.contains(leaf))
		{
			Set<IMethod> memory = root_memory.get(leaf);
			if (memory == null)
			{
				return new HashSet<IMethod>();
			}
			return memory;
		}
		visited.add(leaf);
		Set<IMethod> roots = new HashSet<IMethod>();
		Set<IMethod> parents = inverse_call_graph.get(leaf);
		if (parents == null) {
			roots.add(leaf);
		} else {
			boolean all_cycle = true;
			Iterator<IMethod> pitr = parents.iterator();
			while (pitr.hasNext())
			{
				IMethod parent = pitr.next();
				if (!visited.contains(parent))
				{
					all_cycle = false;
					roots.addAll(FindRootEntry(parent, inverse_call_graph));
				}
			}
			if (all_cycle) {
				roots.add(leaf);
			}
		}
		root_memory.put(leaf, roots);
		return roots;
	}
	
}
