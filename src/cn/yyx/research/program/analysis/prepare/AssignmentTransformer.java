package cn.yyx.research.program.analysis.prepare;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class AssignmentTransformer extends ASTVisitor {
	
	ASTRewrite rewrite = null;
	
	public AssignmentTransformer(ASTRewrite rewrite) {
		this.rewrite = rewrite;
	}
	
	@Override
	public void endVisit(Assignment node) {
		String optr = node.getOperator().toString();
		if (optr.equals("=")) {
			return;
		}
		Expression left_expr = node.getLeftHandSide();
		Expression right_expr = node.getRightHandSide();
		AST ast = node.getAST();
		Assignment new_assign = ast.newAssignment();
		new_assign.setOperator(org.eclipse.jdt.core.dom.Assignment.Operator.ASSIGN);
		new_assign.setLeftHandSide((Expression)rewrite.createCopyTarget(left_expr));
		InfixExpression infix_expr = ast.newInfixExpression();
		Operator op = Operator.toOperator(optr.substring(0, optr.length()-1));
		infix_expr.setOperator(op);
		infix_expr.setLeftOperand((Expression)rewrite.createCopyTarget(left_expr));
		infix_expr.setRightOperand((Expression)rewrite.createCopyTarget(right_expr));
		new_assign.setRightHandSide(infix_expr);
		rewrite.replace(node, new_assign, null);
		super.endVisit(node);
	}
	
}
