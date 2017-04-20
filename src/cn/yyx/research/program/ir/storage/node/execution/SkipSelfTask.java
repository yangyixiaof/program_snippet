package cn.yyx.research.program.ir.storage.node.execution;

import cn.yyx.research.program.ir.storage.node.IIRNode;
import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.connection.DynamicConnection;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnection;

public class SkipSelfTask extends IIRNodeTask {

	public SkipSelfTask(IIRNode iirnode) {
		super(iirnode);
	}

	@Override
	public DynamicConnection HandleOutConnection(StaticConnection connect) {
		// TODO Auto-generated method stub
		return null;
	}

}
