package cn.yyx.research.program.eclipse.searchutil;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

public class EclipseSearchForICompilationUnits {

	public static List<ICompilationUnit> SearchForAllICompilationUnits(IJavaProject java_project)
			throws JavaModelException {
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

}
