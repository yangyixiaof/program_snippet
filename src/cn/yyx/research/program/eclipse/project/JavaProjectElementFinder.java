package cn.yyx.research.program.eclipse.project;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

public class JavaProjectElementFinder {

	public static IPath FindSourceFolder(IJavaProject javaProject) throws JavaModelException {
		IPackageFragmentRoot[] packageFragmentRoot = javaProject.getAllPackageFragmentRoots();
		for (int i = 0; i < packageFragmentRoot.length; i++) {
			IPackageFragmentRoot froot = packageFragmentRoot[i];
			if (froot.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT && !froot.isArchive()
					&& froot.getKind() == IPackageFragmentRoot.K_SOURCE) {
				return froot.getPath();
			}
		}
		return null;
	}

}
