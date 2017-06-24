package cn.yyx.research.program.ir.visual.dot.generation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.yyx.research.program.ir.storage.connection.ConnectionInfo;
import cn.yyx.research.program.ir.storage.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.connection.EdgeTypeUtil;
import cn.yyx.research.program.ir.storage.connection.detail.ConnectionDetail;
import cn.yyx.research.program.ir.visual.node.IVNode;
import cn.yyx.research.program.ir.visual.node.connection.IVConnection;
import cn.yyx.research.program.ir.visual.node.container.IVNodeContainer;

public class CommonDotGenerator {
	
	String dot_file = null;
	Set<IVNode> pc = null;
	IVNodeContainer ivc = null;
	int idx = 0;
	Map<IVNode, Integer> ivn_id = new HashMap<IVNode, Integer>();
	String description = null;
	
	public CommonDotGenerator(Set<IVNode> pc, IVNodeContainer ivc, String dot_file, String description) {
		this.dot_file = dot_file;
		this.pc = pc;
		this.ivc = ivc;
		this.description = description;
	}
	
	private void DrawConnections(Set<IVConnection> conns, StringBuffer one_bw, String line_seperator) {
		Iterator<IVConnection> conitr = conns.iterator();
		while (conitr.hasNext()) {
			IVConnection conn = conitr.next();
			String color = "black";
			ConnectionInfo info = conn.getInfo();
			int conn_type = info.getType();
			if (EdgeTypeUtil.HasSpecificType(conn_type, EdgeBaseType.BranchControl.Value())) {
				color = "yellow";
			}
			if (EdgeTypeUtil.HasSpecificType(conn_type, EdgeBaseType.SequentialSameOperation.Value())) {
				color = "pink";
			}
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
			
			List<ConnectionDetail> details = info.GetDetails();
			if (details == null || details.size() == 0) {
				one_bw.append(source_node + "->" + target_node + "[color=" + color + "];" + line_seperator);
			} else {
				Iterator<ConnectionDetail> ditr = details.iterator();
				while (ditr.hasNext()) {
					ConnectionDetail cd = ditr.next();
					one_bw.append(source_node + "->" + target_node + "[label=\"" + cd.toString() + "\", color=" + color + "];" + line_seperator);
				}
			}
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
			bw.write("label=\"" + description + "\";");
			bw.newLine();
			
			String line_seperator = System.getProperty("line.separator");
			int cluster_idx = 0;
			Iterator<DotCluster> citr = clusters.iterator();
			while (citr.hasNext()) {
				cluster_idx++;
				DotCluster dc = citr.next();
				StringBuffer cluster_bw = new StringBuffer();
				cluster_bw.append("subgraph cluster" + cluster_idx + " {" + line_seperator);
				cluster_bw.append("node [style = filled,color = white];" + line_seperator);
				cluster_bw.append("style = filled;" + line_seperator);
				cluster_bw.append("color = lightgrey;" + line_seperator);
				cluster_bw.append("label = \"\";" + line_seperator);
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
	
	// Iterate IVGraph to sort each cluster.
	// Map<Integer, DotCluster> cluster_id = new TreeMap<Integer, DotCluster>();
	
	// Generating IVGraph full of IVNodes.
	Map<IVNode, DotCluster> ivn_cluster = new HashMap<IVNode, DotCluster>();
	Set<DotCluster> clusters = new HashSet<DotCluster>();
	// Map<DotCluster, Map<DotCluster, Integer>> cluster_conn = new HashMap<DotCluster, Map<DotCluster, Integer>>();
	
	private DotCluster GetCluster(IVNode ivn) {
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
			ConnectionInfo info = conn.getInfo();
			DotCluster source_cluster = GetCluster(source);
			DotCluster target_cluster = GetCluster(target);
			
			// showing.
//			System.out.println("===== now handling conn:" + conn);
			
			if (EdgeTypeUtil.HasSpecificType(info.getType(), EdgeBaseType.Self.Value())) {
				// Merge clusters.
				// handle the merge of cluster connections.
//				Map<DotCluster, Integer> merge_conn_cluster = cluster_conn.get(source_cluster);
//				if (merge_conn_cluster == null) {
//					merge_conn_cluster = new HashMap<DotCluster, Integer>();
//					cluster_conn.put(source_cluster, merge_conn_cluster);
//				}
//				Map<DotCluster, Integer> conn_cluster = cluster_conn.remove(target_cluster);
//				if (conn_cluster != null) {
//					Set<DotCluster> conn_keys = conn_cluster.keySet();
//					Iterator<DotCluster> conn_itr = conn_keys.iterator();
//					while (conn_itr.hasNext()) {
//						DotCluster dc = conn_itr.next();
//						if (dc != source_cluster) {
//							int to_merge_num = conn_cluster.get(dc);
//							Integer already_num = merge_conn_cluster.get(dc);
//							if (already_num == null) {
//								already_num = 0;
//							}
//							already_num += to_merge_num;
//							merge_conn_cluster.put(dc, already_num);
//						}
//					}
//				}
				
				// handle other merges.
				if (!source_cluster.equals(target_cluster)) {
					clusters.remove(target_cluster);
					source_cluster.Merge(target_cluster);
					Set<IVNode> target_ivns = target_cluster.GetIvns();
					Iterator<IVNode> titr = target_ivns.iterator();
					while (titr.hasNext()) {
						IVNode tivn = titr.next();
						ivn_cluster.put(tivn, source_cluster);
					}
				}
				source_cluster.AddIVConnection(conn);
			} else {
				non_cluster_ivn_conns.add(conn);
				
				// handle the recording of number of connections.
//				Map<DotCluster, Integer> conn_cluster = cluster_conn.get(source_cluster);
//				if (conn_cluster == null) {
//					conn_cluster = new HashMap<DotCluster, Integer>();
//					cluster_conn.put(source_cluster, conn_cluster);
//				}
//				Integer num = conn_cluster.get(target_cluster);
//				if (num == null) {
//					num = 0;
//				}
//				num++;
//				conn_cluster.put(target_cluster, num);
			}
			
			// showing.
//			for (DotCluster cluster : clusters) {
//				System.out.println("one cluster:" + cluster);
//			}
			
			GenerateDotForOneTempRoot(target, already_visit);
		}
	}
	
}
