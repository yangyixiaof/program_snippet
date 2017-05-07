package cn.yyx.research.program.ir.storage.node.connection;

import cn.yyx.research.program.ir.storage.node.zstatic.lowlevel.IRForOneInstruction;

public class StaticConnection {
	
	private int type = 0;
	private IRForOneInstruction source = null;
	private IRForOneInstruction target = null;
	
	public StaticConnection(IRForOneInstruction source, IRForOneInstruction target, int type) {
		this.setSource(source);
		this.setTarget(target);
		this.setType(type);
	}
	
	public StaticConnection MergeStaticConnection(StaticConnection another_connection)
	{
		if (source != another_connection.source || target != another_connection.target)
		{
			System.err.println("To_Merge Connection is wrong match source and target are not matched.");
			System.exit(1);
		}
		return new StaticConnection(source, target, getType() | another_connection.getType());
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
        int result = getType();
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StaticConnection)
		{
			StaticConnection cnt = (StaticConnection) obj;
			if (getType() == cnt.getType())
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

	public int getType() {
		return type;
	}

	private void setType(int type) {
		this.type = type;
	}
	
}
