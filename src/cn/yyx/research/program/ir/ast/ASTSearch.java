package cn.yyx.research.program.ir.ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

public class ASTSearch {
	
//	public static boolean ASTNodeContainsAMember(ASTNode astnode, IJavaElement im)
//	{
//		if (im == null)
//		{
//			return false;
//		}
//		ContainsMemberVisitor cv = new ContainsMemberVisitor(im);
//		astnode.accept(cv);
//		return cv.DoContains();
//	}
	
	public static boolean ASTNodeContainsAnASTNode(ASTNode astnode, ASTNode searched)
	{
		if (searched == null)
		{
			return false;
		}
		ContainsASTNodeVisitor cv = new ContainsASTNodeVisitor(searched);
		astnode.accept(cv);
		return cv.DoContains();
	}
	
//	public static boolean ASTNodeContainsABinding(ASTNode astnode, IBinding ib)
//	{
//		if (ib == null)
//		{
//			return false;
//		}
//		ContainsBindingVisitor cv = new ContainsBindingVisitor(ib);
//		astnode.accept(cv);
//		return cv.DoContains();
//	}
	
	public static ASTNode FindMostCloseLoopNode(ASTNode astnode)
	{
		ASTNode temp = astnode;
		while (temp != null && !(temp instanceof WhileStatement) && !(temp instanceof ForStatement) && !(temp instanceof EnhancedForStatement))
		{
			temp = temp.getParent();
		}
		return temp;
	}
	
	public static ASTNode FindMostCloseAbstractTypeDeclaration(ASTNode astnode)
	{
		ASTNode temp = astnode;
		while (temp != null && !(temp instanceof AbstractTypeDeclaration) && !(temp instanceof AnonymousClassDeclaration))
		{
			temp = temp.getParent();
		}
		return temp;
	}
	
	public static ASTNode FindMostCloseBreakContinueScope(ASTNode astnode)
	{
		ASTNode temp = astnode;
		while (temp != null && !(temp instanceof WhileStatement) && !(temp instanceof DoStatement) && !(temp instanceof ForStatement) && !(temp instanceof EnhancedForStatement) && !(temp instanceof SwitchStatement))
		{
			temp = temp.getParent();
		}
		return temp;
	}
	
}
