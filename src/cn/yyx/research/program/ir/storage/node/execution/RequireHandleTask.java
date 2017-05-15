package cn.yyx.research.program.ir.storage.node.execution;

import cn.yyx.research.program.analysis.fulltrace.storage.connection.DynamicConnection;
import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnection;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class RequireHandleTask extends IIRNodeTask {
	
	public RequireHandleTask(IRForOneInstruction iirnode) {
		super(iirnode);
	}

	@Override
	public DynamicConnection HandleOutConnection(StaticConnection connect) {
		// TODO
		return null;
	}
	
}
