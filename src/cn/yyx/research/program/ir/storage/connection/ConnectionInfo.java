package cn.yyx.research.program.ir.storage.connection;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import cn.yyx.research.program.ir.storage.connection.detail.ConnectionDetail;

public class ConnectionInfo {
	
	private int type = 0;
	private List<ConnectionDetail> details = new LinkedList<ConnectionDetail>();
	
	public ConnectionInfo(int type) {
		this.setType(type);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public void AddConnectionDetail(ConnectionDetail cd) {
		details.add(cd);
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		ConnectionInfo ci = new ConnectionInfo(type);
		for (ConnectionDetail cd : details) {
			ci.AddConnectionDetail((ConnectionDetail)cd.clone());
		}
		return ci;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ConnectionInfo) {
			ConnectionInfo sci = (ConnectionInfo)obj;
			if (type == sci.type) {
				if (details.size() != sci.details.size()) {
					return false;
				}
				Iterator<ConnectionDetail> ditr = details.iterator();
				Iterator<ConnectionDetail> sditr = sci.details.iterator();
				while (ditr.hasNext()) {
					ConnectionDetail cd = ditr.next();
					ConnectionDetail scd = sditr.next();
					if (!cd.equals(scd)) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
		return super.equals(obj);
	}
	
}
