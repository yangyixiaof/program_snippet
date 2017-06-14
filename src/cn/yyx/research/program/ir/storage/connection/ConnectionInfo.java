package cn.yyx.research.program.ir.storage.connection;

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
		if (cds != null) {
			for (ConnectionDetail cd : cds) {
				if (cd != null) {
					GetDetails().add(cd);
				}
			}
		}
	}
	
	public ConnectionInfo(int type, Collection<ConnectionDetail> cds) {
		this.setType(type);
		GetDetails().addAll(cds);
	}

	public int getType() {
		return type;
	}

	private void setType(int type) {
		this.type = type;
	}
	
	private void AddConnectionDetail(ConnectionDetail cd) {
		GetDetails().add(cd);
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		ConnectionInfo ci = new ConnectionInfo(type);
		for (ConnectionDetail cd : GetDetails()) {
			ci.AddConnectionDetail((ConnectionDetail)cd.clone());
		}
		return ci;
	}
	
	@Override
	public int hashCode() {
		int result = type;
		final int prime = 31;
		for (ConnectionDetail detail : details) {
			result =result * prime + detail.hashCode();
		}
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ConnectionInfo) {
			ConnectionInfo sci = (ConnectionInfo)obj;
			if (type == sci.type) {
				if (GetDetails().size() != sci.GetDetails().size()) {
					return false;
				}
				Iterator<ConnectionDetail> ditr = GetDetails().iterator();
				Iterator<ConnectionDetail> sditr = sci.GetDetails().iterator();
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
		Iterator<ConnectionDetail> cicditr = ci.GetDetails().iterator();
		while (cicditr.hasNext()) {
			ConnectionDetail cicd = cicditr.next();
			Iterator<ConnectionDetail> cditr = GetDetails().iterator();
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
		new_details.addAll(GetDetails());
		new_details.addAll(ci.GetDetails());
		ConnectionInfo new_ci = new ConnectionInfo(type | ci.type, new_details);
		return new_ci;
	}
	
	public ConnectionInfo VerticalMerge(ConnectionInfo ci) throws ConflictConnectionDetailException
	{
		List<ConnectionDetail> new_details = new LinkedList<ConnectionDetail>();
		Iterator<ConnectionDetail> cicditr = ci.GetDetails().iterator();
		while (cicditr.hasNext()) {
			ConnectionDetail cicd = cicditr.next();
			Iterator<ConnectionDetail> cditr = GetDetails().iterator();
			while (cditr.hasNext()) {
				ConnectionDetail cd = cditr.next();
				ConnectionDetail merged = cicd.VerticalMerge(cd);
				new_details.add(merged);
			}
		}
		ConnectionInfo new_ci = new ConnectionInfo(type | ci.type, new_details);
		return new_ci;
	}

	public List<ConnectionDetail> GetDetails() {
		return details;
	}
	
	@Override
	public String toString() {
		return "type:" + type + ";" + details.toString();
	}
	
}
