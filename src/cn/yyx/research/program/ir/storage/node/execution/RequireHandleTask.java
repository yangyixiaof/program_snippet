package cn.yyx.research.program.ir.storage.node.execution;

import cn.yyx.research.program.ir.storage.node.IIRNode;
import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.connection.DynamicConnection;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnection;

public class RequireHandleTask extends IIRNodeTask {
	
	public RequireHandleTask(IIRNode iirnode) {
		super(iirnode);
	}

	@Override
	public DynamicConnection HandleOutConnection(StaticConnection connect) {
		return null;
	}
	
}
