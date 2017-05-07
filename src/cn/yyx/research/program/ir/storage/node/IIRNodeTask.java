package cn.yyx.research.program.ir.storage.node;

import cn.yyx.research.program.ir.storage.node.connection.DynamicConnection;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnection;
import cn.yyx.research.program.ir.storage.node.zstatic.lowlevel.IRForOneInstruction;

public abstract class IIRNodeTask {
	
	IRForOneInstruction iirnode = null;
	
	public IIRNodeTask(IRForOneInstruction iirnode) {
		this.iirnode = iirnode;
	}
	
	public abstract DynamicConnection HandleOutConnection(StaticConnection connect);
	
}
