package cn.yyx.research.program.ir.storage.node.connection;

public class StaticConnectionInfo {
	
	private int type = 0;
	
	public StaticConnectionInfo(int type) {
		this.setType(type);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StaticConnectionInfo) {
			StaticConnectionInfo sci = (StaticConnectionInfo)obj;
			if (type == sci.type) {
				return true;
			}
			return false;
		}
		return super.equals(obj);
	}
	
}
