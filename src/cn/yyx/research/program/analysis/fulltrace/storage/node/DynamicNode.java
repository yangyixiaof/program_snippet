package cn.yyx.research.program.analysis.fulltrace.storage.node;

import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class DynamicNode {

	IRForOneInstruction instr = null;
	IRCode irc = null;
	int idx = -1;

	public DynamicNode(IRForOneInstruction instr, IRCode irc, int idx) {
		this.instr = instr;
		this.irc = irc;
		this.idx = idx;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DynamicNode) {
			DynamicNode dn = (DynamicNode) obj;
			if (instr == dn.instr && irc == dn.irc && idx == dn.idx) {
				return true;
			}
			return false;
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = idx;
		result = prime * result + instr.hashCode();
		result = prime * result + irc.hashCode();
		return result;
	}

}
