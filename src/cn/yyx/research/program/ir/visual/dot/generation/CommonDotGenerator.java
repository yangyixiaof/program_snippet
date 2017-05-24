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

import cn.yyx.research.program.ir.storage.node.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.node.connection.EdgeTypeUtil;
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
	
	private void DrawConnections(Set<IVConnection> conns, StringBuffer one_bw, String line_seperator) {
		Iterator<IVConnection> conitr = conns.iterator();
		while (conitr.hasNext()) {
			IVConnection conn = conitr.next();
			String color = "black";
			StaticConnectionInfo info = conn.getInfo();
			int conn_type = info.getType();
			if (EdgeTypeUtil.HasSpecificType(conn_type, EdgeBaseType.SameOperations.Value())) {
				color = "green";
			}
			if (EdgeTypeUtil.HasSpecificType(conn_type, EdgeBaseType.Sequential.Value())) {
				color = "blue";
			}
			if (EdgeTypeUtil.HasSpecificType(conn_type, EdgeBaseType.Branch.Value())) {
				color = "red";
			}
			if (EdgeTypeUtil.HasSpecificType(conn_type, EdgeBaseType.Self.Value())) {
				color = "black";
			}
			IVNode source = conn.getSource();
			IVNode target = conn.getTarget();
			int source_id = GetNodeID(source);
			String source_node = "n" + source_id;
			int target_id = GetNodeID(target);
			String target_node = "n" + target_id;
			one_bw.append(source_node + "->" + target_node + "[color=" + color + "];" + line_seperator);
		}
	}
	
	public void GenerateDot() {
		try {
			Set<IVNode> already_visit = new HashSet<IVNode>();
			Iterator<IVNode> nitr = pc.iterator();
			while (nitr.hasNext()) {
				IVNode ivn = nitr.next();
				GenerateDotForOneTempRoot(ivn, already_visit);
			}
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dot_file)));
			bw.write("digraph {");
			bw.newLine();
			
			String line_seperator = System.getProperty("line.separator");
			Iterator<DotCluster> citr = clusters.iterator();
			int idx = 0;
			while (citr.hasNext()) {
				idx++;
				DotCluster dc = citr.next();
				StringBuffer cluster_bw = new StringBuffer();
				cluster_bw.append("subgraph cluster" + idx + " {" + line_seperator);
				cluster_bw.append("node [style = filled,color = white];" + line_seperator);
				cluster_bw.append("style = filled;" + line_seperator);
				cluster_bw.append("color = lightgrey;" + line_seperator);
				cluster_bw.append(line_seperator);
				
				DrawConnections(dc.GetIvnConns(), cluster_bw, line_seperator);
				
				cluster_bw.append("}" + line_seperator);
				bw.write(cluster_bw.toString());
			}
			
			StringBuffer share_bw = new StringBuffer();
			DrawConnections(non_cluster_ivn_conns, share_bw, line_seperator);
			bw.write(share_bw.toString());
			
			StringBuffer node_bf = new StringBuffer();
			Set<IVNode> iv_keys = ivn_id.keySet();
			Iterator<IVNode> iitr = iv_keys.iterator();
			while (iitr.hasNext()) {
				IVNode ivn = iitr.next();
				int ivn_id = GetNodeID(ivn);
				String ivn_node = "n" + ivn_id;
				node_bf.append(ivn_node + "[label=\"" + ivn.ToVisual() + "\"];" + line_seperator);
			}
			bw.write(node_bf.toString() + line_seperator);
			
			bw.newLine();
			bw.write("}");
			bw.newLine();
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
	Set<DotCluster> clusters = new HashSet<DotCluster>();
	
	public DotCluster GetCluster(IVNode ivn) {
		DotCluster cluster = ivn_cluster.get(ivn);
		if (cluster == null) {
			cluster = new DotCluster(ivn);
			ivn_cluster.put(ivn, cluster);
			clusters.add(cluster);
		}
		return cluster;
	}
	
	Set<IVConnection> non_cluster_ivn_conns = new HashSet<IVConnection>();
	
	private void GenerateDotForOneTempRoot(IVNode t_root, Set<IVNode> already_visit) {
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
			StaticConnectionInfo info = conn.getInfo();
			DotCluster source_cluster = GetCluster(source);
			DotCluster target_cluster = GetCluster(target);
			if (EdgeTypeUtil.HasSpecificType(info.getType(), EdgeBaseType.Self.Value())) {
				clusters.remove(target_cluster);
				source_cluster.Merge(target_cluster);
				source_cluster.AddIVConnection(conn);
				Set<IVNode> target_ivns = target_cluster.GetIvns();
				Iterator<IVNode> titr = target_ivns.iterator();
				while (titr.hasNext()) {
					IVNode tivn = titr.next();
					ivn_cluster.put(tivn, source_cluster);
				}
			} else {
				non_cluster_ivn_conns.add(conn);
			}
			GenerateDotForOneTempRoot(target, already_visit);
		}
	}
	
}
