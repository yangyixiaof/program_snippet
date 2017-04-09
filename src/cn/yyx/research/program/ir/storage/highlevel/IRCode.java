package cn.yyx.research.program.ir.storage.highlevel;

import java.util.List;

import org.eclipse.jdt.core.IMember;

import cn.yyx.research.program.ir.storage.lowlevel.IRForOneJavaInstruction;

public interface IRCode {
	
	public void AddOneIRUnit(IMember ivb, IRForOneJavaInstruction irfou);
	
	public void AddParameter(IMember im);
	
	public List<IRForOneJavaInstruction> GetOneAllIRUnits(IMember ivb);
	
}
