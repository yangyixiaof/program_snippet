package cn.yyx.research.program.ir.orgranization;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class IRTreeNode {
	
	private IRForOneInstruction node = null;
	private Map<IRTreeNode, Integer> parents = new HashMap<IRTreeNode, Integer>();
	private Map<IRTreeNode, Integer> children = new HashMap<IRTreeNode, Integer>();
	
	public IRTreeNode(IRForOneInstruction node) {
		this.setNode(node);
	}

	public IRForOneInstruction getNode() {
		return node;
	}

	private void setNode(IRForOneInstruction node) {
		this.node = node;
	}
	
	private void PutNode(IRTreeNode node, Map<IRTreeNode, Integer> map)
	{
		Integer it = map.get(node);
		if (it == null)
		{
			it = 0;
		}
		it++;
		map.put(node, it);
	}
	
	private void PutParent(IRTreeNode parent) {
		PutNode(parent, parents);
	}
	
	public void PutChild(IRTreeNode child) {
		PutNode(child, children);
		child.PutParent(this);
	}
	
	public Iterator<IRTreeNode> IterateParent()
	{
		return parents.keySet().iterator();
	}
	
	public Integer ToParentNumberOfConnections(IRTreeNode irtn)
	{
		return parents.get(irtn);
	}
	
	public Iterator<IRTreeNode> IterateChild()
	{
		return children.keySet().iterator();
	}
	
	public Integer ToChildNumberOfConnections(IRTreeNode irtn)
	{
		return children.get(irtn);
	}
	
}
