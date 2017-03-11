package cn.yyx.research.test;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import cn.yyx.research.program.eclipse.jdtutil.JDTParser;
import cn.yyx.research.program.eclipse.searchutil.JavaSearch;
import cn.yyx.research.program.eclipse.searchutil.SearchIMethodVisitor;

public class TestJavaSearch {
	
	public TestJavaSearch() {
	}
	
	public void TestJavaSearchMehodInvocation(JDTParser jdtparser) throws JavaModelException
	{
		IJavaProject java_project = jdtparser.GetJavaProject();
		List<ICompilationUnit> units = JavaSearch.SearchForAllICompilationUnits(java_project);
		for (ICompilationUnit unit : units)
		{
			CompilationUnit cu = jdtparser.ParseICompilationUnit(unit);
			
			@SuppressWarnings("unchecked")
			List<AbstractTypeDeclaration> tps = cu.types();
			Iterator<AbstractTypeDeclaration> titr = tps.iterator();
			while (titr.hasNext())
			{
				AbstractTypeDeclaration atd = titr.next();
				if (atd instanceof TypeDeclaration)
				{
					TypeDeclaration td = (TypeDeclaration)atd;
					MethodDeclaration[] methods = td.getMethods();
					for (MethodDeclaration md : methods)
					{
						SearchIMethodVisitor smv = new SearchIMethodVisitor(md);
						cu.accept(smv);
						System.out.println("MethodDeclaration:" + md);
						System.out.println("MethodBinding:" + md.resolveBinding());
						System.out.println("IMethod:" + smv.getImethod());
						// JavaSearch.SearchForWhereTheMethodIsInvoked(md);
					}
				}
			}
		}
	}
	
}
