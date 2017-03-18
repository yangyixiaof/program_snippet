package cn.yyx.research.program.ir.method;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.IVariableBinding;

import cn.yyx.research.program.ir.IRTask;

public class IRForOneMethod {
	
	// The entrance must be MethodDeclaration.
	
	private IMethod im = null;
	
	// this is set when exploring MethodDeclaration the first time.
	private Map<IVariableBinding, Integer> parameters_order = new HashMap<IVariableBinding, Integer>();
	
	// this is set when handling a MethodInvocation.
	private Map<IRForOneExtension, HashMap<IVariableBinding, Integer>> variable_parameter_map = new HashMap<IRForOneExtension, HashMap<IVariableBinding, Integer>>();
	
	private Map<IVariableBinding, IRForOneUnit> irs = new HashMap<IVariableBinding, IRForOneUnit>();
	private Map<IVariableBinding, HashSet<IVariableBinding>> data_dependency = new HashMap<IVariableBinding, HashSet<IVariableBinding>>();
	private List<IRForOneUnit> units = new LinkedList<IRForOneUnit>();
	
	public IRForOneMethod(IMethod im) {
		this.setIm(im);
	}

	public IMethod getIm() {
		return im;
	}

	private void setIm(IMethod im) {
		this.im = im;
	}
	
	public void AddOneIRUnit(IRForOneUnit irfou)
	{
		units.add(irfou);
	}
	
	public Iterator<IRForOneUnit> IterateAllUnits()
	{
		return units.iterator();
	}
	
}
