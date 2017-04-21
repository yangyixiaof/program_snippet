package cn.yyx.research.program.ir.storage.node;

import java.util.Map;
import java.util.Set;

import cn.yyx.research.program.ir.storage.node.connection.StaticConnection;

public interface IIRNode {
	
	// public void AddInConnectionMergeTask(Runnable run);
	
	public Map<IIRNode, Set<StaticConnection>> PrepareOutNodes();
	
	public Map<IIRNode, Set<StaticConnection>> PrepareInNodes();
	
	public IIRNodeTask GetOutConnectionMergeTask();
	
	public int GetRequireType();
	
	public void SetRequireType(int require_type);
	
	public int GetAcceptType();
	
	public void SetAcceptType(int accept_type);
	
}
