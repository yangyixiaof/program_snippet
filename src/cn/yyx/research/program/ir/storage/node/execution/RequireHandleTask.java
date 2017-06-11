package cn.yyx.research.program.ir.storage.node.execution;

import cn.yyx.research.program.analysis.fulltrace.storage.FullTrace;
import cn.yyx.research.program.analysis.fulltrace.storage.connection.DynamicConnection;
import cn.yyx.research.program.analysis.fulltrace.storage.node.DynamicNode;
import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnectionInfo;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class RequireHandleTask extends IIRNodeTask {
	
	public RequireHandleTask(IRForOneInstruction iirnode) {
		super(iirnode);
	}
	
	@Override
	public void HandleOutConnection(DynamicNode source, DynamicNode target, StaticConnectionInfo connect_info,
			FullTrace ft) {
		int final_type = TaskExecutionHelper.ComputeFinalType(source, target, connect_info);
		DynamicConnection conn = new DynamicConnection(source, target, final_type, connect_info.getNum());
		ft.AddConnection(conn);
	}
	
}
