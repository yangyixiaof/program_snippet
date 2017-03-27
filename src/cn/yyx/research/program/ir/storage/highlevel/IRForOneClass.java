package cn.yyx.research.program.ir.storage.highlevel;

import java.util.List;

public class IRForOneClass {
	
	private IRForOneCloseBlockUnit field_level = null;
	private List<IRForOneCloseBlockUnit> method_level = null;
	
	public IRForOneClass(IRForOneCloseBlockUnit field_level, List<IRForOneCloseBlockUnit> method_level) {
		this.setField_level(field_level);
		this.setMethod_level(method_level);
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
	
}
