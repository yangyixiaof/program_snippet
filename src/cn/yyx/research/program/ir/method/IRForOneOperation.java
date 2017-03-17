package cn.yyx.research.program.ir.method;

import org.eclipse.jdt.core.IMethod;

public class IRForOneOperation extends IRForOneUnit {
	
	private String ir = null;
	
	public IRForOneOperation(IMethod im, int start, int end, String ir) {
		super(im, start, end);
		this.setIr(ir);
	}

	public String getIr() {
		return ir;
	}

	public void setIr(String ir) {
		this.ir = ir;
	}

}
