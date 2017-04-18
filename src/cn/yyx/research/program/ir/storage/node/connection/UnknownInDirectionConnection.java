package cn.yyx.research.program.ir.storage.node.connection;

import cn.yyx.research.program.ir.storage.node.IIRNode;

public class UnknownInDirectionConnection extends Connection {
	
	private static UnknownInDirectionConnection uodc = new UnknownInDirectionConnection(null, null, null);
	
	public static UnknownInDirectionConnection GetUnknownInDirectionConnection()
	{
		return uodc;
	}
	
	private UnknownInDirectionConnection(IIRNode source, IIRNode target, EdgeConnectionType type) {
		super(source, target, type);
	}
	
}
