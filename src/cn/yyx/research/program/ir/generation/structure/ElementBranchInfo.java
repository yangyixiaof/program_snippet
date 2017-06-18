package cn.yyx.research.program.ir.generation.structure;

import java.util.Collection;
import java.util.HashMap;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class ElementBranchInfo {
	
	private HashMap<IJavaElement, Boolean> element_has_set_info = new HashMap<IJavaElement, Boolean>();
	private HashMap<IJavaElement, IRForOneInstruction> element_changed = new HashMap<IJavaElement, IRForOneInstruction>();
	private HashMap<IJavaElement, Boolean> element_change_applied = new HashMap<IJavaElement, Boolean>();
	
	public boolean ElementChanged() {
		return !element_changed.isEmpty();
	}
	
	public Collection<IRForOneInstruction> ChangedElements() {
		return element_changed.values();
	}
	
	public boolean ChangeIsApplied(IJavaElement ije) {
		return element_change_applied.containsKey(ije);
	}
	
	public Boolean ElementMainBranchHasSet(IJavaElement ije) {
		return element_has_set_info.get(ije);
	}
	
	public void SetElementMainBranch(IJavaElement ije) {
		element_has_set_info.put(ije, true);
	}
	
	public void PutElementChanged(IJavaElement ije, IRForOneInstruction instr) {
		element_changed.put(ije, instr);
	}
	
	public void ClearElementChanged() {
		element_changed.clear();
		element_change_applied.clear();
	}
	
	public void Clear() {
		element_has_set_info.clear();
		element_changed.clear();
		element_change_applied.clear();
	}

	public void SetChangeApplied(IJavaElement ije) {
		element_change_applied.put(ije, true);
	}
	
}
