package cn.yyx.research.program.ir.visual.dot.generation;

import java.util.HashSet;
import java.util.Set;

import cn.yyx.research.program.ir.visual.node.IVNode;
import cn.yyx.research.program.ir.visual.node.connection.IVConnection;

public class DotCluster {
	
	private Set<IVNode> ivns = new HashSet<IVNode>();
	private Set<IVConnection> ivn_conns = new HashSet<IVConnection>();
	
	public DotCluster(IVNode ivn) {
		GetIvns().add(ivn);
	}
	
	public void AddIVNode(IVNode ivn) {
		GetIvns().add(ivn);
	}
	
	public void AddIVConnection(IVConnection ivn_conn) {
		GetIvnConns().add(ivn_conn);
	}
	
	public void Merge(DotCluster cluster) {
		GetIvns().addAll(cluster.GetIvns());
		GetIvnConns().addAll(cluster.GetIvnConns());
	}

	public Set<IVNode> GetIvns() {
		return ivns;
	}

	public Set<IVConnection> GetIvnConns() {
		return ivn_conns;
	}
	
	@Override
	public String toString() {
		return "nodes:" + ivns + ";conns:" + ivn_conns;
	}
	
}
