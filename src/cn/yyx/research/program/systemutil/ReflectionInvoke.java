package cn.yyx.research.program.systemutil;

import java.lang.reflect.Constructor;

public class ReflectionInvoke {
	
	private static boolean ParameterClassAssignable(Object[] objs, Class<?>[] match_class)
	{
		if (objs == null && match_class == null)
		{
			return true;
		}
		if (objs == null || match_class == null)
		{
			return false;
		}
		if (objs.length != match_class.length)
		{
			return false;
		}
		for (int i=0;i<objs.length;i++)
		{
			if (!match_class[i].isAssignableFrom(objs[i].getClass()))
			{
				return false;
			}
		}
		return true;
	}
	
	public static Object InvokeConstructor(Class<?> cls, Object[] objs)
	{
		Constructor<?> cons[] = cls.getConstructors();
		for (Constructor<?> con : cons) {
			Class<?>[] para_types = con.getParameterTypes();
			if (ParameterClassAssignable(objs, para_types)) {
				try {
					Object r_val = con.newInstance(objs);
					return r_val;
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Error in new IIRTask.");
					System.exit(1);
				}
			}
		}
		return null;
	}
	
}
