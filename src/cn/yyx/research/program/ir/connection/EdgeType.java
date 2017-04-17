package cn.yyx.research.program.ir.connection;

public enum EdgeType {
	
	Sequential(1),
	Self(1 << 1 | 1),
	Branch(1 << 2);
	
	private int type = 0;
	
	private EdgeType(int type) {
		this.type = type;
	}
	
	public int getType(){
        return type;
    }
	
}
