package cn.yyx.research.program.ir.storage.highlevel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;

import cn.yyx.research.program.ir.storage.lowlevel.IRForOneJavaInstruction;

public class IRForOneField extends IRForOneJavaElement implements IRCode {
	
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
	
}
