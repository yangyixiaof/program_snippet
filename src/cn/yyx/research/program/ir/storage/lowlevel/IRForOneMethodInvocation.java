package cn.yyx.research.program.ir.storage.lowlevel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;

public class IRForOneMethodInvocation extends IRForOneUnit {
	
	private List<IMethod> methods = new LinkedList<IMethod>();
	// this is set when handling a MethodInvocation.
	// private HashMap<IMember, Integer> variable_parameter_order = new HashMap<IMember, Integer>();
	private List<HashMap<IMember, Integer>> variable_parameter_orders = new LinkedList<HashMap<IMember, Integer>>();
	
	public IRForOneMethodInvocation(IMember im, Collection<IMethod> methods, List<HashMap<IMember, Integer>> orders) {
		super(im);
		this.AddMethods(methods);
		this.AddVariableParameterOrders(orders);
	}

	public Iterator<IMethod> MethodIterator() {
		return methods.iterator();
	}

	private void AddMethods(Collection<IMethod> methods) {
		this.methods.addAll(methods);
	}
	
	public Iterator<HashMap<IMember, Integer>> VariableParameterIterator() {
		return variable_parameter_orders.iterator();
	}

	private void AddVariableParameterOrders(List<HashMap<IMember, Integer>> orders) {
		this.variable_parameter_orders.addAll(orders);
	}
	
}
