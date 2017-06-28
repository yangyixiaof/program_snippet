package cn.yyx.research.program.ir.orgranization;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

import cn.yyx.research.program.ir.generation.IRGeneratorForOneProject;
import cn.yyx.research.program.ir.storage.connection.ConnectionInfo;
import cn.yyx.research.program.ir.storage.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.connection.StaticConnection;
import cn.yyx.research.program.ir.storage.node.execution.DefaultINodeTask;
import cn.yyx.research.program.ir.storage.node.execution.SkipSelfTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRBranchControlType;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneBranchControl;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class IRTreeForOneControlElement {
	
	protected IRForOneBranchControl root = null;
	
	protected IJavaElement control_logic_holder_element = null;
	protected IRCode parent_env = null;
	
//	protected Map<ASTNode, IRForOneBranchControl> ast_control_map = new HashMap<ASTNode, IRForOneBranchControl>();
	
	protected Map<IRForOneBranchControl, Stack<IRForOneBranchControl>> inner_level_branch = new HashMap<IRForOneBranchControl, Stack<IRForOneBranchControl>>();
	
//	protected Map<IRForOneBranchControl, ElementBranchInfo> element_has_set_branch = new HashMap<IRForOneBranchControl, ElementBranchInfo>();
//	protected Map<IRForOneBranchControl, Map<IJavaElement, IRForOneInstruction>> branch_var_instr_order = new HashMap<IRForOneBranchControl, Map<IJavaElement, IRForOneInstruction>>();
//	protected Map<IRForOneBranchControl, Map<IJavaElement, IRForOneInstruction>> inner_level_branch_overs_memory = new HashMap<IRForOneBranchControl, Map<IJavaElement, IRForOneInstruction>>();
	
	protected Stack<IRForOneBranchControl> branch_judge_stack = new Stack<IRForOneBranchControl>();
	
//	protected Map<ASTNode, LinkedList<IRForOneBranchControl>> branch_to_merge = new HashMap<ASTNode, LinkedList<IRForOneBranchControl>>();
	
	public IRTreeForOneControlElement(IJavaElement control_logic_holder_element, IRCode parent_env) {
		this.control_logic_holder_element = control_logic_holder_element;
		this.parent_env = parent_env;
		this.root = new IRForOneBranchControl(control_logic_holder_element, parent_env, SkipSelfTask.class, IRBranchControlType.Branch_Over);
		IRForOneBranchControl empty_holder = IRForOneBranchControl.GetEmptyControlHolder();
		this.branch_judge_stack.push(empty_holder);
		// this.element_has_set_branch.put(empty_holder, new ElementBranchInfo());
		Stack<IRForOneBranchControl> irbc_list = new Stack<IRForOneBranchControl>();
		irbc_list.push(this.root);
		this.inner_level_branch.put(empty_holder, irbc_list);
		// this.branch_var_instr_order.put(empty_holder, new HashMap<IJavaElement, IRForOneInstruction>());
	}
	
	public void EnteredOneLogicBlock(ASTNode logic_block, Map<IJavaElement, IRForOneInstruction> logic_env) {
//		if (!branch_judge_stack.isEmpty())
//		{
//			inner_level_branchover.put(branch_judge_stack.peek(), null);
//		}
		IRForOneBranchControl judge = new IRForOneBranchControl(control_logic_holder_element, parent_env, DefaultINodeTask.class, IRBranchControlType.Branch_Judge); // IgnoreSelfTask.class
		// inherit from parent judge.
		if (!branch_judge_stack.isEmpty())
		{
			IRForOneBranchControl last_judge = branch_judge_stack.peek();
			Stack<IRForOneBranchControl> list = inner_level_branch.get(last_judge);
			if (list == null || list.size() == 0)
			{
				System.err.println("What the fuck! judge without branches?");
				System.exit(1);
			}
			IRForOneBranchControl last = list.peek();
			IRGeneratorForOneProject.GetInstance().RegistConnection(new StaticConnection(last, judge, new ConnectionInfo(EdgeBaseType.Self.Value())));
		}
		
		branch_judge_stack.push(judge);
		// element_has_set_branch.put(judge, new ElementBranchInfo());
		// branch_var_instr_order.put(judge, logic_env);
		inner_level_branch.put(judge, new Stack<IRForOneBranchControl>());
		
		// add connections from ir-eles_tokens to branch_control_judge.
		Set<IJavaElement> lkeys = logic_env.keySet();
		Iterator<IJavaElement> litr = lkeys.iterator();
		while (litr.hasNext())
		{
			IJavaElement lje = litr.next();
			IRForOneInstruction ir = logic_env.get(lje);
			IRGeneratorForOneProject.GetInstance().RegistConnection(new StaticConnection(ir, judge, new ConnectionInfo(EdgeBaseType.BranchControl.Value())));
		}
	}
	
	public void GoToOneBranch(ASTNode logic_block) {
		IRForOneBranchControl irbc = branch_judge_stack.peek();
		// ast_control_map.get(logic_block);
		Stack<IRForOneBranchControl> list = inner_level_branch.get(irbc);
		IRForOneBranchControl irbc_bc = new IRForOneBranchControl(control_logic_holder_element, parent_env, DefaultINodeTask.class, IRBranchControlType.Branch);
		list.add(irbc_bc);
		IRGeneratorForOneProject.GetInstance().RegistConnection(new StaticConnection(irbc, irbc_bc, new ConnectionInfo(EdgeBaseType.Self.Value())));
		UpdateIRControlBranchInstructionOrder();
		// inner_level_branch_overs_memory.put(irbc, null);
	}
	
	public void ExitOneLogicBlock(ASTNode logic_block, List<IRForOneInstruction> branch_overs) {
		// handle branch_control graph.
		IRForOneBranchControl branch_over = new IRForOneBranchControl(control_logic_holder_element, parent_env, SkipSelfTask.class, IRBranchControlType.Branch_Over);
		IRForOneBranchControl irbc = branch_judge_stack.pop();
		// ast_control_map.remove(logic_block);
		Stack<IRForOneBranchControl> list = inner_level_branch.remove(irbc);
		Iterator<IRForOneBranchControl> itr = list.iterator();
		while (itr.hasNext())
		{
			IRForOneBranchControl irbc_bc = itr.next();
			IRGeneratorForOneProject.GetInstance().RegistConnection(new StaticConnection(irbc_bc, branch_over, new ConnectionInfo(EdgeBaseType.Self.Value())));
		}
		list.clear();
		IRForOneBranchControl now_irbc = branch_judge_stack.peek();
		Stack<IRForOneBranchControl> now_list = inner_level_branch.get(now_irbc);
		now_list.pop();
		now_list.push(branch_over);
//		if (!branch_judge_stack.isEmpty())
//		{
//			inner_level_branchover.put(branch_judge_stack.peek(), branch_over);
//		}
		
		// handle ir-token graph.
		if (branch_overs.size() > 0) {
			Map<IJavaElement, IRForOneInstruction> branch_instr_order = new HashMap<IJavaElement, IRForOneInstruction>();
			Iterator<IRForOneInstruction> boitr = branch_overs.iterator();
			while (boitr.hasNext()) {
				IRForOneInstruction irfoi = boitr.next();
				branch_instr_order.put(irfoi.getIm(), irfoi);
			}
			Set<IJavaElement> lkeys = branch_instr_order.keySet();
			Iterator<IJavaElement> litr = lkeys.iterator();
			while (litr.hasNext())
			{
				IJavaElement lje = litr.next();
				IRForOneInstruction ir = branch_instr_order.get(lje);
				IRGeneratorForOneProject.GetInstance().RegistConnection(new StaticConnection(ir, branch_over, new ConnectionInfo(EdgeBaseType.BranchControl.Value())));
			}
			// inner_level_branch_overs_memory.put(now_irbc, branch_instr_order);
		}
	}
	
	public IRForOneBranchControl GetControlNode() {
		if (!branch_judge_stack.isEmpty())
		{
			IRForOneBranchControl now_bc_judge = branch_judge_stack.peek();
//			IRForOneBranchControl inner_over = inner_level_branchover.get(now_bc_judge);
//			if (inner_over != null)
//			{
//				return inner_over;
//			}
			Stack<IRForOneBranchControl> in_level = inner_level_branch.get(now_bc_judge);
			if (in_level.size() == 0) {
				new Exception().printStackTrace();
				System.err.println("Strange! inner_level_branch is null!");
				System.exit(1);
			}
			return in_level.peek();
		}
		System.err.println("Strange! Control node should not be null!");
		System.exit(1);
		return null;
	}
	
	public IRForOneBranchControl GetRoot() {
		return root;
	}

	private void UpdateIRControlBranchInstructionOrder() {
		// IRForOneBranchControl irbc = branch_judge_stack.peek();
		// ElementBranchInfo element_branch_info = element_has_set_branch.get(irbc);
		// element_branch_info.ClearElementChanged();
		// IRForOneBranchControl control_node = GetControlNode();
		// if (control_node != null) {
		//	element_branch_info.PutElementChanged(control_logic_holder_element, control_node);
		// }
	}
	
	protected Map<IRForOneBranchControl, Map<IJavaElement, Boolean>> branch_control_has_set = new HashMap<IRForOneBranchControl, Map<IJavaElement, Boolean>>();
	
	public Map<IJavaElement, IRForOneInstruction> GetBranchInstructionOrder(IJavaElement ije) {
		// IRForOneBranchControl irbc = branch_judge_stack.peek();
		Map<IJavaElement, IRForOneInstruction> branch_instr_order = new HashMap<IJavaElement, IRForOneInstruction>();
		IRForOneBranchControl control_node = GetControlNode();
		Map<IJavaElement, Boolean> has_set = branch_control_has_set.get(control_node);
		if (has_set == null) {
			has_set = new HashMap<IJavaElement, Boolean>();
			branch_control_has_set.put(control_node, has_set);
		}
		if (has_set.get(ije) == null) {
			branch_instr_order.put(control_logic_holder_element, control_node);
			has_set.put(ije, true);
		}
		// inner_level_branch_overs_memory.get(irbc);
		// if (branch_instr_order == null) {
		//	branch_instr_order = branch_var_instr_order.get(irbc);
		// }
		return branch_instr_order;
	}
	
//	public ElementBranchInfo GetElementBranchInfo() {
//		IRForOneBranchControl irbc = branch_judge_stack.peek();
//		ElementBranchInfo element_branch_info = element_has_set_branch.get(irbc);
//		return element_branch_info;
//	}
	
}
