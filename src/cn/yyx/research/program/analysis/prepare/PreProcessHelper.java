package cn.yyx.research.program.analysis.prepare;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.text.edits.TextEdit;

import cn.yyx.research.program.eclipse.searchutil.JavaSearch;

public class PreProcessHelper {

	public static void EliminateAllParameterizedType(IJavaProject java_project) throws JavaModelException {
		List<ICompilationUnit> units = JavaSearch.SearchForAllICompilationUnits(java_project);
		// System.err.println("unit_size:" + units.size());
		for (final ICompilationUnit compilation_resource : units) {
			TextEdit edit = PreProcessCompilationUnitHelper.EntirePreProcessCompilationUnit(compilation_resource,
					java_project);
			compilation_resource.applyTextEdit(edit, null);
			compilation_resource.reconcile(ICompilationUnit.NO_AST, false, compilation_resource.getOwner(), null);
//			CompilationUnit cu = 
//			if (cu == null)
//			{
//				System.err.println("ModifiedCompilationUnit is null, something must be wrong!");
//				System.exit(1);
//			}
			compilation_resource.save(null, false);
			
			// testing
			// System.out.println("CompilationUnit:" + cu);
			// testing
			// System.out.println("ICompilationUnit:" + compilation_resource.getSource());
		}
		java_project.save(null, false);
	}

}
