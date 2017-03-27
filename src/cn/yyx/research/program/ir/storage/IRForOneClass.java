package cn.yyx.research.program.ir.storage;

import java.util.LinkedList;
import java.util.List;

public class IRForOneClass {
	
	private IRForOneCloseBlockUnit field_level = null;
	private List<IRForOneCloseBlockUnit> method_level = new LinkedList<IRForOneCloseBlockUnit>();
	
	public IRForOneClass() {
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

	public void AddOneMethod_level(IRForOneCloseBlockUnit one_method_level) {
		this.method_level.add(one_method_level);
	}
	
}
