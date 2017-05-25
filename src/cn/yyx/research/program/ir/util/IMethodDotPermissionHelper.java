package cn.yyx.research.program.ir.util;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

public class IMethodDotPermissionHelper {
	
	public static boolean GainPermissionToGenerateDot(IMethod im) {
		boolean permit = true;
		try {
			// System.out.println(im);
			// System.out.println(im.hasChildren());
			if (!im.isConstructor() && !im.hasChildren()) {
				permit = false;
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return permit;
	}
	
}
