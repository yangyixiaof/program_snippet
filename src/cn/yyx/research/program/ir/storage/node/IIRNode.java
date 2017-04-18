package cn.yyx.research.program.ir.storage.node;

import java.util.Map;
import java.util.Set;

import cn.yyx.research.program.ir.storage.node.connection.Connection;

public interface IIRNode {
	
	// public void AddInConnectionMergeTask(Runnable run);
	
	public Map<IIRNode, Set<Connection>> PrepareOutNodes();
	
	public Map<IIRNode, Set<Connection>> PrepareInNodes();
	
	public void PutConnectionMergeTask(Connection conn, IIRNodeTask run);
	
	public IIRNodeTask GetConnectionMergeTask(Connection conn);
	
}
