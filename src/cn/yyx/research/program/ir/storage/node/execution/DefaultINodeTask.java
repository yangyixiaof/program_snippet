package cn.yyx.research.program.ir.storage.node.execution;

import cn.yyx.research.program.analysis.fulltrace.storage.connection.DynamicConnection;
import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnection;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class DefaultINodeTask extends IIRNodeTask {
	
	public DefaultINodeTask(IRForOneInstruction iirnode) {
		super(iirnode);
	}
	
	@Override
	public DynamicConnection HandleOutConnection(StaticConnection child_in_connect) {
		// TODO
		return null;
	}
	
}
