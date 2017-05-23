package cn.yyx.research.program.analysis.fulltrace.generation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

import cn.yyx.research.program.analysis.fulltrace.storage.FullTrace;
import cn.yyx.research.program.analysis.fulltrace.storage.node.DynamicNode;
import cn.yyx.research.program.ir.generation.IRGeneratorForOneProject;
import cn.yyx.research.program.ir.orgranization.IRTreeForOneControlElement;
import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnection;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnectionInfo;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneClass;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneConstructor;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneField;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneMethod;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneBranchControl;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneSourceMethodInvocation;

public class CodeOnOneTraceGenerator {
	
	private MethodSelection method_selection = null;
	private FullTrace full_trace = new FullTrace();
	private Map<IRCode, Stack<BranchControlForOneIRCode>> branch_control_stack_foreach_ircode = new HashMap<IRCode, Stack<BranchControlForOneIRCode>>();
	// private Stack<BranchControlForOneIRCode> branch_control_stack = new Stack<BranchControlForOneIRCode>();
	private Map<IRCode, Integer> method_max_id = new HashMap<IRCode, Integer>();
	private Map<IRCode, Stack<Map<IRForOneSourceMethodInvocation, Integer>>> method_id = new HashMap<IRCode, Stack<Map<IRForOneSourceMethodInvocation, Integer>>>();
	
	public CodeOnOneTraceGenerator(MethodSelection ms) {
		this.method_selection = ms;
		IRForOneMethod root_code = IRGeneratorForOneProject.GetInstance().FetchIMethodIR(ms.GetRoot());
		int env_idx = GetID(root_code);
		GenerateFullTrace(root_code, null, env_idx, full_trace);
	}
	
	public FullTrace GetFullTrace() {
		return full_trace;
	}
	
	private int GetID(IRCode irc)
	{
		Integer id = method_max_id.get(irc);
		if (id == null) {
			id = 0;
		}
		id++;
		method_max_id.put(irc, id);
		return id;
	}
	
	private void GenerateFullTrace(IRCode irfom, IRForOneSourceMethodInvocation now_instruction, int env_idx, FullTrace ft)
	{
		// Solved. handle constructor here.
		if (irfom instanceof IRForOneConstructor) {
			IRForOneConstructor irfoc = (IRForOneConstructor)irfom;
			IType it = irfoc.getWrap_class();
			IRForOneClass irfot = IRGeneratorForOneProject.GetInstance().GetClassIR(it);
			if (irfot != null) {
				IRForOneField field_level = irfot.GetFieldLevel();
				if (field_level != null) {
					GenerateFullTrace(irfoc, null, GetID(irfoc), ft);
				}
			}
		}
		
		Stack<Map<IRForOneSourceMethodInvocation, Integer>> id_stack = method_id.get(irfom);
		if (id_stack == null) {
			id_stack = new Stack<Map<IRForOneSourceMethodInvocation, Integer>>();
			method_id.put(irfom, id_stack);
		}
		id_stack.push(new HashMap<IRForOneSourceMethodInvocation, Integer>());
		
		IRTreeForOneControlElement control_ir = irfom.GetControlLogicHolderElementIR();
		IRForOneBranchControl control_root = control_ir.GetRoot();
		
		HashMap<IRCode, Stack<BranchControlForOneIRCode>> copy_env = new HashMap<IRCode, Stack<BranchControlForOneIRCode>>(branch_control_stack_foreach_ircode);
//		int last_size = branch_control_stack.size();
//		List<IRForOneBranchControl> new_list = branch_control_stack.subList(0, last_size);
		
		ExecutionMemory memory = new ExecutionMemory();
		HandleCallerToCallee(irfom, now_instruction, ft, env_idx, memory);
		
		Stack<BranchControlForOneIRCode> branch_control_stack = branch_control_stack_foreach_ircode.get(irfom);
		if (branch_control_stack == null) {
			branch_control_stack = new Stack<BranchControlForOneIRCode>();
			branch_control_stack_foreach_ircode.put(irfom, branch_control_stack);
		}
		Stack<BranchControlForOneIRCode> branch_control_stack_copy = new Stack<BranchControlForOneIRCode>();
		branch_control_stack_copy.addAll(branch_control_stack);
		BranchControlForOneIRCode branch_control = new BranchControlForOneIRCode(irfom);
		branch_control_stack.push(branch_control);
		DepthFirstToVisitControlLogic(ft, branch_control_stack_copy, branch_control, control_root, irfom, memory, env_idx);
		branch_control_stack.pop();
		
		method_id.get(irfom).pop();
//		branch_control_stack.clear();
//		branch_control_stack.addAll(new_list);
		branch_control_stack_foreach_ircode.clear();
		branch_control_stack_foreach_ircode.putAll(copy_env);
	}
	
