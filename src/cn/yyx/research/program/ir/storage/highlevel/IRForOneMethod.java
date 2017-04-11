package cn.yyx.research.program.ir.storage.highlevel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;

import cn.yyx.research.program.ir.storage.lowlevel.IRForOneJavaInstruction;

public class IRForOneMethod extends IRForOneJavaElement implements IRCode {
	
	// The entrance must be MethodDeclaration.
	
	// this is set when exploring MethodDeclaration the first time.
	private List<IJavaElement> parameters = new LinkedList<IJavaElement>();
	
	// valid index is 0, -1 means no irs.
	private Map<IJavaElement, LinkedList<IRForOneJavaInstruction>> irs = new HashMap<IJavaElement, LinkedList<IRForOneJavaInstruction>>();
	
	// only three situations could lead to data_dependency key: first var_bind in method invocation(exclude cascade)/left value in assignment.
	// private Map<IBinding, HashSet<IBinding>> data_dependency = new HashMap<IBinding, HashSet<IBinding>>();
	// private List<IRForOneUnit> units = new LinkedList<IRForOneUnit>();
	
	public IRForOneMethod(IMethod im) {
		super(im);
		// this statement will be moved to the places where the method is first be visited in AST.
		// IRGeneratorForOneProject.FetchIMethodIR(im);
	}
	
//	public void AddDataDependency(IBinding key, Set<IBinding> value)
//	{
//		data_dependency.put(key, new HashSet<IBinding>(value));
//	}
//	
//	public void AddVariableParameterOrder(IRForOneMethodInvocation irfoe, HashMap<IBinding, Integer> order)
//	{
//		variable_parameter_order.put(irfoe, order);
//	}
	
	public void AddOneIRUnit(IJavaElement ivb, IRForOneJavaInstruction irfou)
	{
		LinkedList<IRForOneJavaInstruction> list = irs.get(ivb);
		if (list == null)
		{
			list = new LinkedList<IRForOneJavaInstruction>();
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
	
	public void AddParameter(IJavaElement im)
	{
		parameters.add(im);
	}
	
	@Override
	public List<IRForOneJavaInstruction> GetOneAllIRUnits(IJavaElement ivb) {
		return irs.get(ivb);
	}
	
	@Override
	public IRForOneJavaInstruction GetLastIRUnit(IJavaElement ivb) {
		LinkedList<IRForOneJavaInstruction> ii = irs.get(ivb);
		if (ii == null)
		{
			return null;
		}
		return ii.getLast();
	}

	@Override
	public IRForOneJavaInstruction GetIRUnitByIndex(IJavaElement ivb, int index) {
		LinkedList<IRForOneJavaInstruction> ii = irs.get(ivb);
		if (ii != null && ii.size() > index)
		{
			return ii.get(index);
		}
		return null;
	}
	
}
