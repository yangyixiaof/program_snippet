package cn.yyx.research.program.analysis.fulltrace.storage.node;

import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class DynamicNode {

	private IRForOneInstruction instr = null;
	private IRCode irc = null;
	private int idx = -1;

	public DynamicNode(IRForOneInstruction instr, IRCode irc, int idx) {
		this.setInstr(instr);
		this.setIrc(irc);
		this.setIdx(idx);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DynamicNode) {
			DynamicNode dn = (DynamicNode) obj;
			if (getInstr() == dn.getInstr() && getIrc() == dn.getIrc() && getIdx() == dn.getIdx()) {
				return true;
			}
			return false;
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = getIdx();
		result = prime * result + getInstr().hashCode();
		result = prime * result + getIrc().hashCode();
		return result;
	}

	public IRForOneInstruction getInstr() {
		return instr;
	}

	private void setInstr(IRForOneInstruction instr) {
		this.instr = instr;
	}

	public IRCode getIrc() {
		return irc;
	}

	private void setIrc(IRCode irc) {
		this.irc = irc;
	}

	public int getIdx() {
		return idx;
	}

	private void setIdx(int idx) {
		this.idx = idx;
	}

}
