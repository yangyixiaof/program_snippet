package cn.yyx.research.program.ir.generation.structure;

import cn.yyx.research.program.ir.storage.connection.detail.ConnectionDetail;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class NodeConnectionDetailPair {
	
	private IRForOneInstruction irfoi = null;
	private ConnectionDetail detail = null;
	
	public NodeConnectionDetailPair(IRForOneInstruction irfoi, ConnectionDetail detail) {
		this.setInstruction(irfoi);
		this.setDetail(detail);
	}

	public IRForOneInstruction getInstruction() {
		return irfoi;
	}

	private void setInstruction(IRForOneInstruction irfoi) {
		this.irfoi = irfoi;
	}

	public ConnectionDetail getDetail() {
		return detail;
	}

	public void setDetail(ConnectionDetail detail) {
		this.detail = detail;
	}
	
	@Override
	public String toString() {
		return irfoi + "&" + detail;
	}
	
}
