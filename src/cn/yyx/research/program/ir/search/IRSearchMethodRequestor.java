package cn.yyx.research.program.ir.search;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;

import cn.yyx.research.logger.DebugLogger;
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
	
	private void HandleExtensionOfIType(IType tit)
	{
		EclipseSearchForIMember esfi = new EclipseSearchForIMember();
		IRSearchTypeRequestor request = new IRSearchTypeRequestor(java_project, tit);
		try {
			esfi.SearchForConcreteImplementationOfInterface(tit, request);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		Set<IType> types = request.getTypes();
		Iterator<IType> titr = types.iterator();
		while (titr.hasNext()) {
			IType tt_tit = titr.next();
			HandleUnImplementatedIMethodInIType(tt_tit);
		}
	}

	private void HandleUnImplementatedIMethodInIType(IType tit) {
		DebugLogger.Log("temp tit:" + tit);
		IMethod imd = tit.getMethod(method.getElementName(), method.getParameterTypes());
		DebugLogger.Log("temp tit imd:" + imd);
		if (imd != null) // && !Flags.isAbstract(imd.getFlags())
		{
			// System.out.println("That is really exist:" + imd + "; in " + tit
			// + ";" + imd.exists());
			try {
				if (tit.isInterface() || !imd.exists() || Flags.isAbstract(imd.getFlags())) {
					 HandleExtensionOfIType(tit);
				} else {
					EclipseSearchForIMember search = new EclipseSearchForIMember();
					IRSearchMethodRequestor requestor = new IRSearchMethodRequestor(java_project, imd);
					try {
						search.SearchForWhereTheMethodIsConcreteImplementated(imd, requestor);
					} catch (CoreException e) {
						e.printStackTrace();
					}
					methods.addAll(requestor.GetMethods());
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void acceptSearchMatch(SearchMatch match) throws CoreException {
		// find IMethod.
		Object element = match.getElement();
		if (element instanceof ResolvedSourceMethod) {
			IMethod im = (IMethod) element;
			IType it = im.getDeclaringType();
			if (it.isInterface() || !im.exists() || Flags.isAbstract(im.getFlags())) {
				HandleExtensionOfIType(it);
			} else {
				if (!im.isBinary())
				{
					methods.add(im);
				}
			}
		}
		DebugLogger.Log(match, java_project);
	}

	public Set<IMethod> GetMethods() {
		return methods;
	}

}
