package cn.yyx.research.program.ir.storage.connection.detail;

import cn.yyx.research.program.ir.exception.ConflictConnectionDetailException;
import cn.yyx.research.program.ir.exception.NotCastConnectionDetailException;

public abstract class ConnectionDetail {
	
	public abstract void HorizontalMergeCheck(ConnectionDetail cd) throws NotCastConnectionDetailException;
	public abstract ConnectionDetail VerticalMerge(ConnectionDetail cd) throws ConflictConnectionDetailException;
	
	@Override
	public abstract int hashCode();
	
	@Override
	public abstract boolean equals(Object obj);
	
	@Override
	public abstract Object clone() throws CloneNotSupportedException;
	
	@Override
	public abstract String toString();
	
}
