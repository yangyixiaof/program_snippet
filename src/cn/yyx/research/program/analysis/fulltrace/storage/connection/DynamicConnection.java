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

	public void setType(int type) {
		this.type = type;
	}
	
	public DynamicConnection Merge(DynamicConnection dnn) {
		if (!source.equals(dnn.source) || !target.equals(dnn.target)) {
			System.err.println("source is not source or target is not target.");
			System.exit(1);
		}
		return new DynamicConnection(source, target, type | dnn.getType());
	}
	
}
