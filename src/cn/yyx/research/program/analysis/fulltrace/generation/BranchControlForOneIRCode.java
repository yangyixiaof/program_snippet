package cn.yyx.research.program.analysis.fulltrace.generation;

import java.util.List;
import java.util.Stack;

import cn.yyx.research.jdkutil.ListCompare;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneBranchControl;

public class BranchControlForOneIRCode {
	
	IRCode parent_env = null;
	Stack<IRForOneBranchControl> already_branch_path = new Stack<IRForOneBranchControl>();
	
	public BranchControlForOneIRCode(IRCode parent_env) {
		this.parent_env = parent_env;
	}
	
	public boolean IsStartWithTheParameterSpecified(BranchControlForOneIRCode bcfoi) {
		if (bcfoi.already_branch_path.size() <= already_branch_path.size()) {
			List<IRForOneBranchControl> already_list = already_branch_path.subList(0, bcfoi.already_branch_path.size());
			if (ListCompare.TwoListEqual(already_list, bcfoi.already_branch_path)) {
				return true;
			}
		}
		return false;
	}
	
}
