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

	public void setSource(IVNode source) {
		this.source = source;
	}

	public IVNode getTarget() {
		return target;
	}

	public void setTarget(IVNode target) {
		this.target = target;
	}

	public ConnectionInfo getInfo() {
		return info;
	}

	public void setInfo(ConnectionInfo info) {
		this.info = info;
	}
	
}
