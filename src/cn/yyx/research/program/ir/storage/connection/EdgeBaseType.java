package cn.yyx.research.program.ir.storage.connection;

public enum EdgeBaseType {
	
	SameOperations(1),
	SequentialSameOperation(1 << 1),
	Sequential(1 << 2),
	Self(1 << 3 | 1 << 2),
	Branch(1 << 4 | 1 << 2),
	Barrier(1 << 5 | 1 << 2),
	BranchControl(1 << 6);
	
	private int type = 0;
	
	private EdgeBaseType(int type) {
		this.type = type;
	}
	
	public int Value(){
        return type;
    }
	
}
