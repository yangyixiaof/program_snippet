package cn.yyx.research.program.ir.search;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
import cn.yyx.research.program.eclipse.searchutil.EclipseSearchForIMember;
import cn.yyx.research.program.ir.IRControl;

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
					IMethod imd = tit.getMethod(method.getElementName(), method.getParameterTypes());
					if (imd != null && !Flags.isAbstract(imd.getFlags()))
					{
						methods.add(imd);
					}
				}
			} else if (Flags.isAbstract(im.getFlags())) {
				// do nothing.
			} else {
				methods.add(im);
			}
			
			
			
			
			
			// codes below are just used for debugging.
			
			if (IRControl.debug)
			{
				IType type = im.getDeclaringType();
				System.out.println("================== start ==================");
				System.out.println("IType:" + type);
				System.out.println("matches:" + match.toString());
				System.out.println("match element class:" + match.getElement().getClass());
				CompilationUnit cu = JDTParser.CreateJDTParser(java_project).ParseICompilationUnit(im.getCompilationUnit());
				String searched_content = cu.getTypeRoot().getBuffer().getText(match.getOffset(), match.getLength());
				System.out.println("searched_content:" + searched_content);
				System.out.println("================== end ==================");
			}
		}
	}

	public Collection<IMethod> GetMethods() {
		return methods;
	}
	
}
