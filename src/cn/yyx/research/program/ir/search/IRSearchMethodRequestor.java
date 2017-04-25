package cn.yyx.research.program.ir.search;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;

import cn.yyx.research.program.eclipse.searchutil.EclipseSearchForIMember;

@SuppressWarnings("restriction")
public class IRSearchMethodRequestor extends SearchRequestor {
	
	private IJavaProject java_project = null;
	private Set<IMethod> methods = new HashSet<IMethod>();
	private IMethod method = null;
	
	public IRSearchMethodRequestor(IJavaProject java_project, IMethod method) {
		this.java_project = java_project;
		this.method = method;
	}
	
	@Override
	public void acceptSearchMatch(SearchMatch match) throws CoreException {
		// find IMethod.
		Object element = match.getElement();
		if (element instanceof ResolvedSourceMethod)
		{
			IMethod im = (IMethod) element;
			
			IType it = im.getDeclaringType();
			if (it.isInterface()) {
				EclipseSearchForIMember esfi = new EclipseSearchForIMember();
				IRSearchTypeRequestor request = new IRSearchTypeRequestor(java_project, it);
				esfi.SearchForConcreteImplementationOfInterface(it, request);
				Set<IType> types = request.getTypes();
				Iterator<IType> titr = types.iterator();
				while (titr.hasNext())
				{
					IType tit = titr.next();
					
					// System.out.println("temp tit:" + tit);
					
					IMethod imd = tit.getMethod(method.getElementName(), method.getParameterTypes());
					
					// System.out.println("temp tit imd:" + imd);
					
					if (imd != null) //  && !Flags.isAbstract(imd.getFlags())
					{
						EclipseSearchForIMember search = new EclipseSearchForIMember();
						IRSearchMethodRequestor requestor = new IRSearchMethodRequestor(java_project, imd);
						search.SearchForWhereTheMethodIsConcreteImplementated(imd, requestor);
						methods.addAll(requestor.GetMethods());
					}
				}
			} else if (Flags.isAbstract(im.getFlags())) {
				// do nothing.
			} else {
				methods.add(im);
			}
		}
	}

	public Set<IMethod> GetMethods() {
		return methods;
	}
	
}
