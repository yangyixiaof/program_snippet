package cn.yyx.research.program.ir.storage.highlevel;

import java.util.List;

import org.eclipse.jdt.core.IType;

public class IRForOneClass extends IRForOneJavaElement {
	
	private IRForOneField field_level = null;
	private List<IRForOneMethod> method_level = null;
	
	public IRForOneClass(IType it) {
		super(it);
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
