package cn.yyx.research.program.ir.storage.node.connection;

import cn.yyx.research.program.ir.storage.node.IIRNode;

public class DynamicConnection {
	
	private int type = 0;
	private IIRNode source = null;
	private IIRNode target = null;
	
	public DynamicConnection(IIRNode source, IIRNode target, int type) {
		this.setSource(source);
		this.setTarget(target);
		this.setType(type);
	}

	public IIRNode getTarget() {
		return target;
	}

	private void setTarget(IIRNode target) {
		this.target = target;
	}

	public IIRNode getSource() {
		return source;
	}

	private void setSource(IIRNode source) {
		this.source = source;
	}

	public int getType() {
		return type;
	}

	private void setType(int type) {
		this.type = type;
	}
	
}
