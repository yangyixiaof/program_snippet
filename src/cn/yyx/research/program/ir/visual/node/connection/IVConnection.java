package cn.yyx.research.program.ir.visual.node.connection;

import cn.yyx.research.program.ir.storage.node.connection.StaticConnectionInfo;
import cn.yyx.research.program.ir.visual.node.IVNode;

public class IVConnection {
	
	IVNode source = null;
	IVNode target = null;
	StaticConnectionInfo info = null;
	
	public IVConnection(IVNode source, IVNode target, StaticConnectionInfo info) {
		this.source = source;
		this.target = target;
		this.info = info;
	}
	
	@Override
	public int hashCode() {
		int result = source.hashCode();
		result = result*31 + target.hashCode();
		result = result*31 + info.hashCode();
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IVConnection) {
			IVConnection ivc = (IVConnection)obj;
			if (source == ivc.source && target == ivc.target && info.equals(ivc.info)) {
				return true;
			}
			return false;
		}
		return super.equals(obj);
	}
	
}
