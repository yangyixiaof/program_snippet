package cn.yyx.research.program.ir.task;

import org.eclipse.jdt.core.IType;

import cn.yyx.research.program.ir.storage.IRForOneCloseBlockUnit;

public class IRClassLevelTask extends IRTask {
	
	private IRForOneCloseBlockUnit irfom = null;
	
	public IRClassLevelTask(IType it, IRForOneCloseBlockUnit irfom) {
		super(it);
		setIrfom(irfom);
	}

	public IRForOneCloseBlockUnit getIrfom() {
		return irfom;
	}

	public void setIrfom(IRForOneCloseBlockUnit irfom) {
		this.irfom = irfom;
	}
	
}
