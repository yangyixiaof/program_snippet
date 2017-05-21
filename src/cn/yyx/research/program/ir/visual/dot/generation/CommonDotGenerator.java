package cn.yyx.research.program.ir.visual.dot.generation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import cn.yyx.research.program.ir.storage.node.connection.StaticConnectionInfo;
import cn.yyx.research.program.ir.visual.node.IVNode;
import cn.yyx.research.program.ir.visual.node.connection.IVConnection;
import cn.yyx.research.program.ir.visual.node.container.IVNodeContainer;

public class CommonDotGenerator {
	
	String dot_file = null;
	Set<IVNode> pc = null;
	IVNodeContainer ivc = null;
	int idx = 0;
	Map<IVNode, Integer> ivn_id = new HashMap<IVNode, Integer>();
	
	public CommonDotGenerator(Set<IVNode> pc, IVNodeContainer ivc, String dot_file) {
		this.dot_file = dot_file;
		this.pc = pc;
		this.ivc = ivc;
	}
	
	public void GenerateDot() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dot_file)));
			Set<IVNode> already_visit = new HashSet<IVNode>();
			Map<IVNode, Integer> ivn_id = new HashMap<IVNode, Integer>();
			Iterator<IVNode> nitr = pc.iterator();
			while (nitr.hasNext()) {
				IVNode ivn = nitr.next();
				GenerateDotForOneTempRoot(ivn, already_visit, bw);
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private int GetNodeID(IVNode node) {
		Integer id = ivn_id.get(node);
		if (id == null) {
			id = idx++;
			ivn_id.put(node, id);
		}
		return id;
	}
	
	Map<IVNode, DotCluster> ivn_cluster = new HashMap<IVNode, DotCluster>();
	
	public DotCluster GetCluster(IVNode ivn) {
		DotCluster cluster = ivn_cluster.get(ivn);
		if (cluster == null) {
			cluster = new DotCluster(ivn);
			ivn_cluster.put(ivn, cluster);
		}
		return cluster;
	}
	
	public void GenerateDotForOneTempRoot(IVNode t_root, Set<IVNode> already_visit, BufferedWriter bw) {
		if (already_visit.contains(t_root)) {
			return;
		} else {
			already_visit.add(t_root);
		}
		Set<IVConnection> conns = ivc.GetOutConnection(t_root);
		Iterator<IVConnection> citr = conns.iterator();
		while (citr.hasNext()) {
			IVConnection conn = citr.next();
			IVNode source = conn.getSource();
			IVNode target = conn.getTarget();
			int source_id = GetNodeID(source);
			int target_id = GetNodeID(target);
			StaticConnectionInfo info = conn.getInfo();
			DotCluster source_cluster = GetCluster(source);
			DotCluster target_cluster = GetCluster(target);
			
			// TODO
			
		}
		
	}
	
}
