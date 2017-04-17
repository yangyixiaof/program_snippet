package cn.yyx.research.program.ir.storage.node.highlevel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;

import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneJavaInstruction;

public class IRForOneField extends IRForOneJavaElement implements IRCode {
	
	private Map<IJavaElement, Set<IJavaElement>> deps = new HashMap<IJavaElement, Set<IJavaElement>>();
	private Map<IJavaElement, LinkedList<IRForOneJavaInstruction>> irs = new HashMap<IJavaElement, LinkedList<IRForOneJavaInstruction>>();
	
	public IRForOneField(IType it) {
		super(it);
	}
	
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
	
	public void AddParameter(IJavaElement im)
	{
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

	@Override
	public void AddAssignDependency(IJavaElement ije, Set<IJavaElement> assign_depend_set) {
		deps.put(ije, assign_depend_set);
	}

	@Override
	public Set<IJavaElement> GetAssignDependency(IJavaElement ije) {
		return deps.get(ije);
	}
	
	@Override
	public IJavaElement GetScopeIElement() {
		return getIm();
	}

	@Override
	public Map<IJavaElement, Integer> CopyEnvironment() {
		Map<IJavaElement, Integer> env = new HashMap<IJavaElement, Integer>();
		Set<IJavaElement> ikeys = irs.keySet();
		Iterator<IJavaElement> iitr = ikeys.iterator();
		while (iitr.hasNext())
		{
			IJavaElement ije = iitr.next();
			env.put(ije, irs.get(ije).size()-1);
		}
		return env;
	}
	
}