	private void DepthFirstToVisitControlLogic(FullTrace ft, Stack<BranchControlForOneIRCode> branch_control_stack_copy, BranchControlForOneIRCode branch_control, IRForOneBranchControl now_control_root, IRCode irfom, ExecutionMemory execution_memory, int env_idx)
	{
		Set<IRForOneBranchControl> already_visited = new HashSet<IRForOneBranchControl>();
		branch_control.already_branch_path.push(now_control_root);
		Iterator<BranchControlForOneIRCode> bitr = branch_control_stack_copy.iterator();
		while (bitr.hasNext()) {
			BranchControlForOneIRCode bcfoi = bitr.next();
			if (bcfoi.IsStartWithTheParameterSpecified(branch_control)) {
				Set<IRForOneBranchControl> children = IRGeneratorForOneProject.GetInstance().GetChildrenOfControl(bcfoi.already_branch_path.lastElement());
				children.addAll(bcfoi.already_branch_path);
				already_visited.addAll(children);
			}
		}
		Set<IRForOneBranchControl> curr_visited = new HashSet<IRForOneBranchControl>();
		curr_visited.addAll(branch_control.already_branch_path);
		curr_visited.addAll(IRGeneratorForOneProject.GetInstance().GetChildrenOfControl(now_control_root));
		if (already_visited.containsAll(curr_visited)) {
			branch_control.already_branch_path.pop();
			return;
		}
		
		Set<StaticConnection> out_conns = IRGeneratorForOneProject.GetInstance().GetOutConnections(now_control_root);
		execution_memory.executed_conns.addAll(out_conns);
		
		BreadthFirstToVisitIR(ft, execution_memory, env_idx);
		
		IRGeneratorForOneProject irproj = IRGeneratorForOneProject.GetInstance();
		Set<IRForOneInstruction> control_outs = irproj.GetOutINodesByContainingSpecificType(now_control_root, EdgeBaseType.BranchControl.Value());
		Iterator<IRForOneInstruction> coitr = control_outs.iterator();
		while (coitr.hasNext())
		{
			IRForOneInstruction irfoi = coitr.next();
			if (!(irfoi instanceof IRForOneBranchControl))
			{
				System.err.println("Not IRForOneBranchControl? What the fuck!");
				System.exit(1);
			}
			IRForOneBranchControl ir_control = (IRForOneBranchControl)irfoi;
			DepthFirstToVisitControlLogic(ft, branch_control_stack_copy, branch_control, ir_control, irfom, execution_memory, env_idx);
		}
		
		branch_control.already_branch_path.pop();
	}
	
	private void BreadthFirstToVisitIR(FullTrace ft, ExecutionMemory memory, int env_idx)
	{
		while (true)
		{
			boolean could_continue = false;
			Set<IJavaElement> exe_keys = memory.last_execution.keySet();
			Iterator<IJavaElement> exe_itr = exe_keys.iterator();
			while (exe_itr.hasNext())
			{
				IJavaElement ije = exe_itr.next();
				IRForOneInstruction inode = memory.last_execution.get(ije);
				
				// Solved. handle operations first, remember to handle IRForMethodInvocation which is totally different.
				
				Set<StaticConnection> in_conns = ObtainExecutionPermission(inode, memory.executed_conns);
				could_continue = could_continue || ((in_conns != null) && (in_conns.size() > 0));
				if (in_conns != null && in_conns.size() > 0)
				{
					Iterator<StaticConnection> in_itr = in_conns.iterator();
					while (in_itr.hasNext())
					{
						StaticConnection sc = in_itr.next();
						HandleStaticConnectionForTarget(ft, sc.getSource(), sc.getTarget(), sc.GetStaticConnectionInfo(), env_idx);
						HandleStaticConnectionForSource(ft, sc.getSource(), sc.getTarget(), sc.GetStaticConnectionInfo(), env_idx);
					}
				}
			}
			if (!could_continue)
			{
				break;
			}
		}
	}
	
