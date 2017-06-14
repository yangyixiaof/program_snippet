package cn.yyx.research.program.ir.storage.connection.detail;

import cn.yyx.research.program.ir.exception.ConflictConnectionDetailException;
import cn.yyx.research.program.ir.exception.NotCastConnectionDetailException;

public class DefaultConnection extends ConnectionDetail {
	
//	private static DefaultConnection default_connection = new DefaultConnection();
//	
//	public static DefaultConnection GetDefaultConnection() {
//		return default_connection;
//	}
	
	private DefaultConnection() {
	}
	
	@Override
	public void HorizontalMergeCheck(ConnectionDetail cd) throws NotCastConnectionDetailException {
	}

	@Override
	public ConnectionDetail VerticalMerge(ConnectionDetail cd) throws ConflictConnectionDetailException {
		ConnectionDetail result = null;
		try {
			result = (ConnectionDetail)cd.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DefaultConnection) {
			return true;
		}
		return false;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return this;
	}

	@Override
	public String toString() {
		return "DefaultConnection";
	}

	@Override
	public int hashCode() {
		return 0;
	}
	
}
