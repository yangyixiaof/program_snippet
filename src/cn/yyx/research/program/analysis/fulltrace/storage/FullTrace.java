package cn.yyx.research.program.analysis.fulltrace.storage;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.analysis.fulltrace.storage.connection.DynamicConnection;
import cn.yyx.research.program.analysis.fulltrace.storage.node.DynamicNode;

public class FullTrace {
	
	Map<DynamicNode, Map<DynamicNode, DynamicConnection>> in_conns = new HashMap<DynamicNode, Map<DynamicNode, DynamicConnection>>();
	Map<DynamicNode, Map<DynamicNode, DynamicConnection>> out_conns = new HashMap<DynamicNode, Map<DynamicNode, DynamicConnection>>();
	
	Map<IJavaElement, DynamicNode> last_pc = new HashMap<IJavaElement, DynamicNode>();
	
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
	
	public DynamicNode GetLastPC(IJavaElement ije)
	{
		return last_pc.get(ije);
	}
	
	public void PutLastPC(IJavaElement ije, DynamicNode dn)
	{
		last_pc.put(ije, dn);
	}
	
}
