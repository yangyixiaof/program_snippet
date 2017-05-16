package cn.yyx.research.program.analysis.fulltrace.generation;

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
import cn.yyx.research.program.ir.generation.IRGeneratorForOneProject;
import cn.yyx.research.program.ir.orgranization.IRTreeForOneControlElement;
import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnection;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneClass;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneConstructor;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneField;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneMethod;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneBranchControl;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneSourceMethodInvocation;

public class CodeOnOneTraceGenerator {
	
	private Set<IMethod> visited = new HashSet<IMethod>();
	private MethodSelection method_selection = null;
	
	private Stack<IRForOneBranchControl> branch_control_stack = new Stack<IRForOneBranchControl>();
	private Map<IRForOneMethod, Integer> same_method_max_id = new HashMap<IRForOneMethod, Integer>();
	private Map<IRForOneMethod, Stack<Integer>> same_method_id = new HashMap<IRForOneMethod, Stack<Integer>>();
	// private Map<IRForOneMethod, Stack<Set<IRForOneBranchControl>>> activaton_node = new HashMap<IRForOneMethod, Stack<Set<IRForOneBranchControl>>>();
	
	public CodeOnOneTraceGenerator(MethodSelection ms) {
		this.method_selection = ms;
	}
	
	private void GenerateFullTrace(IMethod now_method, IRForOneSourceMethodInvocation now_instruction)
	{
		IRForOneMethod irfom = IRGeneratorForOneProject.GetInstance().GetMethodIR(now_method);
		IRTreeForOneControlElement control_ir = irfom.GetControlLogicHolderElementIR();
		IRForOneBranchControl control_root = control_ir.GetRoot();
		
		int last_size = branch_control_stack.size();
		List<IRForOneBranchControl> new_list = branch_control_stack.subList(0, last_size);
		
		FullTrace ft = new FullTrace();
		ExecutionMemory memory = new ExecutionMemory();
		// TODO should handle the before connection to parameters.
		// TODO remember to add root dynamic node if now_instruction is null which means now_method is the root.
		DepthFirstToVisitControlLogic(ft, control_root, irfom, memory);
		
		branch_control_stack.clear();
		branch_control_stack.addAll(new_list);
	}
	
