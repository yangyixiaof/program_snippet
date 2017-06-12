package cn.yyx.research.program.ir.generation.structure;

import cn.yyx.research.program.ir.storage.connection.detail.ConnectionDetail;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class NodeConnectionDetailPair {
	
	private IRForOneInstruction irfoi = null;
	private ConnectionDetail detail = null;
	
	public NodeConnectionDetailPair(IRForOneInstruction irfoi, ConnectionDetail detail) {
		this.setIrfoi(irfoi);
		this.setDetail(detail);
	}

	public IRForOneInstruction getIrfoi() {
		return irfoi;
	}

	public void setIrfoi(IRForOneInstruction irfoi) {
		this.irfoi = irfoi;
	}

	public ConnectionDetail getDetail() {
		return detail;
	}

	public void setDetail(ConnectionDetail detail) {
		this.detail = detail;
	}
	
}
