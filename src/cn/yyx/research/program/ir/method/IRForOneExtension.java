package cn.yyx.research.program.ir.method;

import org.eclipse.jdt.core.IMethod;

public class IRForOneExtension extends IRForOneUnit {
	
	private IRForOneMethod method = null;
	
	public IRForOneExtension(IMethod im, int start, int end, IRForOneMethod method, IRInstrKind ir_kind) {
		super(im, start, end, ir_kind);
		this.setMethod(method);
	}

	public IRForOneMethod getMethod() {
		return method;
	}

	public void setMethod(IRForOneMethod method) {
		this.method = method;
	}
	
}
