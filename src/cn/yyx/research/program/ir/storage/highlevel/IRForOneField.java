package cn.yyx.research.program.ir.storage.highlevel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;

import cn.yyx.research.program.ir.storage.lowlevel.IRForOneJavaInstruction;

public class IRForOneField extends IRForOneJavaElement implements IRCode {
	
	private Map<IMember, LinkedList<IRForOneJavaInstruction>> irs = new HashMap<IMember, LinkedList<IRForOneJavaInstruction>>();
	
	public IRForOneField(IType it) {
		super(it);
	}
	
	public void AddOneIRUnit(IMember ivb, IRForOneJavaInstruction irfou)
	{
		LinkedList<IRForOneJavaInstruction> list = irs.get(ivb);
		if (list == null)
		{
			list = new LinkedList<IRForOneJavaInstruction>();
			irs.put(ivb, list);
		}
		list.add(irfou);
	}
	
	public void AddParameter(IMember im)
	{
	}

	@Override
	public List<IRForOneJavaInstruction> GetOneAllIRUnits(IMember ivb) {
		return irs.get(ivb);
	}
	
}
