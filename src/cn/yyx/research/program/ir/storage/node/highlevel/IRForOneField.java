package cn.yyx.research.program.ir.storage.node.highlevel;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;

public class IRForOneField extends IRCode {
	
	public IRForOneField(IType it) {
		super(it);
	}
	
	public void AddParameter(IJavaElement im) {}
	public List<IJavaElement> GetParameters() {return null;}
	
}
