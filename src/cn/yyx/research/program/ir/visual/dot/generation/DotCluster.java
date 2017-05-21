package cn.yyx.research.program.ir.visual.dot.generation;

import java.util.HashSet;
import java.util.Set;

import cn.yyx.research.program.ir.visual.node.IVNode;

public class DotCluster {
	
	Set<IVNode> ivns = new HashSet<IVNode>();
	
	public DotCluster(IVNode ivn) {
		ivns.add(ivn);
	}
	
	public void AddIVNode(IVNode ivn) {
		ivns.add(ivn);
	}
	
	public void Merge(DotCluster cluster) {
		ivns.addAll(cluster.ivns);
	}
	
}
