package cn.yyx.research.program.eclipse.searchutil;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;

public class SearchResultRequestor extends SearchRequestor {
	
	@Override
	public void acceptSearchMatch(SearchMatch match) throws CoreException {
		System.out.println("matches:" + match.toString());
		System.out.println("matche element class:" + match.getElement().getClass());
//		ResolvedSourceMethod rsm = (ResolvedSourceMethod)match.getElement();
//		System.out.println("key:"+rsm.getKey());
	}

}
