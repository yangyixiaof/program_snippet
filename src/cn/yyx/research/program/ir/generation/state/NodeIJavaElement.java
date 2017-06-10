package cn.yyx.research.program.ir.generation.state;

import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

public class NodeIJavaElement {
	
	private ASTNode node = null;
	private Set<IJavaElement> ije = null;
	
	public NodeIJavaElement(ASTNode node, Set<IJavaElement> set) {
		this.SetNode(node);
		this.SetIJavaElement(set);
	}

	public ASTNode GetNode() {
		return node;
	}

	private void SetNode(ASTNode node) {
		this.node = node;
	}

	public Set<IJavaElement> GetIJavaElement() {
		return ije;
	}

	private void SetIJavaElement(Set<IJavaElement> ije) {
		this.ije = ije;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NodeIJavaElement) {
			NodeIJavaElement nije = (NodeIJavaElement)obj;
			if (node.equals(nije.node)) {
				if (ije == null) {
					if (nije.ije == null) {
						return true;
					}
				} else {
					if (ije.equals(nije.ije)) {
						return true;
					}
				}
			}
		}
		return super.equals(obj);
	}
	
}
