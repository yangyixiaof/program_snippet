package cn.yyx.research.program.ir.storage.node;

import cn.yyx.research.program.analysis.fulltrace.storage.FullTrace;
import cn.yyx.research.program.analysis.fulltrace.storage.node.DynamicNode;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnectionInfo;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public abstract class IIRNodeTask {
	
	IRForOneInstruction iirnode = null;
	
	public IIRNodeTask(IRForOneInstruction iirnode) {
		this.iirnode = iirnode;
	}
	
	public abstract void HandleOutConnection(DynamicNode source, DynamicNode target, StaticConnectionInfo connect_info, FullTrace ft);
	
}
