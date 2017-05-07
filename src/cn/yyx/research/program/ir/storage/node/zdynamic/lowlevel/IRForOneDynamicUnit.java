package cn.yyx.research.program.ir.storage.node.zdynamic.lowlevel;

import cn.yyx.research.program.ir.storage.node.zstatic.lowlevel.IRForOneInstruction;

public class IRForOneDynamicUnit {
	
	int id = 0;
	IRForOneInstruction instr = null;
	
	public IRForOneDynamicUnit(int id, IRForOneInstruction instr) {
		this.id = id;
		this.instr = instr;
	}
	
	
	
}
