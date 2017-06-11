package cn.yyx.research.program.ir.storage.node.connection;

public class StaticConnectionInfo {
	
	private int type = 0;
	private int num = 0;
	
	public StaticConnectionInfo(int type, int num) {
		this.setType(type);
		this.setNum(num);
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

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}
	
}
