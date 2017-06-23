package cn.yyx.research.program.ir.generation.structure;

import java.util.HashMap;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

public class IElementASTNodeHappen {
	
	HashMap<IJavaElement, ASTNode> ele_to_node = new HashMap<IJavaElement, ASTNode>();
	HashMap<ASTNode, IJavaElement> node_to_ele = new HashMap<ASTNode, IJavaElement>();
	
	public void PutHappen(IJavaElement ije, ASTNode node) {
		ele_to_node.put(ije, node);
		node_to_ele.put(node, ije);
	}
	
	public ASTNode GetASTNodeByIElement(IJavaElement ije) {
		return ele_to_node.get(ije);
	}
	
	public IJavaElement GetIElementByASTNode(ASTNode node) {
		return node_to_ele.get(node);
	}
	
}
