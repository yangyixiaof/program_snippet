package cn.yyx.research.test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;

@SuppressWarnings("restriction")
public class SearchResultRequestorForTest extends SearchRequestor {
	
	CompilationUnit unit = null;
	
	public SearchResultRequestorForTest(CompilationUnit unit) {
		this.unit = unit;
	}
	
	@Override
	public void acceptSearchMatch(SearchMatch match) throws CoreException {
		if (match.getElement() instanceof ResolvedSourceMethod)
		{
			Object element = match.getElement();
			if (element instanceof IMethod)
			{
				IMethod method = (IMethod) element;
				IType type = method.getDeclaringType();
				System.out.println("IType:" + type);
				System.out.println("matches:" + match.toString());
				System.out.println("match element class:" + match.getElement().getClass());
				String searched_content = unit.getTypeRoot().getBuffer().getText(match.getOffset(), match.getLength());
				System.out.println("searched_content:" + searched_content);
			}
//			ResolvedSourceMethod rsm = (ResolvedSourceMethod)match.getElement();
//			System.out.println("key:"+rsm.getKey());
		}
	}

}
