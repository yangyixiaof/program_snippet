package cn.yyx.research.test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;

import cn.yyx.research.program.eclipse.jdtutil.JDTParser;

@SuppressWarnings("restriction")
public class SearchResultRequestorForTest extends SearchRequestor {

	IJavaProject java_project = null;
	
	public SearchResultRequestorForTest(IJavaProject java_project ) {
		this.java_project = java_project;
	}
	
	@Override
	public void acceptSearchMatch(SearchMatch match) throws CoreException {
		if (match.getElement() instanceof ResolvedSourceMethod)
		{
			Object element = match.getElement();
			System.out.println("result:" + element);
			if (element instanceof IMethod)
			{
				IMethod method = (IMethod) element;
				
				Flags.isAbstract(method.getFlags());
				
				IType type = method.getDeclaringType();
				System.out.println("================== start ==================");
				System.out.println("IType:" + type);
				System.out.println("matches:" + match.toString());
				System.out.println("match element class:" + match.getElement().getClass());
				CompilationUnit cu = JDTParser.CreateJDTParser(java_project).ParseICompilationUnit(method.getCompilationUnit());
				String searched_content = cu.getTypeRoot().getBuffer().getText(match.getOffset(), match.getLength());
				System.out.println("searched_content:" + searched_content);
				System.out.println("================== end ==================");
			}
//			ResolvedSourceMethod rsm = (ResolvedSourceMethod)match.getElement();
//			System.out.println("key:"+rsm.getKey());
		}
	}

}
