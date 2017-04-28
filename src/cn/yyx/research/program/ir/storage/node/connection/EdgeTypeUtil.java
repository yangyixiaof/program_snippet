package cn.yyx.research.program.ir.storage.node.connection;

public class EdgeTypeUtil {
	
	public static boolean OnlyHasBaseType(int type, EdgeBaseType ebt)
	{
		if ((type & ebt.getType()) > 0 && (type & (~ebt.getType())) == 0)
		{
			return true;
		}
		return false;
	}
	
	public static boolean HasBaseType(int type, EdgeBaseType ebt)
	{
		if ((type & ebt.getType()) > 0)
		{
			return true;
		}
		return false;
	}
	
}
