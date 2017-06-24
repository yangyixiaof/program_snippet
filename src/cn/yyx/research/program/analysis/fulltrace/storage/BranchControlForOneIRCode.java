package cn.yyx.research.program.analysis.fulltrace.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.jdkutil.ListCompare;
import cn.yyx.research.program.analysis.fulltrace.storage.helper.MapSetUtil;
import cn.yyx.research.program.analysis.fulltrace.storage.node.DynamicNode;
import cn.yyx.research.program.ir.generation.IRGeneratorForOneProject;
import cn.yyx.research.program.ir.storage.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneBranchControl;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class BranchControlForOneIRCode {
	
	private IRCode parent_env = null;
	private Stack<IRForOneBranchControl> already_branch_path = new Stack<IRForOneBranchControl>();
	private Stack<Map<IJavaElement, Set<DynamicNode>>> last_instrs = new Stack<Map<IJavaElement, Set<DynamicNode>>>();
	private Map<IJavaElement, Set<DynamicNode>> out_last_instrs = new HashMap<IJavaElement, Set<DynamicNode>>();
	
	public BranchControlForOneIRCode(IRCode parent_env) {
		this.SetParentEnv(parent_env);
	}
	
	public void Push(IRForOneBranchControl bc) {
		already_branch_path.push(bc);
		HashMap<IJavaElement, Set<DynamicNode>> one_last_instrs = new HashMap<IJavaElement, Set<DynamicNode>>();
		if (!last_instrs.isEmpty()) {
			one_last_instrs.putAll(last_instrs.peek());
		}
		last_instrs.push(one_last_instrs);
	}
	
	public Collection<IRForOneBranchControl> GetAllBranchControls() {
		return already_branch_path;
	}
	
	public void Pop() {
		IRForOneBranchControl bc = already_branch_path.pop();
		Set<IRForOneInstruction> out_conns = IRGeneratorForOneProject.GetInstance().GetOutINodesByContainingSpecificType(bc, EdgeBaseType.BranchControl.Value());
		Map<IJavaElement, Set<DynamicNode>> pop_cnt = last_instrs.pop();
		if (out_conns == null || out_conns.isEmpty()) {
			MapSetUtil.HandleTwoMapSet(out_last_instrs, pop_cnt);
		}
	}
	
	public IRForOneBranchControl LastBranchControl() {
		if (already_branch_path.isEmpty()) {
			return null;
		}
		return already_branch_path.peek();
	}
	
	public Set<DynamicNode> LastLastInstructions(IJavaElement ije) {
		Set<DynamicNode> ir_set = last_instrs.peek().get(ije);
		if (ir_set == null) {
			ir_set = new HashSet<DynamicNode>();
			last_instrs.peek().put(ije, ir_set);
		}
		return ir_set;
	}
	
	public Map<IJavaElement, Set<DynamicNode>> LastLastInstructions() {
		if (last_instrs.isEmpty()) {
			return null;
		}
		return last_instrs.peek();
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
	
	public void InheritFromExecutedIRCode(BranchControlForOneIRCode executed) {
		MapSetUtil.HandleTwoMapSet(last_instrs.peek(), executed.out_last_instrs);
	}

	public void HandleOutControl(DynamicNode new_dn) {
		IRForOneInstruction instr = new_dn.getInstr();
		if (parent_env.IsOutControl(instr)) {
			IJavaElement ije = instr.getIm();
			Set<DynamicNode> out_nodes = out_last_instrs.get(ije);
			if (out_nodes == null) {
				out_nodes = new HashSet<DynamicNode>();
			}
			out_last_instrs.put(ije, out_nodes);
			out_nodes.add(new_dn);
		}
	}
	
}
