package cn.yyx.research.program.ir.util;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

import cn.yyx.research.program.ir.generation.IRGeneratorForOneProject;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneMethod;

public class IMethodDotPermissionHelper {
	
	public static boolean GainPermissionToGenerateDot(IMethod im) {
		boolean permit = true;
		try {
			// System.out.println(im);
			// System.out.println(im.hasChildren());
			IRForOneMethod ir = IRGeneratorForOneProject.GetInstance().FetchIMethodIR(im);
			if (ir != null && !im.isConstructor() && !ir.IsHasElement()) {
				permit = false;
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return permit;
	}
	
}
