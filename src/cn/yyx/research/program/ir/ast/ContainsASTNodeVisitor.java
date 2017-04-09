package cn.yyx.research.program.ir.ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class ContainsASTNodeVisitor extends ASTVisitor {
	
	private ASTNode ast = null;
	private boolean do_contains = false;
	
	public ContainsASTNodeVisitor(ASTNode ast) {
		this.ast = ast;
	}
	
	@Override
	public boolean preVisit2(ASTNode node) {
		if (node.equals(ast))
		{
			do_contains = true;
		}
		if (do_contains)
		{
			return false;
		}
		return super.preVisit2(node);
	}
	
	public boolean DoContains()
	{
		return do_contains;
	}
	
}
