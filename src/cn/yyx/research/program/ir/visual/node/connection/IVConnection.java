package cn.yyx.research.program.ir.visual.node.connection;

import cn.yyx.research.program.ir.storage.connection.ConnectionInfo;
import cn.yyx.research.program.ir.visual.node.IVNode;

public class IVConnection {
	
	private IVNode source = null;
	private IVNode target = null;
	private ConnectionInfo info = null;
	
	public IVConnection(IVNode source, IVNode target, ConnectionInfo info) {
		this.setSource(source);
		this.setTarget(target);
		this.setInfo(info);
	}
	
	@Override
	public int hashCode() {
		int result = getSource().hashCode();
		
		// debugging.
		if (getTarget() == null) {
			System.err.println("source:" + source + ";target:" + target + ";info:" + info);
		}
		
		result = result*31 + getTarget().hashCode();
		result = result*31 + getInfo().hashCode();
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IVConnection) {
			IVConnection ivc = (IVConnection)obj;
			if (getSource() == ivc.getSource() && getTarget() == ivc.getTarget() && getInfo().equals(ivc.getInfo())) {
				return true;
			}
			return false;
		}
		return super.equals(obj);
	}

	public IVNode getSource() {
		return source;
	}

	private void setSource(IVNode source) {
		this.source = source;
	}

	public IVNode getTarget() {
		return target;
	}

	private void setTarget(IVNode target) {
		this.target = target;
	}

	public ConnectionInfo getInfo() {
		return info;
	}

	private void setInfo(ConnectionInfo info) {
		this.info = info;
	}
	
	@Override
	public String toString() {
		return source.toString() + "&" + target.toString() + "&" + info;
	}
	
}
