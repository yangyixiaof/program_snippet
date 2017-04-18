package cn.yyx.research.program.ir.storage.node.connection;

import cn.yyx.research.program.ir.storage.node.IIRNode;

public class UnknownOutDirectionConnection extends Connection {
	
	private static UnknownOutDirectionConnection uodc = new UnknownOutDirectionConnection(null, null, null);
	
	public static UnknownOutDirectionConnection GetUnknownOutDirectionConnection()
	{
		return uodc;
	}
	
	private UnknownOutDirectionConnection(IIRNode source, IIRNode target, EdgeConnectionType type) {
		super(source, target, type);
	}
	
}
