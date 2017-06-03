package cn.yyx.research.program.systemutil;

public class SystemUtil {
	
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	public static void Delay(long times)
	{
		try {
			Thread.sleep(times);
		} catch (Exception e) {
		}
	}
	
	public static void Flush() {
		System.out.flush();
		System.err.flush();
	}
	
}
