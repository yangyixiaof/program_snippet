package cn.yyx.research.program.ir.generation.state;

import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

public class NodeIJavaElement {
	
	private ASTNode node = null;
	private Set<IJavaElement> ijes = null;
	
	public NodeIJavaElement(ASTNode node, Set<IJavaElement> set) {
		this.SetNode(node);
		this.SetIJavaElementSet(set);
	}

	public ASTNode GetNode() {
		return node;
	}

	private void SetNode(ASTNode node) {
		this.node = node;
	}

	public Set<IJavaElement> GetIJavaElementSet() {
		return ijes;
	}

	private void SetIJavaElementSet(Set<IJavaElement> ijes) {
		this.ijes = ijes;
	}
	
	public void Merge(NodeIJavaElement nije) {
//		Set<IJavaElement> remove = new HashSet<IJavaElement>();
//		if (!(nije.GetNode() instanceof Statement)) {
//			// need to do contains judge.
//			Iterator<IJavaElement> iitr = ijes.iterator();
//			while (iitr.hasNext()) {
//				IJavaElement iije = iitr.next();
//				ASTNode happen = all_happen.get(iije);
//				Set<IJavaElement> next_level_set = nije.GetIJavaElementSet();
//				Iterator<IJavaElement> nitr = next_level_set.iterator();
//				while (nitr.hasNext()) {
//					IJavaElement next_level_ije = nitr.next();
//					ASTNode next_level_happen = all_happen.get(next_level_ije);
//					if (ASTSearch.ASTNodeContainsAnASTNode(happen, next_level_happen)) {
//						remove.add(next_level_ije);
//					}
//				}
//			}
//		}
		ijes.addAll(nije.GetIJavaElementSet());
//		ijes.removeAll(remove);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NodeIJavaElement) {
			NodeIJavaElement nije = (NodeIJavaElement)obj;
			if (node.equals(nije.node)) {
				if (ijes == null) {
					if (nije.ijes == null) {
						return true;
					}
				} else {
					if (ijes.equals(nije.ijes)) {
						return true;
					}
				}
			}
		}
		return super.equals(obj);
	}
	
}
