package cn.yyx.research.program.analysis.fulltrace.generation;

import java.util.Stack;

import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneBranchControl;

public class BranchControlForOneIRCode {
	
	IRCode parent_env = null;
	Stack<IRForOneBranchControl> already_branch_path = new Stack<IRForOneBranchControl>();
	
	public BranchControlForOneIRCode(IRCode parent_env) {
		this.parent_env = parent_env;
	}
	
}
