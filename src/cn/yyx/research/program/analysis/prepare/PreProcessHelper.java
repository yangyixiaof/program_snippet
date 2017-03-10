package cn.yyx.research.program.analysis.prepare;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.text.edits.TextEdit;

import cn.yyx.research.program.eclipse.jdtutil.JDTParser;

public class PreProcessHelper {

	public static void EliminateAllParameterizedType(JDTParser jdtparser) throws JavaModelException {
		IJavaProject java_project = jdtparser.GetJavaProject();
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
		for (final ICompilationUnit compilation_resource : units) {
			TextEdit edit = PreProcessCompilationUnitHelper.EntirePreProcessCompilationUnit(compilation_resource,
					jdtparser);
			compilation_resource.applyTextEdit(edit, null);
			CompilationUnit cu = compilation_resource.reconcile(ICompilationUnit.NO_AST, false, null, null);

			// testing
			System.out.println("CompilationUnit:"+cu);
		}
	}

}
