package cn.yyx.research.program.ir.storage.connection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import cn.yyx.research.program.ir.exception.ConflictConnectionDetailException;
import cn.yyx.research.program.ir.exception.NotCastConnectionDetailException;
import cn.yyx.research.program.ir.storage.connection.detail.ConnectionDetail;

public class ConnectionInfo {
	
	private int type = 0;
	private List<ConnectionDetail> details = new LinkedList<ConnectionDetail>();
	
	public ConnectionInfo(int type, ConnectionDetail... cds) {
		this.setType(type);
		details.addAll(Arrays.asList(cds));
	}
	
	public ConnectionInfo(int type, Collection<ConnectionDetail> cds) {
		this.setType(type);
		details.addAll(cds);
	}

	public int getType() {
		return type;
	}

	private void setType(int type) {
		this.type = type;
	}
	
	private void AddConnectionDetail(ConnectionDetail cd) {
		details.add(cd);
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
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
	
	private void HorizontalMergeCheck(ConnectionInfo ci) throws NotCastConnectionDetailException {
		Iterator<ConnectionDetail> cicditr = ci.details.iterator();
		while (cicditr.hasNext()) {
			ConnectionDetail cicd = cicditr.next();
			Iterator<ConnectionDetail> cditr = details.iterator();
			while (cditr.hasNext()) {
				ConnectionDetail cd = cditr.next();
				cicd.HorizontalMergeCheck(cd);
			}
		}
	}
	
	public ConnectionInfo HorizontalMerge(ConnectionInfo ci) throws NotCastConnectionDetailException
	{
		HorizontalMergeCheck(ci);
		List<ConnectionDetail> new_details = new LinkedList<ConnectionDetail>();
		new_details.addAll(details);
		new_details.addAll(ci.details);
		ConnectionInfo new_ci = new ConnectionInfo(type | ci.type, new_details);
		return new_ci;
	}
	
	public ConnectionInfo VerticalMerge(ConnectionInfo ci) throws ConflictConnectionDetailException
	{
		List<ConnectionDetail> new_details = new LinkedList<ConnectionDetail>();
		Iterator<ConnectionDetail> cicditr = ci.details.iterator();
		while (cicditr.hasNext()) {
			ConnectionDetail cicd = cicditr.next();
			Iterator<ConnectionDetail> cditr = details.iterator();
			while (cditr.hasNext()) {
				ConnectionDetail cd = cditr.next();
				ConnectionDetail merged = cicd.VerticalMerge(cd);
				new_details.add(merged);
			}
		}
		ConnectionInfo new_ci = new ConnectionInfo(type | ci.type, new_details);
		return new_ci;
	}
	
}
