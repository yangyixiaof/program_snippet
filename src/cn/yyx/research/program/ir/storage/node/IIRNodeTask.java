package cn.yyx.research.program.ir.storage.node;

import cn.yyx.research.program.analysis.fulltrace.storage.FullTrace;
import cn.yyx.research.program.analysis.fulltrace.storage.node.DynamicNode;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnection;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public abstract class IIRNodeTask {
	
	IRForOneInstruction iirnode = null;
	
	public IIRNodeTask(IRForOneInstruction iirnode) {
		this.iirnode = iirnode;
	}
	
	public abstract void HandleOutConnection(DynamicNode source, DynamicNode target, StaticConnection connect, FullTrace ft);
	
}
