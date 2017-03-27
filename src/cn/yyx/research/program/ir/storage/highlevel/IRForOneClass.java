package cn.yyx.research.program.ir.storage.highlevel;

import java.util.List;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;

import cn.yyx.research.program.ir.IRGeneratorForOneProject;

public class IRForOneClass {
	
	private IMember im = null;
	private IRForOneCloseBlockUnit field_level = null;
	private List<IRForOneCloseBlockUnit> method_level = null;
	
	public IRForOneClass(IMember im, IRForOneCloseBlockUnit field_level, List<IRForOneCloseBlockUnit> method_level) {
		this.setIm(im);
		this.setField_level(field_level);
		this.setMethod_level(method_level);
		if (im instanceof IType)
		{
			IRGeneratorForOneProject.AddITypeIR((IType)im, this);
		}
	}

	public IRForOneCloseBlockUnit getField_level() {
		return field_level;
	}

	public void setField_level(IRForOneCloseBlockUnit field_level) {
		this.field_level = field_level;
	}

	public List<IRForOneCloseBlockUnit> getMethod_level() {
		return method_level;
	}

	public void setMethod_level(List<IRForOneCloseBlockUnit> method_level) {
		this.method_level = method_level;
	}

	public IMember getIm() {
		return im;
	}

	public void setIm(IMember im) {
		this.im = im;
	}
	
}
