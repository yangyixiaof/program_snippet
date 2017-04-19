package cn.yyx.research.program.ir.storage.node.highlevel;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;

public class IRForOneMethod extends IRCode {
	
	// The entrance must be MethodDeclaration.
	
	// this is set when exploring MethodDeclaration the first time.
	private List<IJavaElement> parameters = new LinkedList<IJavaElement>();
	
	// valid index is 0, -1 means no irs.
	
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
	
	public List<IJavaElement> GetParameters()
	{
		return parameters;
	}
	
}
