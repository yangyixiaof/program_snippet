package cn.yyx.research.program.eclipse.searchutil;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class SearchIMethodVisitor extends ASTVisitor {
	
	MethodDeclaration to_be_searched = null;
	private IMethod imethod = null;
	
	public SearchIMethodVisitor(MethodDeclaration searched) {
		to_be_searched = searched;
	}
	
	@Override
	public void endVisit(MethodDeclaration node) {
		if (node.equals(to_be_searched))
		{
			// deleting.
			System.err.println("executed!");
			IMethodBinding ibinding = node.resolveBinding();
			System.err.println("IMethodBinding:" + ibinding);
			if (ibinding != null)
			{
				setImethod((IMethod)ibinding.getJavaElement());
			}
		}
		// return super.visit(node);
	}

	public IMethod getImethod() {
		return imethod;
	}

	private void setImethod(IMethod imethod) {
		this.imethod = imethod;
	}
	
}
