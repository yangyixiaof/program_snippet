package cn.yyx.research.program.ir.storage.node.connection;

public class EdgeTypeUtil {
	
	public static boolean OnlyHasSpecificType(int type, int ebt)
	{
		if ((type & ebt) > 0 && (type & (~ebt)) == 0)
		{
			return true;
		}
		return false;
	}
	
	public static boolean HasSpecificType(int type, int ebt)
	{
		if ((type & ebt) > 0)
		{
			return true;
		}
		return false;
	}
	
}
