package cn.yyx.research.program.ir.search;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;

import cn.yyx.research.program.ir.method.IRForOneMethod;

@SuppressWarnings("restriction")
public class IRSearchRequestor extends SearchRequestor {
	
	private List<IRForOneMethod> methods = new LinkedList<IRForOneMethod>();
	
	public IRSearchRequestor() {
	}
	
	@Override
	public void acceptSearchMatch(SearchMatch match) throws CoreException {
		// find IMethod.
		Object element = match.getElement();
		if (element instanceof ResolvedSourceMethod)
		{
			IMethod method = (IMethod) element;
			methods.add(new IRForOneMethod(method));
		}
	}

	public Collection<IRForOneMethod> GetMethods() {
		return methods;
	}
	
}
