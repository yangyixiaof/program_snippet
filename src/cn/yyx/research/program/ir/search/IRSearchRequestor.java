package cn.yyx.research.program.ir.search;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;

import cn.yyx.research.program.ir.method.IRForOneMethod;

public class IRSearchRequestor extends SearchRequestor {
	
	private List<IRForOneMethod> methods = new LinkedList<IRForOneMethod>();
	
	public IRSearchRequestor() {
	}
	
	@Override
	public void acceptSearchMatch(SearchMatch match) throws CoreException {
		// find IMethod.
		System.out.println("Match:" + match);
		// methods.add(new IRForOneMethod(im));
	}

	public Collection<IRForOneMethod> GetMethods() {
		return methods;
	}
	
}
