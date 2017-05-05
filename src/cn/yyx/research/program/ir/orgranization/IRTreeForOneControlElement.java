package cn.yyx.research.program.ir.orgranization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneBranchControl;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class IRTreeForOneControlElement {
	
	protected IJavaElement ije = null;
	protected IRCode parent_env = null;
	
	protected Map<IRForOneBranchControl, List<IRForOneBranchControl>> same_level_branch = new HashMap<IRForOneBranchControl, List<IRForOneBranchControl>>();
	protected Map<IRForOneBranchControl, IRForOneBranchControl> inner_level_branchover = new HashMap<IRForOneBranchControl, IRForOneBranchControl>();
	protected Stack<IRForOneBranchControl> branch_judge_stack = new Stack<IRForOneBranchControl>();
	
	protected Map<ASTNode, List<IRForOneBranchControl>> branch_to_merge = new HashMap<ASTNode, List<IRForOneBranchControl>>();
	
	public IRTreeForOneControlElement(IJavaElement ije, IRCode parent_env) {
		this.ije = ije;
		this.parent_env = parent_env;
	}
	
	public void EnteredOneLogicBlock(ASTNode logic_block, Map<IJavaElement, IRForOneInstruction> logic_env) {
		
	}
	
	public void GoToOneBranch(ASTNode logic_block) {
		
	}
	
	public void ExitOneLogicBlock(ASTNode logic_block) {
		
	}
	
	public Set<IRForOneBranchControl> GetControlNodes() {
		Set<IRForOneBranchControl> result = new HashSet<IRForOneBranchControl>();
		
		return result;
	}
	
}
