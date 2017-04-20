package cn.yyx.research.program.ir.storage.node.connection;

import cn.yyx.research.program.ir.storage.node.IIRNode;

public class StaticConnection {
	
	private int type = 0;
	private int require_type = 0;
	private IIRNode source = null;
	private IIRNode target = null;
	
	public StaticConnection(IIRNode source, IIRNode target, int type, int require_type) {
		this.setSource(source);
		this.setTarget(target);
		this.setType(type);
		this.setRequireType(require_type);
	}
	
	public StaticConnection MergeStaticConnection(StaticConnection another_connection)
	{
		if (source != another_connection.source || target != another_connection.target)
		{
			System.err.println("To_Merge Connection is wrong match source and target are not matched.");
			System.exit(1);
		}
		return new StaticConnection(source, target, getType() | another_connection.getType(), getRequireType() | another_connection.getRequireType());
	}
	
	public boolean IsTarget(IIRNode node)
	{
		if (node == getTarget())
		{
			return true;
		}
		return false;
	}
	
	public boolean IsSource(IIRNode node)
	{
		if (node == getSource())
		{
			return true;
		}
		return false;
	}

	public IIRNode getSource() {
		return source;
	}

	private void setSource(IIRNode source) {
		this.source = source;
	}

	public IIRNode getTarget() {
		return target;
	}

	private void setTarget(IIRNode target) {
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

	public int getRequireType() {
		return require_type;
	}

	private void setRequireType(int require_type) {
		this.require_type = require_type;
	}
	
}
