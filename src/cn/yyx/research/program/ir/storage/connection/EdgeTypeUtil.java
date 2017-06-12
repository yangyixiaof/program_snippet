package cn.yyx.research.program.ir.storage.connection;

public class EdgeTypeUtil {
	
	public static boolean OnlyHasSpecificType(int type, int ebt)
	{
		if ((type & ebt) == ebt && (type & (~ebt)) == 0)
		{
			return true;
		}
		return false;
	}
	
	public static boolean HasSpecificType(int type, int ebt)
	{
		if ((type & ebt) == ebt)
		{
			return true;
		}
		return false;
	}
	
}
