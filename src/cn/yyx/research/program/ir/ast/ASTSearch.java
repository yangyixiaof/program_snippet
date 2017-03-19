package cn.yyx.research.program.ir.ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;

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
	
}
