package cn.yyx.research.program.ir.task;

import org.eclipse.jdt.core.IType;

import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneMethod;

public class IRClassLevelTask extends IRTask {
	
	private IRForOneMethod irfom = null;
	
	public IRClassLevelTask(IType it, IRForOneMethod irfom) {
		super(it);
		setIrfom(irfom);
	}

	public IRForOneMethod getIrfom() {
		return irfom;
	}

	public void setIrfom(IRForOneMethod irfom) {
		this.irfom = irfom;
	}
	
}
