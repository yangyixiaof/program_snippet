package cn.yyx.research.program.ir.storage.highlevel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.IBinding;

import cn.yyx.research.program.ir.IRGeneratorForOneProject;
import cn.yyx.research.program.ir.storage.lowlevel.IRForOneExtension;
import cn.yyx.research.program.ir.storage.lowlevel.IRForOneUnit;

public class IRForOneCloseBlockUnit {
	
	// The entrance must be MethodDeclaration.
	
	private IMember im = null;
	
	// this is set when exploring MethodDeclaration the first time.
	private Map<IBinding, Integer> parameters_order = new HashMap<IBinding, Integer>();
	
	// this is set when handling a MethodInvocation.
	private Map<IRForOneExtension, HashMap<IBinding, Integer>> variable_parameter_order = new HashMap<IRForOneExtension, HashMap<IBinding, Integer>>();
	
	private Map<IBinding, LinkedList<IRForOneUnit>> irs = new HashMap<IBinding, LinkedList<IRForOneUnit>>();
	
	// only three situations could lead to data_dependency key: first var_bind in method invocation(exclude cascade)/left value in assignment.
	private Map<IBinding, HashSet<IBinding>> data_dependency = new HashMap<IBinding, HashSet<IBinding>>();
	// private List<IRForOneUnit> units = new LinkedList<IRForOneUnit>();
	
	public IRForOneCloseBlockUnit(IMember im) {
		this.setIm(im);
		if (im instanceof IMethod)
		{
			IRGeneratorForOneProject.AddIMethodIR((IMethod)im, this);
		}
	}
	
	public IMember getIm() {
		return im;
	}
	
	private void setIm(IMember im) {
		this.im = im;
	}
	
	public void AddDataDependency(IBinding key, Set<IBinding> value)
	{
		data_dependency.put(key, new HashSet<IBinding>(value));
	}
	
	public void AddVariableParameterOrder(IRForOneExtension irfoe, HashMap<IBinding, Integer> order)
	{
		variable_parameter_order.put(irfoe, order);
	}
	
	public void AddOneIRUnit(IBinding ivb, IRForOneUnit irfou)
	{
		LinkedList<IRForOneUnit> list = irs.get(ivb);
		if (list == null)
		{
			list = new LinkedList<IRForOneUnit>();
			irs.put(ivb, list);
		}
		list.add(irfou);
	}
	
//	public void AddOneIRUnit(IRForOneUnit irfou)
//	{
//		units.add(irfou);
//	}
//	
//	public Iterator<IRForOneUnit> IterateAllUnits()
//	{
//		return units.iterator();
//	}
	
	public void PutParameterPrder(IBinding key, Integer value)
	{
		parameters_order.put(key, value);
	}
	
}
