package cn.yyx.research.program.ir;

import org.eclipse.jdt.core.IMethod;

import cn.yyx.research.program.ir.method.IRForOneMethod;

public class IRTask {
	
	private IMethod im = null;
	private int idx = -1;
	private IRForOneMethod irfom = null;
	
	public IRTask(IMethod im, int idx, IRForOneMethod irfom) {
		this.setIm(im);
		this.setIdx(idx);
		this.setIrfom(irfom);
	}

	public IMethod getIm() {
		return im;
	}

	private void setIm(IMethod im) {
		this.im = im;
	}

	public int getIdx() {
		return idx;
	}

	private void setIdx(int idx) {
		this.idx = idx;
	}

	public IRForOneMethod getIrfom() {
		return irfom;
	}

	private void setIrfom(IRForOneMethod irfom) {
		this.irfom = irfom;
	}
	
}
