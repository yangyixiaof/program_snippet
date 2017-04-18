package cn.yyx.research.program.ir.storage.node.connection;

public class EdgeConnectionType {
	
	int type = 0;
	
	public EdgeConnectionType(int type) {
		this.type = type;
	}
	
	public boolean HasBaseType(EdgeBaseType ebt)
	{
		if ((type & ebt.getType()) > 0)
		{
			return true;
		}
		return false;
	}
	
}
