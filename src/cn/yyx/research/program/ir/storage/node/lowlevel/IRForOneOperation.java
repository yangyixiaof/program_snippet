package cn.yyx.research.program.ir.storage.node.lowlevel;

import org.eclipse.jdt.core.IJavaElement;

public class IRForOneOperation extends IRForOneJavaInstruction {
	
	private String ir = null;
	
	public IRForOneOperation(IJavaElement im, String ir) {
		super(im);
		this.setIr(ir);
	}

	public String getIr() {
		return ir;
	}

	public void setIr(String ir) {
		this.ir = ir;
	}

}
