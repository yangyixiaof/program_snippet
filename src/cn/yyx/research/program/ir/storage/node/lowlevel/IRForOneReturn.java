package cn.yyx.research.program.ir.storage.node.lowlevel;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;

public class IRForOneReturn extends IRForOneOperation {

	public IRForOneReturn(IRCode parent_env, IJavaElement im, String ir, Class<? extends IIRNodeTask> task_class) {
		super(parent_env, im, ir, task_class);
	}
	
}
