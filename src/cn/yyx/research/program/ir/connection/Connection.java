package cn.yyx.research.program.ir.connection;

import cn.yyx.research.program.ir.storage.node.IIRNode;

public class Connection {
	
	private EdgeType type = null;
	private IIRNode source = null;
	private IIRNode target = null;
	
	public Connection(IIRNode source, IIRNode target, EdgeType type) {
		this.setSource(source);
		this.setTarget(target);
		this.setType(type);
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

	public EdgeType getType() {
		return type;
	}

	public void setType(EdgeType type) {
		this.type = type;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
        int result = type.hashCode();
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Connection)
		{
			Connection cnt = (Connection) obj;
			if (type.equals(cnt.type))
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
	
}
