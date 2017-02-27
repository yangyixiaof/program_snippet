package cn.yyx.research.program.analysis.jdtutil;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

public class JavaSearch {

	public void Search() throws CoreException {
		SearchPattern pattern = SearchPattern.createPattern("foo", IJavaSearchConstants.METHOD,
				IJavaSearchConstants.REFERENCES, SearchPattern.R_EXACT_MATCH);
		SearchRequestor requestor = new SearchRequestor() {
			@Override
			public void acceptSearchMatch(SearchMatch match) throws CoreException {
				System.out.println(match.toString());
			}
		};
		new SearchEngine().search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
				SearchEngine.createWorkspaceScope(), requestor, null);
	}

}
