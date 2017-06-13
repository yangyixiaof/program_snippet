package cn.yyx.research.program.ir.storage.connection;

import cn.yyx.research.program.ir.exception.NotCastConnectionDetailException;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class StaticConnection {
	
	private ConnectionInfo info = null;
//	private int type = 0;
//	private int num = 0;
	private IRForOneInstruction source = null;
	private IRForOneInstruction target = null;
	// Solved. whether int num should be parameter? should not.
	// int type
	public StaticConnection(IRForOneInstruction source, IRForOneInstruction target, ConnectionInfo info) {
		this.setSource(source);
		this.setTarget(target);
		// this.setType(type);
		this.setInfo(info);
	}
	
	public StaticConnection HorizontalMerge(StaticConnection another_connection) throws NotCastConnectionDetailException
	{
		if (source != another_connection.source || target != another_connection.target)
		{
			System.err.println("To_Merge Connection is wrong match source and target are not matched.");
			System.exit(1);
		}
		return new StaticConnection(source, target, info.HorizontalMerge(another_connection.info));
	}
	
	public boolean IsTarget(IRForOneInstruction node)
	{
		if (node == getTarget())
		{
			return true;
		}
		return false;
	}
	
	public boolean IsSource(IRForOneInstruction node)
	{
		if (node == getSource())
		{
			return true;
		}
		return false;
	}

	public IRForOneInstruction getSource() {
		return source;
	}

	private void setSource(IRForOneInstruction source) {
		this.source = source;
	}

	public IRForOneInstruction getTarget() {
		return target;
	}

	private void setTarget(IRForOneInstruction target) {
		this.target = target;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
        int result = info.hashCode();
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StaticConnection)
		{
			StaticConnection cnt = (StaticConnection) obj;
			if (info.equals(cnt.info))
			{
				if (source == cnt.source)
				{
					if (target == cnt.target)
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public ConnectionInfo GetStaticConnectionInfo()
	{
		return info;
	}

	public ConnectionInfo getInfo() {
		return info;
	}

	private void setInfo(ConnectionInfo info) {
		this.info = info;
	}
	
	@Override
	public String toString() {
		return source.toString() + "&" + target.toString() + "&" + info.toString();
	}
	
}
