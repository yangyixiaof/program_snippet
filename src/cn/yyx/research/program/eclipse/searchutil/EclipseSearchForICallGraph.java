package cn.yyx.research.program.eclipse.searchutil;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

@SuppressWarnings("restriction")
public class EclipseSearchForICallGraph {
	
	public static Set<IMethod> GetRootCallers(IMember[] members)
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
	
}
