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
	
	/**
	 * Remember that connections with no exist source nodes should not be added.
	 * @param source
	 * @param target
	 * @param connect_info
	 * @param ft
	 */
	public abstract void HandleOutConnection(DynamicNode source, DynamicNode target, StaticConnectionInfo connect_info, FullTrace ft);
	
}
