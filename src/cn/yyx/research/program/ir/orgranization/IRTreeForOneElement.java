package cn.yyx.research.program.ir.orgranization;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.ir.IRMeta;
import cn.yyx.research.program.ir.storage.node.execution.SkipSelfTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneOperation;

public class IRTreeForOneElement {
	
	private IRTreeNode root_node = null; // sentinel
	
	private IRTreeNode last_node = null;
	
	public IRTreeForOneElement(IJavaElement ije, IRCode parent_env) {
		SetRootNode(new IRTreeNode(new IRForOneOperation(parent_env, ije, IRMeta.VirtualSentinel, SkipSelfTask.class)));
		SetLastNode(GetRootNode());
	}
	
	public boolean HasElement()
	{
		return root_node != last_node;
	}
	
	public void SwitchDirection(IRTreeNode switch_to_last_node)
	{
		this.SetLastNode(switch_to_last_node);
	}
	
	public void GoForwardANode(IRTreeNode child, Integer num_of_connects)
	{
		GetLastNode().PutChild(child, num_of_connects);
		SetLastNode(child);
	}

	public IRTreeNode GetRootNode() {
		return root_node;
	}

	private void SetRootNode(IRTreeNode root_node) {
		this.root_node = root_node;
	}

	public IRTreeNode GetLastNode() {
		return last_node;
	}

	public void SetLastNode(IRTreeNode last_node) {
		this.last_node = last_node;
	}
	
}
