package cn.yyx.research.program.ir.orgranization;

import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.ir.generation.IRGeneratorForOneProject;
import cn.yyx.research.program.ir.storage.connection.ConnectionInfo;
import cn.yyx.research.program.ir.storage.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.connection.StaticConnection;
import cn.yyx.research.program.ir.storage.node.execution.SkipSelfTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneSentinel;

public class IRTreeForOneElement {
	
	protected IJavaElement im = null;
	protected IRCode parent_env = null;
	
	protected IRForOneInstruction root_node = null; // sentinel
	protected IRForOneInstruction last_node = null;
	
	public IRTreeForOneElement(IJavaElement ije, IRCode parent_env) {
		this.im = ije;
		SetParentEnv(parent_env);
		SetRootNode(new IRForOneSentinel(ije, parent_env, SkipSelfTask.class));
		SetLastNode(GetRootNode());
		// SetRootNode(new IRForOneOperation(parent_env, ije, IRMeta.VirtualSentinel, SkipSelfTask.class));
	}
	
	public boolean HasRootConnection()
	{
		Set<StaticConnection> out_cnns = IRGeneratorForOneProject.GetInstance().GetOutConnections(root_node);
		if (out_cnns != null && out_cnns.size() > 0) {
			return true;
		}
		return false;
	}
	
	public boolean HasElement()
	{
		return root_node != last_node;
	}
	
	public void SwitchDirection(IRForOneInstruction switch_to_last_node)
	{
		this.SetLastNode(switch_to_last_node);
	}
	
	public void SwitchToRoot()
	{
		this.SetLastNode(root_node);
	}
	
	public void GoForwardANode(IRForOneInstruction child)
	{
		IRGeneratorForOneProject.GetInstance().RegistConnection(new StaticConnection(last_node, child, new ConnectionInfo(EdgeBaseType.Self.Value())));
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

	private void SetLastNode(IRForOneInstruction last_node) {
		this.last_node = last_node;
	}

	public IRCode GetParentEnv() {
		return parent_env;
	}

	private void SetParentEnv(IRCode parent_env) {
		this.parent_env = parent_env;
	}
	
}
