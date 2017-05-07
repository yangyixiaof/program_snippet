package cn.yyx.research.program.ir.storage.node.zdynamic.lowlevel;

import cn.yyx.research.program.ir.storage.node.zstatic.lowlevel.IRForOneInstruction;

public class IRForOneDynamicUnit {
	
	int id = 0;
	IRForOneInstruction instr = null;
	
	public IRForOneDynamicUnit(int id, IRForOneInstruction instr) {
		this.id = id;
		this.instr = instr;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
        int result = id;
        result = prime * result + ((instr == null) ? 0 : instr.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IRForOneDynamicUnit) {
			IRForOneDynamicUnit irfod = (IRForOneDynamicUnit)obj;
			if (id == irfod.id && instr == irfod.instr) {
				return true;
			}
			return false;
		}
		return super.equals(obj);
	}
	
}
