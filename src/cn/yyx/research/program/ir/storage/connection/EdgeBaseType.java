package cn.yyx.research.program.ir.storage.connection;

public enum EdgeBaseType {
	
	SameOperations(1),
	SequentialSameOperation(1 << 1),
	Sequential(1 << 2),
	Self(1 << 3 | Sequential.Value()),
	// Barrier(1 << 5 | 1 << 2),
	
	// these two types are for branch control irs.
	// Branch means BranchControl to IR.
	Branch(1 << 4 | Sequential.Value()),
	// BranchControl means IR to BranchControl.
	BranchControl(1 << 6 | Sequential.Value());
	
	private int type = 0;
	
	private EdgeBaseType(int type) {
		this.type = type;
	}
	
	public int Value(){
        return type;
    }
	
}
