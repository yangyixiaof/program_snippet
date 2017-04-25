package cn.yyx.research.test;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.CompilationUnit;

import cn.yyx.research.program.eclipse.jdtutil.JDTParser;
import cn.yyx.research.program.eclipse.searchutil.EclipseSearchForICompilationUnits;

public class TestJavaSearch {
	
	public TestJavaSearch() {
	}
	
	public void TestJavaSearchMehodInvocation(IJavaProject java_project) throws CoreException
	{
		List<ICompilationUnit> units = EclipseSearchForICompilationUnits.SearchForAllICompilationUnits(java_project);
		System.out.println("All ICompilationUnit Size:" + units.size());
		for (ICompilationUnit unit : units)
		{
			CompilationUnit cu = JDTParser.CreateJDTParser(java_project).ParseICompilationUnit(unit);
			
			cu.accept(new SearchIMethodVisitor(java_project));
			
			System.out.println("==================== One Round Over ====================");
//			@SuppressWarnings("unchecked")
//			List<AbstractTypeDeclaration> tps = cu.types();
//			Iterator<AbstractTypeDeclaration> titr = tps.iterator();
//			while (titr.hasNext())
//			{
//				AbstractTypeDeclaration atd = titr.next();
//				if (atd instanceof TypeDeclaration)
//				{
//					TypeDeclaration td = (TypeDeclaration)atd;
//					MethodDeclaration[] methods = td.getMethods();
//					for (MethodDeclaration md : methods)
//					{
//						SearchIMethodVisitor smv = new SearchIMethodVisitor(md);
//						cu.accept(smv);
//						IMethod imethod = smv.getImethod();
//						System.out.println("IMethod:" + imethod);
//						JavaSearch.SearchForWhereTheMethodIsInvoked(imethod, false, new SearchResultRequestor());
//					}
//				}
//			}
		}
	}
	
	public static void TestInAll(IJavaProject java_project) throws CoreException
	{
		TestJavaSearch tjs = new TestJavaSearch();
		tjs.TestJavaSearchMehodInvocation(java_project);
	}
	
}
