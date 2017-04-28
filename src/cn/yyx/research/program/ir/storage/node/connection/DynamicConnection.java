package cn.yyx.research.program.ir.storage.node.connection;

import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class DynamicConnection {
	
	private int type = 0;
	private IRForOneInstruction source = null;
	private IRForOneInstruction target = null;
	
	public DynamicConnection(IRForOneInstruction source, IRForOneInstruction target, int type) {
		this.setSource(source);
		this.setTarget(target);
		this.setType(type);
	}

	public IRForOneInstruction getTarget() {
		return target;
	}

	private void setTarget(IRForOneInstruction target) {
		this.target = target;
	}

	public IRForOneInstruction getSource() {
		return source;
	}

	private void setSource(IRForOneInstruction source) {
		this.source = source;
	}

	public int getType() {
		return type;
	}

	private void setType(int type) {
		this.type = type;
	}
	
}
