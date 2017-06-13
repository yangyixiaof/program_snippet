package cn.yyx.research.program.ir.storage.node.lowlevel;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;

public class IRForOneOperation extends IRForOneInstruction {
	
	private String ir = null;
	
	public IRForOneOperation(IRCode parent_env, IJavaElement im, String ir, Class<? extends IIRNodeTask> task_class) {
		super(im, parent_env, task_class);
		this.setIr(ir);
	}

	public String getIr() {
		return ir;
	}

	public void setIr(String ir) {
		this.ir = ir;
	}

	@Override
	public String ToVisual() {
		return im.getElementName() + "$Op:" + ir;
	}

	@Override
	public String toString() {
		return ToVisual();
	}
	
}
