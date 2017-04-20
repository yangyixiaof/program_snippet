package cn.yyx.research.program.ir.storage.node.execution;

import cn.yyx.research.program.ir.storage.node.IIRNode;
import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.connection.DynamicConnection;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnection;

public class DefaultINodeTask extends IIRNodeTask {
	
	public DefaultINodeTask(IIRNode iirnode) {
		super(iirnode);
	}
	
	@Override
	public DynamicConnection HandleOutConnection(StaticConnection child_in_connect) {
		return new DynamicConnection(child_in_connect.getSource(), child_in_connect.getTarget(), child_in_connect.getType());
	}
	
}
