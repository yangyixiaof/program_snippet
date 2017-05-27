package cn.yyx.research.program.ir.storage.node.connection;

public enum EdgeBaseType {
	
	SameOperations(1),
	Sequential(1 << 1),
	Self(1 << 2 | 1 << 1),
	Branch(1 << 3),
	Barrier(1 << 4),
	BranchControl(1 << 5);
	
	private int type = 0;
	
	private EdgeBaseType(int type) {
		this.type = type;
	}
	
	public int Value(){
        return type;
    }
	
}
