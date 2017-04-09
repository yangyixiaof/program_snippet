package cn.yyx.research.program.ir.ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;

public class ContainsBindingVisitor extends ASTVisitor {
	
	private IBinding ib = null;
	private boolean do_contains = false;
	
	public ContainsBindingVisitor(IBinding ib) {
		this.ib = ib;
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
		if (ib != null && ib instanceof ITypeBinding && ib instanceof IVariableBinding)
		{
			if (ib.equals(this.ib))
			{
				do_contains = true;
			}
		}
		return super.visit(node);
	}
	
	public boolean DoContains()
	{
		return do_contains;
	}
	
}
