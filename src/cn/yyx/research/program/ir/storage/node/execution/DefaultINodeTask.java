package cn.yyx.research.program.ir.storage.node.execution;

import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.connection.Connection;

public class DefaultINodeTask implements IIRNodeTask {
	
	private static DefaultINodeTask dint = new DefaultINodeTask();
	
	public static DefaultINodeTask GetDefaultINodeTask()
	{
		return dint;
	}
	
	private DefaultINodeTask() {
	}
	
	@Override
	public Connection MergeConnection(Connection child_in_connect) {
		return new Connection(child_in_connect.getSource(), child_in_connect.getTarget(), child_in_connect.getType());
	}
	
}
