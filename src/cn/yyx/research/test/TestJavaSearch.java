package cn.yyx.research.test;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.CompilationUnit;

import cn.yyx.research.program.eclipse.jdtutil.JDTParser;
import cn.yyx.research.program.eclipse.searchutil.JavaSearch;

public class TestJavaSearch {
	
	public TestJavaSearch() {
	}
	
	public void TestJavaSearchMehodInvocation(JDTParser jdtparser) throws CoreException
	{
		IJavaProject java_project = jdtparser.GetJavaProject();
		List<ICompilationUnit> units = JavaSearch.SearchForAllICompilationUnits(java_project);
		for (ICompilationUnit unit : units)
		{
			CompilationUnit cu = jdtparser.ParseICompilationUnit(unit);
			
			cu.accept(new SearchIMethodVisitor(jdtparser));
			
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
	
	public static void TestInAll(JDTParser jdtparser) throws CoreException
	{
		TestJavaSearch tjs = new TestJavaSearch();
		tjs.TestJavaSearchMehodInvocation(jdtparser);
	}
	
}
