package cn.yyx.research.program.ir.storage.node.lowlevel;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;

import cn.yyx.research.program.ir.generation.IRGeneratorForOneProject;
import cn.yyx.research.program.ir.storage.node.IIRNode;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnection;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneMethod;

public class IRForOneMethodInvocation extends IRForOneInstruction {
	
	// private IJavaElement parent_im = null;
	private List<IMethod> methods = new LinkedList<IMethod>();
	// this is set when handling a MethodInvocation.
	// private HashMap<IJavaElement, Integer> variable_parameter_order = new HashMap<IJavaElement, Integer>();
	// private List<HashMap<IJavaElement, Integer>> variable_parameter_orders = new LinkedList<HashMap<IJavaElement, Integer>>();
	// parameter order starts from 1, 0 refers to the invoking object.
	// this denotes the IJavaElement which im represents, it appers in which method parameter and its max instruction index.
	
	private HashMap<IRForOneInstruction, Integer> para_order_instr_index_map = new HashMap<IRForOneInstruction, Integer>();
	// this im has already contained the information about which IJavaElement this all about.
	// TODO remember to check the im is the source_method_element etc.
	public IRForOneMethodInvocation(IRCode parent_env, IJavaElement im, Collection<IMethod> methods) {
		super(im, parent_env);
		this.AddMethods(methods);
		// this.AddVariableParameterOrderInstructionIndexs(para_order_instr_index_map);
	}

	public Iterator<IMethod> MethodIterator() {
		return methods.iterator();
	}

	private void AddMethods(Collection<IMethod> methods) {
		this.methods.addAll(methods);
	}
	
	public Iterator<IRForOneInstruction> VariableParameterIterator() {
		return para_order_instr_index_map.keySet().iterator();
	}
	
	public Integer VariableParameterInstrIndex(Integer param) {
		return para_order_instr_index_map.get(param);
	}

	public void AddVariableParameterOrderInstructionIndexs(Map<IRForOneInstruction, Integer> para_order_instr_index_map) {
		this.para_order_instr_index_map.putAll(para_order_instr_index_map);
	}

	@Override
	public Map<IIRNode, Set<StaticConnection>> PrepareOutNodes() {
		Map<IIRNode, Set<StaticConnection>> result = new HashMap<IIRNode, Set<StaticConnection>>();
		// Set<IIRNode> imset = new HashSet<IIRNode>();
		Iterator<IMethod> mitr = methods.iterator();
		while (mitr.hasNext())
		{
			IMethod tim = mitr.next();
			IRForOneMethod irfom = IRGeneratorForOneProject.FetchIMethodIR(tim);
			Map<IJavaElement, IRForOneInstruction> ions = irfom.GetOutNodes();
			Set<IJavaElement> ikeys = ions.keySet();
			Iterator<IJavaElement> iitr = ikeys.iterator();
			while (iitr.hasNext())
			{
				IJavaElement ije = iitr.next();
				IRForOneInstruction irfoi = ions.get(ije);
				Set<StaticConnection> out_connects = parent_env.GetOutConnects(this);
				if (out_connects != null) {
					result.put(irfoi, new HashSet<StaticConnection>(out_connects));
				}
			}
		}
		return result;
	}

	@Override
	public Map<IIRNode, Set<StaticConnection>> PrepareInNodes() {
		Map<IIRNode, Set<StaticConnection>> result = new HashMap<IIRNode, Set<StaticConnection>>();
		Set<IRForOneInstruction> keys = para_order_instr_index_map.keySet();
		Iterator<IRForOneInstruction> kitr = keys.iterator();
		while (kitr.hasNext())
		{
			IRForOneInstruction source = kitr.next();
			Integer para_index_in_invoked_method = para_order_instr_index_map.get(source);
			if (para_index_in_invoked_method != null)
			{
				Iterator<IMethod> mitr = methods.iterator();
				while (mitr.hasNext())
				{
					IMethod tim = mitr.next();
					IRForOneMethod irfom = IRGeneratorForOneProject.FetchIMethodIR(tim);
					List<IJavaElement> params = irfom.GetParameters();
					if (params.size() > para_index_in_invoked_method)
					{
						IJavaElement ije = params.get(para_index_in_invoked_method);
						List<IRForOneInstruction> list = irfom.GetOneAllIRUnits(ije);
						if (list != null && list.size() > 0)
						{
							IRForOneInstruction para_element = list.get(0);
							StaticConnection cnn = irfom.GetSpecifiedConnection(source, para_element);
							Set<StaticConnection> para_cnns = result.get(para_element);
							if (para_cnns == null)
							{
								para_cnns = new HashSet<StaticConnection>();
								result.put(para_element, para_cnns);
							}
							para_cnns.add(cnn);
						}
					}
				}
			}
		}
		return result;
	}
	
}
