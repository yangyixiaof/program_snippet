package cn.yyx.research.program.ir.orgranization;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.ir.IRMeta;
import cn.yyx.research.program.ir.generation.IRGeneratorForOneProject;
import cn.yyx.research.program.ir.storage.node.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnection;
import cn.yyx.research.program.ir.storage.node.execution.SkipSelfTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneOperation;

public class IRTreeForOneElement {
	
	protected IJavaElement im = null;
	protected IRCode parent_env = null;
	
	protected IRForOneInstruction root_node = null; // sentinel
	protected IRForOneInstruction last_node = null;
	
	public IRTreeForOneElement(IJavaElement ije, IRCode parent_env) {
		this.im = ije;
		SetParentEnv(parent_env);
		SetRootNode(new IRForOneOperation(parent_env, ije, IRMeta.VirtualSentinel, SkipSelfTask.class));
		SetLastNode(GetRootNode());
	}
	
	public boolean HasElement()
	{
		return root_node != last_node;
	}
	
	public void SwitchDirection(IRForOneInstruction switch_to_last_node)
	{
		this.SetLastNode(switch_to_last_node);
	}
	
	public void GoForwardANode(IRForOneInstruction child)
	{
		IRGeneratorForOneProject.GetInstance().RegistConnection(new StaticConnection(last_node, child, EdgeBaseType.Self.Value()));
		SetLastNode(child);
	}

	public IRForOneInstruction GetRootNode() {
		return root_node;
	}

	private void SetRootNode(IRForOneInstruction root_node) {
		this.root_node = root_node;
	}

	public IRForOneInstruction GetLastNode() {
		return last_node;
	}

	public void SetLastNode(IRForOneInstruction last_node) {
		this.last_node = last_node;
	}

	public IRCode GetParentEnv() {
		return parent_env;
	}

	public void SetParentEnv(IRCode parent_env) {
		this.parent_env = parent_env;
	}
	
}
