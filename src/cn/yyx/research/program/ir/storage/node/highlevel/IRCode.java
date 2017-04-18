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
import cn.yyx.research.program.ir.storage.node.connection.Connection;
import cn.yyx.research.program.ir.storage.node.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.node.connection.EdgeConnectionType;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public abstract class IRCode {

	Map<IJavaElement, Set<IJavaElement>> deps = new HashMap<IJavaElement, Set<IJavaElement>>();
	Map<IJavaElement, LinkedList<IRForOneInstruction>> irs = new HashMap<IJavaElement, LinkedList<IRForOneInstruction>>();
	Map<IIRNode, Set<Connection>> in_connects = new HashMap<IIRNode, Set<Connection>>();
	Map<IIRNode, Set<Connection>> out_connects = new HashMap<IIRNode, Set<Connection>>();

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

	public Connection GetSpecifiedConnection(IIRNode source, IIRNode target) {
		Set<Connection> ocnnts = out_connects.get(source);
		if (ocnnts == null) {
			return null;
		}
		Iterator<Connection> itr = ocnnts.iterator();
		while (itr.hasNext()) {
			Connection cnn = itr.next();
			if (cnn.getTarget() == target) {
				return cnn;
			}
		}
		return null;
	}

	private Set<Connection> GetConnects(IIRNode node, Map<IIRNode, Set<Connection>> connects) {
		Set<Connection> result = new HashSet<Connection>();
		Set<Connection> ics = connects.get(node);
		if (ics != null) {
			result.addAll(ics);
		}
		return result;
	}

	public void AddConnection(Connection conn) {
		IIRNode source = conn.getSource();
		IIRNode target = conn.getTarget();
		{
			Set<Connection> in_set = in_connects.get(target);
			if (in_set == null) {
				in_set = new HashSet<Connection>();
				in_connects.put(target, in_set);
			}
			in_set.add(conn);
		}
		{
			Set<Connection> out_set = out_connects.get(source);
			if (out_set == null) {
				out_set = new HashSet<Connection>();
				out_connects.put(source, out_set);
			}
			out_set.add(conn);
		}
	}

	public Set<Connection> GetInConnects(IIRNode node) {
		return GetConnects(node, in_connects);
	}

	public Set<Connection> GetOutConnects(IIRNode node) {
		return GetConnects(node, out_connects);
	}

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
		Set<Connection> rset = in_connects.get(target);
		if (rset == null) {
			rset = new HashSet<Connection>();
			in_connects.put(target, rset);
		}
		rset.add(new Connection(source, target, et));
	}

	public void AddSequentialDependency(IIRNode source, IIRNode target) {
		AddDependency(source, target, new EdgeConnectionType(EdgeBaseType.Sequential.getType()));
	}

	public void AddSelfDependency(IIRNode source, IIRNode target) {
		AddDependency(source, target, new EdgeConnectionType(EdgeBaseType.Self.getType()));
	}

	public void AddBranchDependency(IIRNode source, IIRNode target) {
		AddDependency(source, target, new EdgeConnectionType(EdgeBaseType.Branch.getType()));
	}

}
