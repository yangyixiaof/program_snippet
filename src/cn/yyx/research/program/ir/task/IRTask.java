package cn.yyx.research.program.ir.task;

import org.eclipse.jdt.core.IType;

public abstract class IRTask {
	
	private IType it = null;
	
	public IRTask(IType it) {
		this.setIt(it);
	}

	public IType getIt() {
		return it;
	}

	public void setIt(IType it) {
		this.it = it;
	}
	
}
