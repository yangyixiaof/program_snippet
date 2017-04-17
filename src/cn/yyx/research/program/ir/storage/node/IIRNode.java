package cn.yyx.research.program.ir.storage.node;

import cn.yyx.research.program.ir.connection.Connection;

public interface IIRNode {
	
	public Connection ConnectToIt(IIRNode child);
	
}
