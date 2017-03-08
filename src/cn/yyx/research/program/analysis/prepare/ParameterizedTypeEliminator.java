package cn.yyx.research.program.analysis.prepare;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class ParameterizedTypeEliminator extends ASTVisitor {
	
	ASTRewrite rewrite = null;
	
	public ParameterizedTypeEliminator(ASTRewrite rewrite) {
		this.rewrite = rewrite;
	}
	
	@Override
	public void endVisit(ParameterizedType node) {
		rewrite.replace(node, node.getType(), null);
	}
	
}
