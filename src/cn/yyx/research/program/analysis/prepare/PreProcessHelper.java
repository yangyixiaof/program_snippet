package cn.yyx.research.program.analysis.prepare;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.text.edits.TextEdit;

import cn.yyx.research.program.eclipse.jdtutil.JDTParser;

public class PreProcessHelper {
	
	public static void EliminateAllParameterizedType(JDTParser jdtparser) throws JavaModelException
	{
		IJavaProject java_project = jdtparser.GetJavaProject();
		IPackageFragment[] package_roots = java_project.getPackageFragments();
		for (IPackageFragment package_root : package_roots)
		{
			for (final ICompilationUnit compilation_resource : package_root.getCompilationUnits()) {
				TextEdit edit = PreProcessCompilationUnitHelper.EntirePreProcessCompilationUnit(compilation_resource, jdtparser);
				compilation_resource.applyTextEdit(edit, null);
				compilation_resource.reconcile(ICompilationUnit.NO_AST, false, null, null);
			}
		}
	}
	
}
