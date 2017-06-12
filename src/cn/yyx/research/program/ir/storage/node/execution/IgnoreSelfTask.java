package cn.yyx.research.program.ir.storage.node.execution;

import cn.yyx.research.program.analysis.fulltrace.storage.FullTrace;
import cn.yyx.research.program.analysis.fulltrace.storage.node.DynamicNode;
import cn.yyx.research.program.ir.storage.connection.ConnectionInfo;
import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class IgnoreSelfTask extends IIRNodeTask {

	public IgnoreSelfTask(IRForOneInstruction iirnode) {
		super(iirnode);
	}

	@Override
	public void HandleOutConnection(DynamicNode source, DynamicNode target, ConnectionInfo connect_info,
			FullTrace ft) {
		// do nothing.
	}
	
}
