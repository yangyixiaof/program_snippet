package cn.yyx.research.program.ir.orgranization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

import cn.yyx.research.program.ir.generation.IRGeneratorForOneProject;
import cn.yyx.research.program.ir.storage.node.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnection;
import cn.yyx.research.program.ir.storage.node.execution.IgnoreSelfTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRBranchControlType;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneBranchControl;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class IRTreeForOneControlElement {
	
	protected IRForOneBranchControl root = null;
	
	protected IJavaElement ije = null;
	protected IRCode parent_env = null;
	
	protected Map<ASTNode, IRForOneBranchControl> ast_control_map = new HashMap<ASTNode, IRForOneBranchControl>();
	protected Map<IRForOneBranchControl, LinkedList<IRForOneBranchControl>> same_level_branch = new HashMap<IRForOneBranchControl, LinkedList<IRForOneBranchControl>>();
	protected Map<IRForOneBranchControl, IRForOneBranchControl> inner_level_branchover = new HashMap<IRForOneBranchControl, IRForOneBranchControl>();
	protected Stack<IRForOneBranchControl> branch_judge_stack = new Stack<IRForOneBranchControl>();
	
	protected Map<ASTNode, LinkedList<IRForOneBranchControl>> branch_to_merge = new HashMap<ASTNode, LinkedList<IRForOneBranchControl>>();
	
	public IRTreeForOneControlElement(IJavaElement ije, IRCode parent_env) {
		this.ije = ije;
		this.parent_env = parent_env;
		this.root = new IRForOneBranchControl(ije, parent_env, IgnoreSelfTask.class, IRBranchControlType.Branch_Over);
	}
	
	public void EnteredOneLogicBlock(ASTNode logic_block, Map<IJavaElement, IRForOneInstruction> logic_env) {
		if (!branch_judge_stack.isEmpty())
		{
			inner_level_branchover.remove(branch_judge_stack.peek());
		}
		IRForOneBranchControl judge = new IRForOneBranchControl(ije, parent_env, IgnoreSelfTask.class, IRBranchControlType.Branch_Judge);
		IRForOneBranchControl last = null;
		if (!branch_judge_stack.isEmpty())
		{
			IRForOneBranchControl last_judge = branch_judge_stack.peek();
			LinkedList<IRForOneBranchControl> list = same_level_branch.get(last_judge);
			if (list == null || list.size() == 0)
			{
				System.err.println("What the fuck! judge without branches?");
				System.exit(1);
			}
			last = list.getLast();
		}
		IRGeneratorForOneProject.GetInstance().RegistConnection(new StaticConnection(last, judge, EdgeBaseType.BranchControl.getType()));
		
		if (branch_judge_stack.isEmpty())
		{
			branch_judge_stack.push(judge);
		}
		ast_control_map.put(logic_block, judge);
		this.same_level_branch.put(judge, new LinkedList<IRForOneBranchControl>());
		
		Set<IJavaElement> lkeys = logic_env.keySet();
		Iterator<IJavaElement> litr = lkeys.iterator();
		while (litr.hasNext())
		{
			IJavaElement lje = litr.next();
			IRForOneInstruction ir = logic_env.get(lje);
			IRGeneratorForOneProject.GetInstance().RegistConnection(new StaticConnection(ir, judge, EdgeBaseType.BranchControl.getType()));
		}
	}
	
	public void GoToOneBranch(ASTNode logic_block) {
		IRForOneBranchControl irbc = ast_control_map.get(logic_block);
		LinkedList<IRForOneBranchControl> list = same_level_branch.get(irbc);
		IRForOneBranchControl irbc_bc = new IRForOneBranchControl(ije, parent_env, IgnoreSelfTask.class, IRBranchControlType.Branch);
		list.add(irbc_bc);
	}
	
	public void ExitOneLogicBlock(ASTNode logic_block) {
		IRForOneBranchControl branch_over = new IRForOneBranchControl(ije, parent_env, IgnoreSelfTask.class, IRBranchControlType.Branch_Over);
		IRForOneBranchControl irbc = ast_control_map.remove(logic_block);
		LinkedList<IRForOneBranchControl> list = same_level_branch.remove(irbc);
		Iterator<IRForOneBranchControl> itr = list.iterator();
		while (itr.hasNext())
		{
			IRForOneBranchControl irbc_bc = itr.next();
			IRGeneratorForOneProject.GetInstance().RegistConnection(new StaticConnection(irbc_bc, branch_over, EdgeBaseType.BranchControl.getType()));
		}
		if (!branch_judge_stack.isEmpty())
		{
			inner_level_branchover.put(branch_judge_stack.peek(), branch_over);
		}
	}
	
	public Set<IRForOneBranchControl> GetControlNodes() {
		Set<IRForOneBranchControl> result = new HashSet<IRForOneBranchControl>();
		if (!branch_judge_stack.isEmpty())
		{
			IRForOneBranchControl now_bc_judge = branch_judge_stack.peek();
			result.add(same_level_branch.get(now_bc_judge).getLast());
			IRForOneBranchControl inner_over = inner_level_branchover.get(now_bc_judge);
			if (inner_over != null)
			{
				result.add(inner_over);
			}
		}
		return result;
	}
	
}
