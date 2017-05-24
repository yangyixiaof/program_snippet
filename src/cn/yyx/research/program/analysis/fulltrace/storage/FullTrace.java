package cn.yyx.research.program.analysis.fulltrace.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.analysis.fulltrace.storage.connection.DynamicConnection;
import cn.yyx.research.program.analysis.fulltrace.storage.node.DynamicNode;
import cn.yyx.research.program.ir.storage.node.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnectionInfo;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneBranchControl;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;
import cn.yyx.research.program.ir.visual.node.IVNode;
import cn.yyx.research.program.ir.visual.node.connection.IVConnection;
import cn.yyx.research.program.ir.visual.node.container.IVNodeContainer;

public class FullTrace implements IVNodeContainer {
	
	Map<DynamicNode, Map<DynamicNode, DynamicConnection>> in_conns = new HashMap<DynamicNode, Map<DynamicNode, DynamicConnection>>();
	Map<DynamicNode, Map<DynamicNode, DynamicConnection>> out_conns = new HashMap<DynamicNode, Map<DynamicNode, DynamicConnection>>();
	
	Map<IJavaElement, DynamicNode> root_pc = new HashMap<IJavaElement, DynamicNode>();
	Map<IJavaElement, DynamicNode> last_pc = new HashMap<IJavaElement, DynamicNode>();
	Map<IJavaElement, Set<DynamicNode>> ele_nodes = new HashMap<IJavaElement, Set<DynamicNode>>();
	
	public FullTrace() {
		
	}
	
	public Set<IVNode> GetRootsForVisual() {
		Set<IVNode> result = new HashSet<IVNode>();
		result.addAll(root_pc.values());
		return result;
	}
	
	public Collection<DynamicConnection> GetInConnections(DynamicNode node) {
		Map<DynamicNode, DynamicConnection> ins = in_conns.get(node);
		if (ins == null) {
			return new HashSet<DynamicConnection>();
		}
		return ins.values();
	}
	
	private void HandleConnection(DynamicNode source, DynamicNode target, DynamicConnection conn, Map<DynamicNode, Map<DynamicNode, DynamicConnection>> conns)
	{
		Map<DynamicNode, DynamicConnection> source_map = conns.get(source);
		if (source_map == null) {
			source_map = new HashMap<DynamicNode, DynamicConnection>();
			conns.put(source, source_map);
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
		DynamicNode source_dn = conn.GetSource();
		IRForOneInstruction instr = source_dn.getInstr();
		IJavaElement ije = instr.getIm();
		Set<DynamicNode> created_nodes = ele_nodes.get(ije);
		if (instr instanceof IRForOneBranchControl || !created_nodes.contains(source_dn)) {
			return;
		}
		HandleConnection(conn.GetTarget(), conn.GetSource(), conn, in_conns);
		HandleConnection(conn.GetSource(), conn.GetTarget(), conn, out_conns);
	}
	
	public void NodeCreated(IJavaElement ije, DynamicNode new_dn)
	{
		IRForOneInstruction instr = new_dn.getInstr();
		if (instr instanceof IRForOneBranchControl) {
			return;
		}
		Set<DynamicNode> nset = ele_nodes.get(ije);
		if (nset == null) {
			nset = new HashSet<DynamicNode>();
			ele_nodes.put(ije, nset);
		}
		if (!nset.contains(new_dn)) {
			nset.add(new_dn);
			DynamicNode last_dn = last_pc.get(ije);
			last_pc.put(ije, new_dn);
			if (last_dn != null) {
				DynamicConnection dc = new DynamicConnection(last_dn, new_dn, EdgeBaseType.Self.Value());
				AddConnection(dc);
			} else {
				root_pc.put(ije, new_dn);
			}
		}
	}

	@Override
	public Set<IVConnection> GetOutConnection(IVNode source) {
		Set<IVConnection> result = new HashSet<IVConnection>();
		Map<DynamicNode, DynamicConnection> out_map = out_conns.get(source);
		if (out_map != null) {
			Set<DynamicNode> okeys = out_map.keySet();
			Iterator<DynamicNode> oitr = okeys.iterator();
			while (oitr.hasNext()) {
				DynamicNode irfoi = oitr.next();
				DynamicConnection sc = out_map.get(irfoi);
				IVConnection ivc = new IVConnection(source, irfoi, new StaticConnectionInfo(sc.getType()));
				result.add(ivc);
			}
		}
		return result;
	}
	
}