	private void HandleStaticConnectionForTarget(FullTrace ft, IRForOneInstruction source, IRForOneInstruction target, StaticConnectionInfo sc_info, int env_idx)
	{
		if (target instanceof IRForOneSourceMethodInvocation) {
			IRForOneSourceMethodInvocation irfosm = (IRForOneSourceMethodInvocation)target;
			IMethod select_method = method_selection.GetMethodSelection(irfosm);
			IRForOneMethod select_method_ir = IRGeneratorForOneProject.GetInstance().FetchIMethodIR(select_method);
			int id = GetID(select_method_ir);
			method_id.get(irfosm.getParentEnv()).peek().put(irfosm, id);
			GenerateFullTrace(select_method_ir, irfosm, id, ft);
		}
	}
	
	private void HandleStaticConnectionForSource(FullTrace ft, IRForOneInstruction source, IRForOneInstruction target, StaticConnectionInfo sc_info, int env_idx)
	{
		DynamicNode source_dn = new DynamicNode(source, source.getParentEnv(), env_idx);
		DynamicNode target_dn = new DynamicNode(target, target.getParentEnv(), env_idx);
		ft.NodeCreated(target.getIm(), target_dn);
		IIRNodeTask out_task = source.GetOutConnectionMergeTask();
		if (source instanceof IRForOneSourceMethodInvocation) {
			IRForOneSourceMethodInvocation irmethod_source = (IRForOneSourceMethodInvocation) source;
			IMethod select_im = method_selection.GetMethodSelection(irmethod_source);
			IRForOneMethod im = IRGeneratorForOneProject.GetInstance().FetchIMethodIR(select_im);
			Map<IJavaElement, IRForOneInstruction> out_nodes = im.GetOutNodes();
			Collection<IRForOneInstruction> ovals = out_nodes.values();
			Iterator<IRForOneInstruction> oitr = ovals.iterator();
			while (oitr.hasNext())
			{
				IRForOneInstruction irfoi = oitr.next();
				DynamicNode precise_source_dn = new DynamicNode(irfoi, irfoi.getParentEnv(), method_id.get(irmethod_source.getParentEnv()).peek().get(irmethod_source));
				IIRNodeTask precise_task = irfoi.GetOutConnectionMergeTask();
				precise_task.HandleOutConnection(precise_source_dn, target_dn, sc_info, ft);
			}
		} else {
			out_task.HandleOutConnection(source_dn, target_dn, sc_info, ft);
		}
	}
	
	private Set<StaticConnection> ObtainExecutionPermission(IRForOneInstruction one_instr_pc, Set<StaticConnection> executed_conns)
	{
		Set<StaticConnection> in_conns = IRGeneratorForOneProject.GetInstance().GetInConnections(one_instr_pc);
		Iterator<StaticConnection> iitr = in_conns.iterator();
		while (iitr.hasNext())
		{
			StaticConnection iirn = iitr.next();
			if (!executed_conns.contains(iirn))
			{
				return null;
			}
		}
		return in_conns;
	}
	
