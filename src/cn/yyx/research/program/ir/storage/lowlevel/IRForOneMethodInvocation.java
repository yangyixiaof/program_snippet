package cn.yyx.research.program.ir.storage.lowlevel;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;

public class IRForOneMethodInvocation extends IRForOneJavaInstruction {
	
	// private IJavaElement parent_im = null;
	private List<IMethod> methods = new LinkedList<IMethod>();
	// this is set when handling a MethodInvocation.
	// private HashMap<IJavaElement, Integer> variable_parameter_order = new HashMap<IJavaElement, Integer>();
	// private List<HashMap<IJavaElement, Integer>> variable_parameter_orders = new LinkedList<HashMap<IJavaElement, Integer>>();
	// parameter order starts from 1, 0 refers to the invoking object.
	private Map<Integer, Integer> para_order_instr_index_map = new TreeMap<Integer, Integer>();
	
	public IRForOneMethodInvocation(IJavaElement im, Collection<IMethod> methods, Map<Integer, Integer> para_order_instr_index_map) {
		super(im);
		this.AddMethods(methods);
		this.AddVariableParameterOrderInstructionIndexs(para_order_instr_index_map);
	}

	public Iterator<IMethod> MethodIterator() {
		return methods.iterator();
	}

	private void AddMethods(Collection<IMethod> methods) {
		this.methods.addAll(methods);
	}
	
	public Iterator<Integer> VariableParameterIterator() {
		return para_order_instr_index_map.keySet().iterator();
	}
	
	public Integer VariableParameterInstrIndex(Integer param) {
		return para_order_instr_index_map.get(param);
	}

	private void AddVariableParameterOrderInstructionIndexs(Map<Integer, Integer> para_order_instr_index_map) {
		this.para_order_instr_index_map.putAll(para_order_instr_index_map);
	}

//	public IJavaElement getParent_im() {
//		return parent_im;
//	}
//
//	public void setParent_im(IJavaElement parent_im) {
//		this.parent_im = parent_im;
//	}
	
}
