package cn.yyx.research.program.ir.storage.highlevel;

import java.util.List;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;

import cn.yyx.research.program.ir.IRGeneratorForOneProject;

public class IRForOneClass {
	
	private IMember im = null;
	private IRForOneMethod field_level = null;
	private List<IRForOneMethod> method_level = null;
	
	public IRForOneClass(IMember im, IRForOneMethod field_level, List<IRForOneMethod> method_level) {
		this.setIm(im);
		this.setField_level(field_level);
		this.setMethod_level(method_level);
		if (im instanceof IType)
		{
			IRGeneratorForOneProject.AddITypeIR((IType)im, this);
		}
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

	public void setMethod_level(List<IRForOneMethod> method_level) {
		this.method_level = method_level;
	}

	public IMember getIm() {
		return im;
	}

	public void setIm(IMember im) {
		this.im = im;
	}
	
}
