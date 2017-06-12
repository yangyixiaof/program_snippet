package cn.yyx.research.program.ir.storage.node.execution;

import cn.yyx.research.program.analysis.fulltrace.storage.node.DynamicNode;
import cn.yyx.research.program.ir.storage.connection.ConnectionInfo;

public class TaskExecutionHelper {
	
	public static int ComputeFinalType(DynamicNode source, DynamicNode target, ConnectionInfo connect_info) {
		int accept_type = source.getInstr().GetAcceptType();
		int require_type = target.getInstr().GetRequireType();
		int final_type = connect_info.getType() | (accept_type & require_type);
		return final_type;
	}
	
}