	private void HandleCallerToCallee(IRCode irc, IRForOneSourceMethodInvocation wrap_node, FullTrace ft_run, int env_idx, ExecutionMemory memory)
	{
		// Solved. need to handle hidden inherit link.
		// Solved. should handle the before connection to parameters.
		
		// Solved: no need to do that. remember to add root dynamic node if now_instruction is null which means now_method is the root or field initialization.
		
		// wrap_node is used only for the first phase.
		// now handle wrap_node for depending on method_parameter_connection.
		// Set<IIRNode> executed_instrs = new HashSet<IIRNode>();
	//	Set<IRForOneInstruction> instr_pc = new HashSet<IRForOneInstruction>();
		Set<IJavaElement> eles = irc.GetAllElements();
		Iterator<IJavaElement> eitr = eles.iterator();
		while (eitr.hasNext())
		{
			IJavaElement ije = eitr.next();
			memory.last_execution.put(ije, irc.GetFirstIRTreeNode(ije));
	//		instr_pc.add(irc.GetFirstIRTreeNode(ije));
		}
		
		if (wrap_node != null) {
			// irc must be of type IRForOneMethod.
			IRForOneMethod irfom = (IRForOneMethod) irc;
			List<IJavaElement> params = irfom.GetParameters();
			List<IJavaElement> non_null_params = new LinkedList<IJavaElement>();
			Iterator<IRForOneInstruction> param_depend_itr = wrap_node.ParameterDependentNodeIterator();
			
			while (param_depend_itr.hasNext())
			{
				IRForOneInstruction irfoi = param_depend_itr.next();
				List<Integer> indexs = wrap_node.ParameterIndexNodeDependsTo(irfoi);
				Iterator<Integer> iitr = indexs.iterator();
				while (iitr.hasNext()) {
					int index = iitr.next();
					IJavaElement param = params.get(index);
					if (param != null) {
						non_null_params.add(param);
						IRForOneInstruction irpara = irc.GetFirstIRTreeNode(param);
						HandleStaticConnectionForSource(ft_run, irfoi, irpara, new StaticConnectionInfo(EdgeBaseType.Sequential.Value()), env_idx);
					}
				}
			}
			Set<IJavaElement> all_eles = new HashSet<IJavaElement>(irfom.GetAllElements());
			all_eles.removeAll(non_null_params);
			Iterator<IJavaElement> aitr = all_eles.iterator();
			while (aitr.hasNext())
			{
				IJavaElement ije = aitr.next();
				IRForOneInstruction irpara = irc.GetFirstIRTreeNode(ije);
				ft_run.NodeCreated(ije, new DynamicNode(irpara, irc, env_idx));
			}
		} else {
			if (irc instanceof IRForOneMethod) {
				// root
				// IRForOneMethod irfom = (IRForOneMethod) irc;
				// do nothing.
			} else {
				// field code.
				// IRForOneField irfom = (IRForOneField) irc;
				// do nothing.
			}
		}
		
//		while (param_depend_itr.hasNext())
//		{
//			IRForOneInstruction irfoi = param_depend_itr.next();
//			Integer index = wrap_node.ParameterIndexNodeDependsTo(irfoi);
//			IJavaElement param = params.get(index);
//			List<IRForOneInstruction> depd = inverse_depend.get(param);
//			if (depd == null)
//			{
//				depd = new LinkedList<IRForOneInstruction>();
//				inverse_depend.put(param, depd);
//			}
//			depd.add(irfoi);
//		}
//		Set<IJavaElement> ikeys = inverse_depend.keySet();
//		Iterator<IJavaElement> iitr = ikeys.iterator();
//		while (iitr.hasNext())
//		{
//			IJavaElement ije = iitr.next();
//			IRForOneInstruction first_instr = irc.GetFirstIRTreeNode(ije);
//			List<IRForOneInstruction> depds = inverse_depend.get(ije);
//			Iterator<IRForOneInstruction> ditr = depds.iterator();
//			while (ditr.hasNext())
//			{
//				IRForOneInstruction irfoi = ditr.next();
//				HandleChildNodeExtendFromParentNode(irfoi, first_instr, IRGeneratorForOneProject.GetInstance().GetSpecifiedConnection(irfoi, wrap_node));
//			}
//			executed_instrs.add(first_instr);
//			instr_pc.remove(first_instr);
//			instr_pc.addAll(IRGeneratorForOneProject.GetInstance().GetOutINodes(first_instr));
//		}
	}
	
//	public void GoForwardOneMethod(IRForOneSourceMethodInvocation wrap_node, FullTrace ft)
//	{
//		Iterator<IMethod> witr = will_visit.iterator();
//		while (witr.hasNext())
//		{
//			IMethod im = witr.next();
//			if (!visited.contains(im))
//			{
//				visited.add(im);
//				IRForOneMethod irfom = IRGeneratorForOneProject.GetInstance().GetMethodIR(im);
//				if (irfom == null)
//				{
//					continue;
//				}
//				FullTrace ft_run = new FullTrace(ft);
//				if (irfom instanceof IRForOneConstructor)
//				{
//					IRForOneConstructor irfoc = (IRForOneConstructor)irfom;
//					IType it = irfoc.getWrap_class();
//					IRForOneClass irfot = IRGeneratorForOneProject.GetInstance().GetClassIR(it);
//					if (irfot != null)
//					{
//						IRForOneField field_level = irfot.GetFieldLevel();
//						if (field_level != null)
//						{
//							// execute field_level.
//							ft_run.ExecuteFieldCode(field_level, this);
//						}
//					}
//				}
//				if (irfom != null)
//				{
//					// execute irfom.
//					ft_run.ExecuteMethodCode(irfom, this);
//				}
//			}
//		}
//	}
	
}
