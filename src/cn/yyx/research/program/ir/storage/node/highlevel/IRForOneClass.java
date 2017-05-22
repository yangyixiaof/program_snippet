package cn.yyx.research.program.ir.storage.node.highlevel;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.IType;

public class IRForOneClass {
	
	private IRForOneField field_level = null;
	private List<IRForOneMethod> method_level = new LinkedList<IRForOneMethod>();
	
	private IType it = null;
	
	public IType getIt() {
		return it;
	}

	public void setIt(IType it) {
		this.it = it;
	}
	
	public IRForOneClass(IType it) {
		this.it = it;
//		IRGeneratorForOneProject.FetchITypeIR(it, this);
	}

	public IRForOneField GetFieldLevel() {
		return field_level;
	}

	public void SetFieldLevel(IRForOneField field_level) {
		this.field_level = field_level;
	}

	public List<IRForOneMethod> GetMethodLevel() {
		return method_level;
	}

	public void AddMethodLevel(IRForOneMethod method_level) {
		this.method_level.add(method_level);
	}
	
}
