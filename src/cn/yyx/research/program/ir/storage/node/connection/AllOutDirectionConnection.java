package cn.yyx.research.program.ir.storage.node.connection;

import cn.yyx.research.program.ir.storage.node.IIRNode;

public class AllOutDirectionConnection extends Connection {
	
	private static AllOutDirectionConnection uodc = new AllOutDirectionConnection(null, null, null);
	
	public static AllOutDirectionConnection GetAllOutDirectionConnection()
	{
		return uodc;
	}
	
	private AllOutDirectionConnection(IIRNode source, IIRNode target, EdgeConnectionType type) {
		super(source, target, type);
	}
	
}
