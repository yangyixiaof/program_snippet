package cn.yyx.research.program.eclipse.searchutil;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

import cn.yyx.research.logger.DebugLogger;

public class EclipseSearchForIMember {
	
	public void SearchForWhereTheMethodIsConcreteImplementated(IMethod method, SearchRequestor requestor)
			throws CoreException {
		// Create search pattern
		DebugLogger.Log("SearchForImplementation, method is:" + method);
		SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.DECLARATIONS);
		if (pattern == null)
		{
			DebugLogger.Log("method search pattern is null... this strange 'method' is:" + method);
			return;
		}
		new SearchEngine().search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
				SearchEngine.createWorkspaceScope(), requestor, null);
	}
	
	public void SearchForConcreteImplementationOfInterface(IType type, SearchRequestor requestor)
			throws CoreException {
		// Create search pattern
		DebugLogger.Log("SearchForImplementation, type is:" + type);
		SearchPattern pattern = SearchPattern.createPattern(type, IJavaSearchConstants.IMPLEMENTORS);
		if (pattern == null)
		{
			DebugLogger.Log("class or interface search pattern is null... this strange 'type' is:" + type);
			return;
		}
		new SearchEngine().search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
				SearchEngine.createWorkspaceScope(), requestor, null);
	}
	
}
