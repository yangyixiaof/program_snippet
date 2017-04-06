package cn.yyx.research.program.ir.storage.highlevel;

import org.eclipse.jdt.core.IMember;

public class IRForOneJavaElement {
	
	private IMember im = null;
	
	public IRForOneJavaElement(IMember im) {
		this.setIm(im);
	}

	public IMember getIm() {
		return im;
	}

	public void setIm(IMember im) {
		this.im = im;
	}
	
}
