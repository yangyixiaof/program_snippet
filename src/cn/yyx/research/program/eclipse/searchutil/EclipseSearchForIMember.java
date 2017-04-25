package cn.yyx.research.program.eclipse.searchutil;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

public class EclipseSearchForIMember {
	
	public void SearchForWhereTheMethodIsConcreteImplementated(IMethod method, SearchRequestor requestor)
			throws CoreException {
		// Create search pattern
		System.out.println("SearchForImplementation, method is:" + method);
		SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.DECLARATIONS);
		new SearchEngine().search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
				SearchEngine.createWorkspaceScope(), requestor, null);
	}
	
	public void SearchForConcreteImplementationOfInterface(IType type, SearchRequestor requestor)
			throws CoreException {
		// Create search pattern
		System.out.println("SearchForImplementation, type is:" + type);
		SearchPattern pattern = SearchPattern.createPattern(type, IJavaSearchConstants.IMPLEMENTORS);
		new SearchEngine().search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
				SearchEngine.createWorkspaceScope(), requestor, null);
	}
	
}
