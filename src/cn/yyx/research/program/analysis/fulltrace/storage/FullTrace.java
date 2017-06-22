package cn.yyx.research.program.analysis.fulltrace.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.analysis.fulltrace.storage.connection.DynamicConnection;
import cn.yyx.research.program.analysis.fulltrace.storage.node.DynamicNode;
import cn.yyx.research.program.ir.IRMeta;
import cn.yyx.research.program.ir.storage.connection.ConnectionInfo;
import cn.yyx.research.program.ir.storage.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.connection.EdgeTypeUtil;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneBranchControl;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;
import cn.yyx.research.program.ir.visual.node.IVNode;
import cn.yyx.research.program.ir.visual.node.connection.IVConnection;
import cn.yyx.research.program.ir.visual.node.container.IVNodeContainer;

public class FullTrace implements IVNodeContainer {
	
	Map<DynamicNode, Map<DynamicNode, DynamicConnection>> in_conns = new HashMap<DynamicNode, Map<DynamicNode, DynamicConnection>>();
	Map<DynamicNode, Map<DynamicNode, DynamicConnection>> out_conns = new HashMap<DynamicNode, Map<DynamicNode, DynamicConnection>>();
	
	Map<IJavaElement, Set<DynamicNode>> root_pc = new HashMap<IJavaElement, Set<DynamicNode>>();
	Map<DynamicNode, Set<DynamicNode>> waiting_replace_root_pc = new HashMap<DynamicNode, Set<DynamicNode>>();
	// Map<IJavaElement, Set<DynamicNode>> last_pc = new HashMap<IJavaElement, Set<DynamicNode>>();
	Map<IJavaElement, Set<DynamicNode>> ele_nodes = new HashMap<IJavaElement, Set<DynamicNode>>();
	
	String description = null;
	
	public FullTrace(String description) {
		this.description = description;
	}
	
	public String GetDescription() {
		return description;
	}
	
	public Set<IVNode> GetRootsForVisual() {
		Set<IVNode> result = new HashSet<IVNode>();
		Iterator<Set<DynamicNode>> ritr = root_pc.values().iterator();
		while (ritr.hasNext()) {
			result.addAll(ritr.next());
		}
		return result;
	}
	
	public Set<DynamicConnection> GetInConnections(DynamicNode node) {
		Map<DynamicNode, DynamicConnection> ins = in_conns.get(node);
		if (ins == null) {
			return new HashSet<DynamicConnection>();
		}
		return new HashSet<DynamicConnection>(ins.values());
	}
	
	private void HandleAddConnection(DynamicNode source, DynamicNode target, DynamicConnection conn, Map<DynamicNode, Map<DynamicNode, DynamicConnection>> conns)
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
	
	public void HandleRemoveConnection(DynamicNode source, DynamicNode target, Map<DynamicNode, Map<DynamicNode, DynamicConnection>> conns) {
		Map<DynamicNode, DynamicConnection> source_map = conns.get(source);
		DynamicConnection dc = source_map.remove(target);
		if (dc == null) {
			System.err.println("Strange! removed objects are null?");
		}
	}
	
	private void HandleRootsBeforeRemovingConnection(DynamicConnection conn) {
		// just handle waiting_replace_root_pc.
		if (EdgeTypeUtil.HasSpecificType(conn.getInfo().getType(), EdgeBaseType.Self.Value())) {
			DynamicNode source = conn.GetSource();
			DynamicNode target = conn.GetTarget();
			Set<DynamicNode> roots = root_pc.get(source.getInstr().getIm());
			if (roots != null && roots.contains(source)) {
				Set<DynamicNode> replace_nodes = waiting_replace_root_pc.get(source);
				if (replace_nodes == null) {
					replace_nodes = new HashSet<DynamicNode>();
					waiting_replace_root_pc.put(source, replace_nodes);
				}
				replace_nodes.add(target);
			}
		}
	}
	
