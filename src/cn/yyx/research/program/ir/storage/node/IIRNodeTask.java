package cn.yyx.research.program.ir.storage.node;

import cn.yyx.research.program.ir.storage.node.connection.DynamicConnection;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnection;

public abstract class IIRNodeTask {
	
	IIRNode iirnode = null;
	
	public IIRNodeTask(IIRNode iirnode) {
		this.iirnode = iirnode;
	}
	
	public abstract DynamicConnection HandleOutConnection(StaticConnection connect);
	
}
