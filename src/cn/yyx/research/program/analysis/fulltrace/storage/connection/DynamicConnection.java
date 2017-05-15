package cn.yyx.research.program.analysis.fulltrace.storage.connection;

import cn.yyx.research.program.analysis.fulltrace.storage.node.DynamicNode;

public class DynamicConnection {
	
	private DynamicNode source = null;
	private DynamicNode target = null;
	private int type = -1;
	
	public DynamicConnection(DynamicNode source, DynamicNode target, int type) {
		this.setSource(source);
		this.setTarget(target);
		this.setType(type);
	}

	public DynamicNode GetSource() {
		return source;
	}

	private void setSource(DynamicNode source) {
		this.source = source;
	}

	public DynamicNode GetTarget() {
		return target;
	}

	private void setTarget(DynamicNode target) {
		this.target = target;
	}

	public int getType() {
		return type;
	}

	private void setType(int type) {
		this.type = type;
	}
	
	public DynamicConnection Merge(DynamicConnection dnn) {
		return new DynamicConnection(source, target, type | dnn.getType());
	}
	
}
