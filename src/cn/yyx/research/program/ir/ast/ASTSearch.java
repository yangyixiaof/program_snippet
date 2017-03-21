package cn.yyx.research.program.ir.ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.WhileStatement;

public class ASTSearch {
	
	public static boolean ASTNodeContainsABinding(ASTNode astnode, IBinding ib)
	{
		if (ib == null)
		{
			return false;
		}
		ContainsVisitor cv = new ContainsVisitor(ib);
		astnode.accept(cv);
		return cv.DoContains();
	}
	
	public static ASTNode FindMostCloseLoopNode(ASTNode astnode)
	{
		ASTNode temp = astnode;
		while (temp != null && !(temp instanceof WhileStatement) && !(temp instanceof ForStatement) && !(temp instanceof EnhancedForStatement))
		{
			temp = temp.getParent();
		}
		return temp;
	}
	
}
