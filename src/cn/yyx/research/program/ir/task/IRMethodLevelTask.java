package cn.yyx.research.program.ir.task;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

import cn.yyx.research.program.ir.storage.IRForOneCloseBlockUnit;

public class IRMethodLevelTask extends IRTask {
	
	private IMethod im = null;
	private IRForOneCloseBlockUnit irfom = null;
	
	public IRMethodLevelTask(IType it, IMethod im, IRForOneCloseBlockUnit irfom) {
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
	
	public IRForOneCloseBlockUnit getIrfom() {
		return irfom;
	}

	private void setIrfom(IRForOneCloseBlockUnit irfom) {
		this.irfom = irfom;
	}
	
}
