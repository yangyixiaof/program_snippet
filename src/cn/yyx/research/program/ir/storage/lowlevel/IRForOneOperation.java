package cn.yyx.research.program.ir.storage.lowlevel;

import org.eclipse.jdt.core.IMember;

public class IRForOneOperation extends IRForOneUnit {
	
	private String ir = null;
	
	public IRForOneOperation(IMember im, String ir) {
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
