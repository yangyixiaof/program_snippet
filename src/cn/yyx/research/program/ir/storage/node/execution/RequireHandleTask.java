package cn.yyx.research.program.ir.storage.node.execution;

import cn.yyx.research.program.ir.storage.node.IIRNode;
import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.connection.DynamicConnection;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnection;

public class RequireHandleTask extends IIRNodeTask {
	
	int accept_type = 0;
	
	public RequireHandleTask(IIRNode iirnode, int accept_type) {
		super(iirnode);
		this.accept_type = accept_type;
	}

	@Override
	public DynamicConnection HandleOutConnection(StaticConnection connect) {
		return null;
	}
	
}
