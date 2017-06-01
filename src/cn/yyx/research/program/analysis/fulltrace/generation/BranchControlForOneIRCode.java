package cn.yyx.research.program.analysis.fulltrace.generation;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.jdkutil.ListCompare;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneBranchControl;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class BranchControlForOneIRCode {
	
	private IRCode parent_env = null;
	private Stack<IRForOneBranchControl> already_branch_path = new Stack<IRForOneBranchControl>();
	private Stack<Map<IJavaElement, Set<IRForOneInstruction>>> last_instrs = new Stack<Map<IJavaElement, Set<IRForOneInstruction>>>();
	
	public BranchControlForOneIRCode(IRCode parent_env) {
		this.SetParentEnv(parent_env);
	}
	
	public void Push(IRForOneBranchControl bc) {
		already_branch_path.push(bc);
		HashMap<IJavaElement, Set<IRForOneInstruction>> one_last_instrs = new HashMap<IJavaElement, Set<IRForOneInstruction>>();
		if (!last_instrs.isEmpty()) {
			one_last_instrs.putAll(last_instrs.peek());
		}
		last_instrs.push(one_last_instrs);
	}
	
	public Collection<IRForOneBranchControl> GetAllBranchControls() {
		return already_branch_path;
	}
	
	public void Pop() {
		already_branch_path.pop();
		last_instrs.pop();
	}
	
	public IRForOneBranchControl Peek() {
		return already_branch_path.peek();
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

	public IRCode GetParentEnv() {
		return parent_env;
	}
	
	public void SetParentEnv(IRCode parent_env) {
		this.parent_env = parent_env;
	}
	
}
