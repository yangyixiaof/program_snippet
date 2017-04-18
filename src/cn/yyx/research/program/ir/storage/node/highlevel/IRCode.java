package cn.yyx.research.program.ir.storage.node.highlevel;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;

import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public interface IRCode {
	
	public void AddOneIRUnit(IJavaElement ivb, IRForOneInstruction irfou);
	
	public void AddParameter(IJavaElement im);
	
	public List<IRForOneInstruction> GetOneAllIRUnits(IJavaElement ivb);
	
	public IRForOneInstruction GetLastIRUnit(IJavaElement ivb);
	
	public IRForOneInstruction GetIRUnitByIndex(IJavaElement ivb, int index);
	
	public void AddAssignDependency(IJavaElement ije, Set<IJavaElement> assign_depend_set);
	
	public Set<IJavaElement> GetAssignDependency(IJavaElement ije);
	
	public IMember GetScopeIElement();
	
	public Map<IJavaElement, Integer> CopyEnvironment();
	
}
