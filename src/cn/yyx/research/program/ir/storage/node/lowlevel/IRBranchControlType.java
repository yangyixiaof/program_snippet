package cn.yyx.research.program.ir.storage.node.lowlevel;

import cn.yyx.research.program.ir.IRMeta;

public enum IRBranchControlType {
	
	Branch_Judge(IRMeta.ControlBranchJudge),
	Branch(IRMeta.ControlBranch),
	Branch_Over(IRMeta.ControlBranchOver);
	
	String val = null;
	
	private IRBranchControlType(String val) {
		this.val = val;
	}
	
	public String Value() {
		return val;
	}
	
}
