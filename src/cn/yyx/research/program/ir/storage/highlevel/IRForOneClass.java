package cn.yyx.research.program.ir.storage.highlevel;

import java.util.List;

import org.eclipse.jdt.core.IType;

public class IRForOneClass extends IRForOneJavaElement {
	
	private IRForOneMethod field_level = null;
	private List<IRForOneMethod> method_level = null;
	
	public IRForOneClass(IType it) {
		super(it);
//		IRGeneratorForOneProject.FetchITypeIR(it, this);
	}

	public IRForOneMethod getField_level() {
		return field_level;
	}

	public void setField_level(IRForOneMethod field_level) {
		this.field_level = field_level;
	}

	public List<IRForOneMethod> getMethod_level() {
		return method_level;
	}

	public void AddMethod_level(List<IRForOneMethod> method_level) {
		this.method_level = method_level;
	}
	
}
