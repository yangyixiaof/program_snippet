package cn.yyx.research.program.ir.storage.highlevel;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.ir.storage.lowlevel.IRForOneJavaInstruction;

public interface IRCode {
	
	public void AddOneIRUnit(IJavaElement ivb, IRForOneJavaInstruction irfou);
	
	public void AddParameter(IJavaElement im);
	
	public List<IRForOneJavaInstruction> GetOneAllIRUnits(IJavaElement ivb);
	
	public IRForOneJavaInstruction GetLastIRUnit(IJavaElement ivb);
	
	public IRForOneJavaInstruction GetIRUnitByIndex(IJavaElement ivb, int index);
}
