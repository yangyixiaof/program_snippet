package cn.yyx.research.program.analysis.fulltrace.storage.connection;

import cn.yyx.research.program.analysis.fulltrace.storage.node.DynamicNode;
import cn.yyx.research.program.ir.storage.connection.ConnectionInfo;

public class DynamicConnection {
	
	private DynamicNode source = null;
	private DynamicNode target = null;
	private ConnectionInfo info = null;
//	private int type = -1;
//	private int num = -1;
	
	// , int type, int num
	public DynamicConnection(DynamicNode source, DynamicNode target, ConnectionInfo info) {
		this.setSource(source);
		this.setTarget(target);
		this.setInfo(info);
//		this.setType(type);
//		this.setNum(num);
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
		return new DynamicConnection(source, target, type | dnn.getType(), num + dnn.getNum());
	}

	public int getNum() {
		return num;
	}

	private void setNum(int num) {
		this.num = num;
	}

	public ConnectionInfo getInfo() {
		return info;
	}

	private void setInfo(ConnectionInfo info) {
		this.info = info;
	}
	
}
