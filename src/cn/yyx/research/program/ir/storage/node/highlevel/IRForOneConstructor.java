package cn.yyx.research.program.ir.storage.node.highlevel;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

public class IRForOneConstructor extends IRForOneMethod {
	
	private IType wrap_class = null;
	
	public IRForOneConstructor(IMethod im, IType wrap_class) {
		super(im);
		this.setWrap_class(wrap_class);
	}

	public IType getWrap_class() {
		return wrap_class;
	}

	private void setWrap_class(IType wrap_class) {
		this.wrap_class = wrap_class;
	}
	
}
