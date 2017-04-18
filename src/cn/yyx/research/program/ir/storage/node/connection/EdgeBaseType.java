package cn.yyx.research.program.ir.storage.node.connection;

public enum EdgeBaseType {
	
	Sequential(1),
	Self(1 << 1 | 1),
	Branch(1 << 2);
	
	private int type = 0;
	
	private EdgeBaseType(int type) {
		this.type = type;
	}
	
	public int getType(){
        return type;
    }
	
}
