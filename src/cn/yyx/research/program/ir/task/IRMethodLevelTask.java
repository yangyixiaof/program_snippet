package cn.yyx.research.program.ir.task;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

import cn.yyx.research.program.ir.storage.highlevel.IRForOneMethod;

public class IRMethodLevelTask extends IRTask {
	
	private IMethod im = null;
	private IRForOneMethod irfom = null;
	
	public IRMethodLevelTask(IType it, IMethod im, IRForOneMethod irfom) {
		super(it);
		setIm(im);
		setIrfom(irfom);
	}
	
	public IMethod getIm() {
		return im;
	}

	private void setIm(IMethod im) {
		this.im = im;
	}
	
	public IRForOneMethod getIrfom() {
		return irfom;
	}

	private void setIrfom(IRForOneMethod irfom) {
		this.irfom = irfom;
	}
	
}
