package cn.yyx.research.program.eclipse.searchutil;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

public class JavaSearch {
	
	public static List<ICompilationUnit> SearchForAllICompilationUnits(IJavaProject java_project) throws JavaModelException  {
		IPackageFragmentRoot[] package_roots = java_project.getPackageFragmentRoots();
		List<ICompilationUnit> units = new LinkedList<ICompilationUnit>();
		for (IPackageFragmentRoot package_root : package_roots) {
			// System.err.println("package_root:"+package_root);
			IJavaElement[] fragments = package_root.getChildren();
			for (int j = 0; j < fragments.length; j++) {
				IPackageFragment fragment = (IPackageFragment) fragments[j];
				IJavaElement[] javaElements = fragment.getChildren();
				for (int k = 0; k < javaElements.length; k++) {
					IJavaElement javaElement = javaElements[k];
					if (javaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
						units.add((ICompilationUnit) javaElement);
					}
				}
			}
		}
		return units;
	}
	
	public static void SearchForWhereTheMethodIsInvoked(IMethod method, boolean is_declaration, SearchRequestor requestor) throws CoreException {// "foo"
	    // Create search pattern
	    SearchPattern pattern = SearchPattern.createPattern(method, (is_declaration ? IJavaSearchConstants.DECLARATIONS : IJavaSearchConstants.REFERENCES));
		new SearchEngine().search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
				SearchEngine.createWorkspaceScope(), requestor, null);
	}
	
}
