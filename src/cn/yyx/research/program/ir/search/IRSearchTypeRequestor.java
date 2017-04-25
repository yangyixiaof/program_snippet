package cn.yyx.research.program.ir.search;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.ResolvedSourceType;

import cn.yyx.research.program.eclipse.searchutil.EclipseSearchForIMember;
import cn.yyx.research.program.ir.IRControl;

@SuppressWarnings("restriction")
public class IRSearchTypeRequestor extends SearchRequestor {
	
	private Set<IType> types = new HashSet<IType>();
	private IJavaProject java_project = null;
	private IType type = null;
	
	public IRSearchTypeRequestor(IJavaProject java_project, IType type) {
		this.java_project = java_project;
		this.setType(type);
	}
	
	@Override
	public void acceptSearchMatch(SearchMatch match) throws CoreException {
		Object ele = match.getElement();
		if (ele instanceof ResolvedSourceType)
		{
			ResolvedSourceType source_type = (ResolvedSourceType)ele;
			if (source_type.isInterface()) {
				IRSearchTypeRequestor req = new IRSearchTypeRequestor(java_project, source_type);
				EclipseSearchForIMember esearch = new EclipseSearchForIMember();
				esearch.SearchForConcreteImplementationOfInterface(source_type, req);
				getTypes().addAll(req.getTypes());
			} else {
				getTypes().add(source_type);
			}
		}
		
		
		
		
		if (IRControl.debug)
		{
			System.out.println("================== type search start ==================");
			System.out.println(match.getElement().getClass());
			System.out.println(match);
			System.out.println("================== type search end ==================");
		}
//			ResolvedSourceMethod rsm = (ResolvedSourceMethod)match.getElement();
//			System.out.println("key:"+rsm.getKey());
	}

	public Set<IType> getTypes() {
		return types;
	}

	public IType getType() {
		return type;
	}

	private void setType(IType type) {
		this.type = type;
	}

}
