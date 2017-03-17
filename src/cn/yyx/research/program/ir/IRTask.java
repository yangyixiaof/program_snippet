package cn.yyx.research.program.ir;

import org.eclipse.jdt.core.IMethod;

import cn.yyx.research.program.ir.method.IRForOneMethod;

public class IRTask {
	
	private IMethod im = null;
	private int idx = -1;
	private IRForOneMethod irfom = null;
	
	public IRTask(IMethod im, int idx) {
		this.setIm(im);
		this.setIdx(idx);
	}

	public IMethod getIm() {
		return im;
	}

	public void setIm(IMethod im) {
		this.im = im;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}
	
}
