package cn.yyx.research.program.analysis.fulltrace.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.analysis.fulltrace.storage.connection.DynamicConnection;
import cn.yyx.research.program.analysis.fulltrace.storage.node.DynamicNode;
import cn.yyx.research.program.ir.storage.node.connection.EdgeBaseType;

public class FullTrace {
	
	Map<DynamicNode, Map<DynamicNode, DynamicConnection>> in_conns = new HashMap<DynamicNode, Map<DynamicNode, DynamicConnection>>();
	Map<DynamicNode, Map<DynamicNode, DynamicConnection>> out_conns = new HashMap<DynamicNode, Map<DynamicNode, DynamicConnection>>();
	
	Map<IJavaElement, DynamicNode> last_pc = new HashMap<IJavaElement, DynamicNode>();
	Map<IJavaElement, Set<DynamicNode>> ele_nodes = new HashMap<IJavaElement, Set<DynamicNode>>();
	
	public FullTrace() {
		
	}
	
	private void HandleConnection(DynamicNode source, DynamicNode target, DynamicConnection conn, Map<DynamicNode, Map<DynamicNode, DynamicConnection>> conns)
	{
		Map<DynamicNode, DynamicConnection> source_map = conns.get(source);
		if (source_map == null) {
			source_map = new HashMap<DynamicNode, DynamicConnection>();
			in_conns.put(source, source_map);
		}
		DynamicConnection cn = source_map.get(target);
		if (cn != null) {
			source_map.put(target, cn.Merge(conn));
		} else {
			source_map.put(target, conn);
		}
	}
	
	public void AddConnection(DynamicConnection conn)
	{
		HandleConnection(conn.GetTarget(), conn.GetSource(), conn, in_conns);
		HandleConnection(conn.GetSource(), conn.GetTarget(), conn, out_conns);
	}
	
	public void NodeCreated(IJavaElement ije, DynamicNode new_dn)
	{
		DynamicNode last_dn = last_pc.get(ije);
		Set<DynamicNode> nset = ele_nodes.get(ije);
		if (nset == null) {
			nset = new HashSet<DynamicNode>();
			ele_nodes.put(ije, nset);
		}
		if (!nset.contains(new_dn)) {
			last_pc.put(ije, new_dn);
			if (last_dn != null) {
				DynamicConnection dc = new DynamicConnection(last_dn, new_dn, EdgeBaseType.Self.Value());
				AddConnection(dc);
			}
		}
	}
	
}
