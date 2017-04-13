package cn.yyx.research.program.ir.ast;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;

public class ContainsMemberVisitor extends ASTVisitor {
	
	private IJavaElement im = null;
	private boolean do_contains = false;
	
	public ContainsMemberVisitor(IJavaElement im) {
		this.im = im;
	}
	
	@Override
	public boolean preVisit2(ASTNode node) {
		if (do_contains)
		{
			return false;
		}
		return super.preVisit2(node);
	}
	
	@Override
	public boolean visit(SimpleName node) {
		IBinding ib = node.resolveBinding();
		if (ib != null)
		{
			IJavaElement im = null;
			if (ib instanceof ITypeBinding)
			{
				ITypeBinding it = (ITypeBinding)ib;
				IJavaElement ije = it.getJavaElement();
				if (ije instanceof IJavaElement)
				{
					im = (IJavaElement)ije;
				}
			}
			if (ib instanceof IVariableBinding)
			{
				IVariableBinding ivb = (IVariableBinding)ib;
				IJavaElement ije = ivb.getJavaElement();
				if (ije instanceof IJavaElement)
				{
					im = (IJavaElement)ije;
				}
			}
			if (im != null)
			{
				if (im.equals(this.im))
				{
					do_contains = true;
				}
			}
		}
		return super.visit(node);
	}
	
	public boolean DoContains()
	{
		return do_contains;
	}
	
}
