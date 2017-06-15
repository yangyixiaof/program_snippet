package cn.yyx.research.program.ir.generation.state;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

public class NodeIJavaElementStack {
	
	Stack<Set<IJavaElement>> node_ijes = new Stack<Set<IJavaElement>>();
	Stack<ASTNode> nodes = new Stack<ASTNode>();
	
	public NodeIJavaElementStack() {
	}
	
	public void Push(ASTNode node, Set<IJavaElement> ijes) {
		nodes.push(node);
		if (ijes == null) {
			ijes = new HashSet<IJavaElement>();
		}
		node_ijes.push(ijes);
	}
	
	public NodeIJavaElement Pop() {
		return new NodeIJavaElement(nodes.pop(), node_ijes.pop());
	}
	
	public NodeIJavaElement Peek() {
		return new NodeIJavaElement(nodes.peek(), node_ijes.peek());
	}
	
	public boolean IsEmpty() {
		return nodes.isEmpty();
	}

	public Set<IJavaElement> GetIJavaElementsFromTopDownToSpecified(ASTNode specified) {
		Set<IJavaElement> result = new HashSet<IJavaElement>();
		int nidx = nodes.size()-1;
		ASTNode s = null;
		while (s != specified) {
			if (nidx == -1) {
				System.err.println("Strange! astnode null!");
				System.exit(1);
			}
			s = nodes.get(nidx);
			result.addAll(node_ijes.get(nidx));
			nidx--;
		}
		return result;
	}
	
}
