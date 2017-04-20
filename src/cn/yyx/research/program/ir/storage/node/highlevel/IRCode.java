package cn.yyx.research.program.ir.storage.node.highlevel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;

import cn.yyx.research.program.ir.storage.node.IIRNode;
import cn.yyx.research.program.ir.storage.node.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.node.connection.EdgeConnectionType;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnection;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public abstract class IRCode {

	Map<IJavaElement, Set<IJavaElement>> deps = new HashMap<IJavaElement, Set<IJavaElement>>();
	Map<IJavaElement, LinkedList<IRForOneInstruction>> irs = new HashMap<IJavaElement, LinkedList<IRForOneInstruction>>();
	Map<IJavaElement, IRForOneInstruction> out_nodes = new HashMap<IJavaElement, IRForOneInstruction>();

	private IMember im = null;

	public IMember getIm() {
		return im;
	}

	private void setIm(IMember im) {
		this.im = im;
	}

	public IRCode(IMember im) {
		this.setIm(im);
	}
	
	public void PutOutNodes(IJavaElement ijele, IRForOneInstruction irfoi) {
		out_nodes.put(ijele, irfoi);
	}
	
	public Map<IJavaElement, IRForOneInstruction> GetOutNodes() {
		return out_nodes;
	}

//	private Set<StaticConnection> GetConnects(IIRNode node, Map<IIRNode, Set<StaticConnection>> connects) {
//		Set<StaticConnection> result = new HashSet<StaticConnection>();
//		Set<StaticConnection> ics = connects.get(node);
//		if (ics != null) {
//			result.addAll(ics);
//		}
//		return result;
//	}
//
//	public void AddConnection(StaticConnection conn) {
//		IIRNode source = conn.getSource();
//		IIRNode target = conn.getTarget();
//		{
//			Set<StaticConnection> in_set = in_connects.get(target);
//			if (in_set == null) {
//				in_set = new HashSet<StaticConnection>();
//				in_connects.put(target, in_set);
//			}
//			in_set.add(conn);
//		}
//		{
//			Set<StaticConnection> out_set = out_connects.get(source);
//			if (out_set == null) {
//				out_set = new HashSet<StaticConnection>();
//				out_connects.put(source, out_set);
//			}
//			out_set.add(conn);
//		}
//	}

	public void AddOneIRUnit(IJavaElement ivb, IRForOneInstruction irfou) {
		LinkedList<IRForOneInstruction> list = irs.get(ivb);
		if (list == null) {
			list = new LinkedList<IRForOneInstruction>();
			irs.put(ivb, list);
		}
		list.add(irfou);
	}

	public abstract void AddParameter(IJavaElement im);

	public List<IRForOneInstruction> GetOneAllIRUnits(IJavaElement ivb) {
		return irs.get(ivb);
	}

	public IRForOneInstruction GetLastIRUnit(IJavaElement ivb) {
		LinkedList<IRForOneInstruction> ii = irs.get(ivb);
		if (ii == null) {
			return null;
		}
		return ii.getLast();
	}

	public IRForOneInstruction GetIRUnitByIndex(IJavaElement ivb, int index) {
		LinkedList<IRForOneInstruction> ii = irs.get(ivb);
		if (ii != null && ii.size() > index) {
			return ii.get(index);
		}
		return null;
	}

	public void AddAssignDependency(IJavaElement ije, Set<IJavaElement> assign_depend_set) {
		deps.put(ije, assign_depend_set);
	}

	public Set<IJavaElement> GetAssignDependency(IJavaElement ije) {
		return deps.get(ije);
	}

	public IMember GetScopeIElement() {
		return im;
	}

	public Map<IJavaElement, Integer> CopyEnvironment() {
		Map<IJavaElement, Integer> env = new HashMap<IJavaElement, Integer>();
		Set<IJavaElement> ikeys = irs.keySet();
		Iterator<IJavaElement> iitr = ikeys.iterator();
		while (iitr.hasNext()) {
			IJavaElement ije = iitr.next();
			env.put(ije, irs.get(ije).size() - 1);
		}
		return env;
	}

	private void AddDependency(IIRNode source, IIRNode target, EdgeConnectionType et) {
		Set<StaticConnection> rset = in_connects.get(target);
		if (rset == null) {
			rset = new HashSet<StaticConnection>();
			in_connects.put(target, rset);
		}
		rset.add(new StaticConnection(source, target, et));
	}

	public void AddSequentialDependency(IIRNode source, IIRNode target) {
		AddDependency(source, target, EdgeBaseType.Sequential.getType(), 0);
	}

	public void AddSelfDependency(IIRNode source, IIRNode target) {
		AddDependency(source, target, EdgeBaseType.Self.getType(), 0);
	}

	public void AddBranchDependency(IIRNode source, IIRNode target) {
		AddDependency(source, target, EdgeBaseType.Branch.getType(), 0);
	}
	
}
