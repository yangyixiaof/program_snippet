package cn.yyx.research.program.ir.visual.node.container;

import java.util.Set;

import cn.yyx.research.program.ir.visual.node.IVNode;
import cn.yyx.research.program.ir.visual.node.connection.IVConnection;

public interface IVNodeContainer {
	
	public Set<IVConnection> GetOutConnection(IVNode source);
	
}