	public void HandleRootsAfterRemovingAllConnections(Set<DynamicConnection> conns) {
		Iterator<DynamicConnection> citr = conns.iterator();
		while (citr.hasNext()) {
			DynamicConnection conn = citr.next();
			DynamicNode source = conn.GetSource();
			Map<DynamicNode, DynamicConnection> source_map = out_conns.get(source);
			if (source_map.isEmpty()) {
				IJavaElement ije = source.getInstr().getIm();
				Set<DynamicNode> created_nodes = ele_nodes.get(ije);
				created_nodes.remove(source);
				Set<DynamicNode> roots = root_pc.get(source.getInstr().getIm());
				if (waiting_replace_root_pc.containsKey(source)) {
					roots.remove(source);
					roots.addAll(waiting_replace_root_pc.remove(source));
				}
			}
		}
	}
	
	public void RemoveConnection(DynamicConnection conn) {
		HandleRootsBeforeRemovingConnection(conn);
		
		HandleRemoveConnection(conn.GetTarget(), conn.GetSource(), in_conns);
		HandleRemoveConnection(conn.GetSource(), conn.GetTarget(), out_conns);
	}
	
	public void AddConnection(DynamicConnection conn)
	{
		// debugging info print.
		DynamicNode source_dn = conn.GetSource();
		IRForOneInstruction instr = source_dn.getInstr();
		IJavaElement ije = instr.getIm();
		Set<DynamicNode> created_nodes = ele_nodes.get(ije);
		if (created_nodes == null || !created_nodes.contains(source_dn)) {
			System.err.println("Strange! created_nodes is null? ije:" + ije);
			System.err.println("Strange! created_nodes do not contain source_dn:" + source_dn);
			System.exit(1);
		}
		
		// IRForOneBranchControl is not needed to be handled.
		if (instr instanceof IRForOneBranchControl) {
			return;
		}
		// real logics.
		HandleAddConnection(conn.GetTarget(), conn.GetSource(), conn, in_conns);
		HandleAddConnection(conn.GetSource(), conn.GetTarget(), conn, out_conns);
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
				IVConnection ivc = null;
				try {
					ivc = new IVConnection(source, irfoi, (ConnectionInfo)(sc.getInfo().clone()));
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
					System.exit(1);
				}
				result.add(ivc);
			}
		}
		return result;
	}
	
	public DynamicConnection GetSpecifiedConnection(DynamicNode source, DynamicNode target) {
		Map<DynamicNode, DynamicConnection> ocnnts = out_conns.get(source);
		if (ocnnts == null) {
			return null;
		}
		DynamicConnection conn = ocnnts.get(target);
		return conn;
	}
	
	public void NodeCreated(IJavaElement ije, DynamicNode source_dn, DynamicNode new_dn, BranchControlForOneIRCode bcfir)
	{
		
		// debugging.
		if (new_dn.toString().trim().endsWith("System.out" + "#1")) {
			System.currentTimeMillis();
		}
		if (source_dn != null && source_dn.toString().trim().endsWith("System.out" + "#1")) {
			System.currentTimeMillis();
		}
		if (new_dn.toString().trim().endsWith(IRMeta.VirtualBranch + "#1")) {
			System.currentTimeMillis();
		}
		
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
			Set<DynamicNode> last_dns = bcfir.LastLastInstructions(ije);
			if (!last_dns.isEmpty()) {
				Set<DynamicNode> remove = new HashSet<DynamicNode>();
				Iterator<DynamicNode> litr = last_dns.iterator();
				while (litr.hasNext()) {
					DynamicNode last_dn = litr.next();
					if (!last_dn.IsSameGroup(new_dn)) {
						// Warning. Serious bug! The child node may be created before the parent node.
						// Warning. The reason is that the new_node is not created after all its in_connections been visited.
						AddConnection(new DynamicConnection(last_dn, new_dn, new ConnectionInfo(EdgeBaseType.Self.Value())));
						remove.add(last_dn);
					} else {
						if (last_dn.equals(source_dn)) {
							remove.add(last_dn);
						}
					}
				}
				last_dns.add(new_dn);
				last_dns.removeAll(remove);
			} else {
				last_dns.add(new_dn);
				if (!root_pc.containsKey(ije)) {
					root_pc.put(ije, new HashSet<DynamicNode>(last_dns));
				}
			}
		}
	}
	
}
