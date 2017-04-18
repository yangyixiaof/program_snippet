package cn.yyx.research.program.ir.storage.node;

import cn.yyx.research.program.ir.connection.Connection;

public interface IIRNode {
	
	public Connection StaticConnectToIt(IIRNode parent);
	public Connection DynamicConnectToIt(IIRNode parent);
	// public Connection DynamicConnectToIt(IIRNode parent);
	
}
