package cn.yyx.research.logger;

public class DebugLogger {
	
	public static void Log(String additioninfo, String[] infos)
	{
		StringBuffer sb = new StringBuffer();
		for (int i=0;i<infos.length;i++)
		{
			sb.append("arr["+i+"]"+":"+infos[i]+";");
		}
		System.out.println("Debug " + additioninfo + ":" + sb);
	}
	
	public static void Log(String additioninfo)
	{
		System.out.println("Debug " + additioninfo);
	}
	
}