	// TODO remember to add virtual branch to every node in only one branch�� such as if(){} without else branch.
	private void DepthFirstToVisitControlLogic(FullTrace ft, IRForOneBranchControl now_control_root, IRForOneMethod irfom, ExecutionMemory execution_memory)
	{
		Set<StaticConnection> out_conns = IRGeneratorForOneProject.GetInstance().GetOutConnections(now_control_root);
		execution_memory.executed_conns.addAll(out_conns);
		
		BreadthFirstToVisitIR(ft, execution_memory);
		
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
			branch_control_stack.add(ir_control);
			DepthFirstToVisitControlLogic(ft, ir_control, irfom, execution_memory);
			branch_control_stack.pop();
		}
	}
	
	private void BreadthFirstToVisitIR(FullTrace ft, ExecutionMemory memory)
	{
		// do ...
		while (true)
		{
			boolean could_continue = false;
			Set<IJavaElement> exe_keys = memory.last_execution.keySet();
			Iterator<IJavaElement> exe_itr = exe_keys.iterator();
			while (exe_itr.hasNext())
			{
				IJavaElement ije = exe_itr.next();
				IRForOneInstruction inode = memory.last_execution.get(ije);
				
				// TODO handle operations first, remember to handle IRForMethodInvocation which is totally different.
				
				Set<StaticConnection> in_conns = ObtainExecutionPermission(inode, memory.executed_conns);
				could_continue = could_continue || ((in_conns != null) && (in_conns.size() > 0));
				if (in_conns != null && in_conns.size() > 0)
				{
					Iterator<StaticConnection> in_itr = in_conns.iterator();
					while (in_itr.hasNext())
					{
						StaticConnection sc = in_itr.next();
						IRForOneInstruction source = sc.getSource();
						IRForOneInstruction target = sc.getTarget();
						if (target instanceof IRForOneSourceMethodInvocation) {
							
						}
						IIRNodeTask out_task = source.GetOutConnectionMergeTask();
						if (source instanceof IRForOneSourceMethodInvocation) {
							IRForOneSourceMethodInvocation irmethod_source = (IRForOneSourceMethodInvocation) source;
							
						} else {
							out_task.HandleOutConnection(sc, ft);
						}
						
						Map<IIRNode, Set<StaticConnection>> outs = sc.PrepareOutNodes();
						Set<IIRNode> okeys = outs.keySet();
						// TODO
						
					}
				}
			}
			if (!could_continue)
			{
				break;
			}
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
	
	public void GoForwardOneMethod(IRForOneSourceMethodInvocation wrap_node, FullTrace ft)
	{
		Iterator<IMethod> witr = will_visit.iterator();
		while (witr.hasNext())
		{
			IMethod im = witr.next();
			if (!visited.contains(im))
			{
				visited.add(im);
				IRForOneMethod irfom = IRGeneratorForOneProject.GetInstance().GetMethodIR(im);
				if (irfom == null)
				{
					continue;
				}
				FullTrace ft_run = new FullTrace(ft);
				if (irfom instanceof IRForOneConstructor)
				{
					IRForOneConstructor irfoc = (IRForOneConstructor)irfom;
					IType it = irfoc.getWrap_class();
					IRForOneClass irfot = IRGeneratorForOneProject.GetInstance().GetClassIR(it);
					if (irfot != null)
					{
						IRForOneField field_level = irfot.GetFieldLevel();
						if (field_level != null)
						{
							// TODO execute field_level.
							ft_run.ExecuteFieldCode(field_level, this);
						}
					}
				}
				if (irfom != null)
				{
					// TODO execute irfom.
					ft_run.ExecuteMethodCode(irfom, this);
				}
			}
		}
	}
	
	private void HandleChildNodeExtendFromParentNode(IIRNode parent, IIRNode child, StaticConnection shared_conn)
	{
		// TODO
		
	}
	
	public void ExecuteMethodCode(IRForOneSourceMethodInvocation wrap_node, IRForOneMethod irc, FullTrace ft_run)
	{
		// wrap_node is used only for the first phase.
		// now handle wrap_node for depending on method_parameter_connection.
		Set<IIRNode> executed_instrs = new HashSet<IIRNode>();
		Set<IIRNode> instr_pc = new HashSet<IIRNode>();
		Set<IJavaElement> eles = irc.GetAllElements();
		Iterator<IJavaElement> eitr = eles.iterator();
		while (eitr.hasNext())
		{
			IJavaElement ije = eitr.next();
			instr_pc.add(irc.GetFirstIRTreeNode(ije));
		}
		
		List<IJavaElement> params = irc.GetParameters();
		Iterator<IRForOneInstruction> param_depend_itr = wrap_node.VariableParameterIterator();
		Map<IJavaElement, List<IRForOneInstruction>> inverse_depend = new HashMap<IJavaElement, List<IRForOneInstruction>>();
		while (param_depend_itr.hasNext())
		{
			IRForOneInstruction irfoi = param_depend_itr.next();
			Integer index = wrap_node.VariableParameterInstrIndex(irfoi);
			IJavaElement param = params.get(index);
			List<IRForOneInstruction> depd = inverse_depend.get(param);
			if (depd == null)
			{
				depd = new LinkedList<IRForOneInstruction>();
				inverse_depend.put(param, depd);
			}
			depd.add(irfoi);
		}
		Set<IJavaElement> ikeys = inverse_depend.keySet();
		Iterator<IJavaElement> iitr = ikeys.iterator();
		while (iitr.hasNext())
		{
			IJavaElement ije = iitr.next();
			IRForOneInstruction first_instr = irc.GetFirstIRTreeNode(ije);
			List<IRForOneInstruction> depds = inverse_depend.get(ije);
			Iterator<IRForOneInstruction> ditr = depds.iterator();
			while (ditr.hasNext())
			{
				IRForOneInstruction irfoi = ditr.next();
				HandleChildNodeExtendFromParentNode(irfoi, first_instr, IRGeneratorForOneProject.GetInstance().GetSpecifiedConnection(irfoi, wrap_node));
			}
			executed_instrs.add(first_instr);
			instr_pc.remove(first_instr);
			instr_pc.addAll(IRGeneratorForOneProject.GetInstance().GetOutINodes(first_instr));
		}
		
	}
	
	public void ExecuteFieldCode(IRForOneField irc, FullTrace ft_run)
	{
		
	}

	public FullTrace Execute() {
		FullTrace ft_run = new FullTrace();
		
		
		
		return ft_run;
	}
	
}
