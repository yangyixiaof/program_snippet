package cn.yyx.research.program.ir.storage.node.lowlevel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;

import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;

public class IRForOneSourceMethodInvocation extends IRForOneInstruction {
	
	// private IJavaElement parent_im = null;
	private List<IMethod> methods = new LinkedList<IMethod>();
	// this is set when handling a MethodInvocation.
	// private HashMap<IJavaElement, Integer> variable_parameter_order = new HashMap<IJavaElement, Integer>();
	// private List<HashMap<IJavaElement, Integer>> variable_parameter_orders = new LinkedList<HashMap<IJavaElement, Integer>>();
	// parameter order starts from 1, 0 refers to the invoking object.
	// this denotes the IJavaElement which im represents, it appers in which method parameter and its max instruction index.
	
	private HashMap<IRForOneInstruction, Integer> para_order_instr_index_map = new HashMap<IRForOneInstruction, Integer>();
	// this im has already contained the information about which IJavaElement this all about.
	// TODO HashMap<IRForOneInstruction, Integer> should be reversed.
	public IRForOneSourceMethodInvocation(IRCode parent_env, IJavaElement im, Collection<IMethod> methods, Class<? extends IIRNodeTask> task_class) {
		super(im, parent_env, task_class);
		this.AddMethods(methods);
		// this.AddVariableParameterOrderInstructionIndexs(para_order_instr_index_map);
	}
	
	public List<IMethod> GetAllMethods() {
		return methods;
	}

	public Iterator<IMethod> MethodIterator() {
		return methods.iterator();
	}

	private void AddMethods(Collection<IMethod> methods) {
		this.methods.addAll(methods);
	}
	
	public Iterator<IRForOneInstruction> ParameterDependentNodeIterator() {
		return para_order_instr_index_map.keySet().iterator();
	}
	
	public Integer ParameterIndexNodeDependsTo(IRForOneInstruction param) {
		return para_order_instr_index_map.get(param);
	}

	public void AddVariableParameterOrderInstructionIndexs(Map<IRForOneInstruction, Integer> para_order_instr_index_map) {
		this.para_order_instr_index_map.putAll(para_order_instr_index_map);
	}

//	@Override
//	public Map<IRForOneInstruction, Set<StaticConnection>> PrepareOutNodes() {
//		Map<IRForOneInstruction, Set<StaticConnection>> result = new HashMap<IRForOneInstruction, Set<StaticConnection>>();
//		// Set<IIRNode> imset = new HashSet<IIRNode>();
//		Iterator<IMethod> mitr = methods.iterator();
//		while (mitr.hasNext())
//		{
//			IMethod tim = mitr.next();
//			IRForOneMethod irfom = IRGeneratorForOneProject.GetInstance().FetchIMethodIR(tim);
//			Map<IJavaElement, IRForOneInstruction> ions = irfom.GetOutNodes();
//			Set<IJavaElement> ikeys = ions.keySet();
//			Iterator<IJavaElement> iitr = ikeys.iterator();
//			while (iitr.hasNext())
//			{
//				IJavaElement ije = iitr.next();
//				IRForOneInstruction irtn = ions.get(ije);
//				IRForOneInstruction irfoi = irtn;
//				Set<StaticConnection> out_connects = IRGeneratorForOneProject.GetInstance().GetOutConnection(this);
//				if (out_connects != null) {
//					HashSet<StaticConnection> temp_result = new HashSet<StaticConnection>();
//					Iterator<StaticConnection> oitr = out_connects.iterator();
//					while (oitr.hasNext())
//					{
//						StaticConnection sc = oitr.next();
//						temp_result.add(new StaticConnection(irfoi, sc.getTarget(), sc.getType()));
//					}
//					result.put(irfoi, temp_result);
//				}
//			}
//		}
//		return result;
//	}
//
//	@Override
//	public Map<IIRNode, Set<StaticConnection>> PrepareInNodes() {
//		Map<IIRNode, Set<StaticConnection>> result = new HashMap<IIRNode, Set<StaticConnection>>();
//		Set<IRForOneInstruction> keys = para_order_instr_index_map.keySet();
//		Iterator<IRForOneInstruction> kitr = keys.iterator();
//		while (kitr.hasNext())
//		{
//			IRForOneInstruction source = kitr.next();
//			Integer para_index_in_invoked_method = para_order_instr_index_map.get(source);
//			if (para_index_in_invoked_method != null)
//			{
//				Iterator<IMethod> mitr = methods.iterator();
//				while (mitr.hasNext())
//				{
//					IMethod tim = mitr.next();
//					IRForOneMethod irfom = IRGeneratorForOneProject.GetInstance().FetchIMethodIR(tim);
//					List<IJavaElement> params = irfom.GetParameters();
//					if (params.size() > para_index_in_invoked_method)
//					{
//						IJavaElement ije = params.get(para_index_in_invoked_method);
//						IRForOneInstruction para_element = irfom.GetFirstIRTreeNode(ije);
//						if (para_element != null)
//						{
//							StaticConnection cnn = IRGeneratorForOneProject.GetInstance().GetSpecifiedConnection(source, this);
//							Set<StaticConnection> para_cnns = result.get(para_element);
//							if (para_cnns == null)
//							{
//								para_cnns = new HashSet<StaticConnection>();
//								result.put(para_element, para_cnns);
//							}
//							para_cnns.add(new StaticConnection(source, para_element, cnn.getType()));
//						}
//					}
//				}
//			}
//		}
//		return result;
//	}
	
}
