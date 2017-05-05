package cn.yyx.research.program.ir.storage.node.highlevel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;

import cn.yyx.research.program.ir.orgranization.IRTreeForOneControlElement;
import cn.yyx.research.program.ir.orgranization.IRTreeForOneElement;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public abstract class IRCode {

	protected Map<IJavaElement, Set<IJavaElement>> deps = new HashMap<IJavaElement, Set<IJavaElement>>();
	protected Map<IJavaElement, IRTreeForOneElement> irs = new HashMap<IJavaElement, IRTreeForOneElement>();
	protected Map<IJavaElement, IRForOneInstruction> out_nodes = new HashMap<IJavaElement, IRForOneInstruction>();
	protected IJavaElement source_method_receiver_element = null;
	
	protected IJavaElement control_logic_holder_element = null;
	protected IRTreeForOneControlElement control_logic_element_ir = null;
	
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
	
	public Set<IJavaElement> GetAllElements() {
		return irs.keySet();
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
	
	public IRTreeForOneElement GetIRTreeForOneElement(IJavaElement ije)
	{
		return irs.get(ije);
	}
	
	public void SwitchDirection(IJavaElement ije, IRForOneInstruction switch_to_last_node)
	{
		IRTreeForOneElement irtree = irs.get(ije);
		if (irtree != null) {
			irtree.SwitchDirection(switch_to_last_node);
		}
	}

	public void GoForwardOneIRTreeNode(IJavaElement ije, IRForOneInstruction irfou) {
		IRTreeForOneElement ir_ele = irs.get(ije);
		if (ir_ele == null) {
			ir_ele = new IRTreeForOneElement(ije, this);
			irs.put(ije, ir_ele);
		}
		ir_ele.GoForwardANode(irfou);
	}

	public abstract void AddParameter(IJavaElement im);

//	public IRTreeForOneElement GetOneAllIRUnits(IJavaElement ivb) {
//		return irs.get(ivb);
//	}
	
	public boolean HasElement(IJavaElement ije)
	{
		IRTreeForOneElement ii = irs.get(ije);
		if (ii == null) {
			return false;
		}
		return ii.HasElement();
	}

	public IRForOneInstruction GetFirstIRTreeNode(IJavaElement ije) {
		IRTreeForOneElement ii = irs.get(ije);
		if (ii == null) {
			return null;
		}
		return ii.GetRootNode();
	}
	
	public IRForOneInstruction GetLastIRTreeNode(IJavaElement ije) {
		IRTreeForOneElement ii = irs.get(ije);
		if (ii == null) {
			return null;
		}
		return ii.GetLastNode();
	}

//	public IRForOneInstruction GetIRUnitByIndex(IJavaElement ivb, int index) {
//		LinkedList<IRForOneInstruction> ii = irs.get(ivb);
//		if (ii != null && ii.size() > index) {
//			return ii.get(index);
//		}
//		return null;
//	}

	public void AddAssignDependency(IJavaElement ije, Set<IJavaElement> assign_depend_set) {
		deps.put(ije, assign_depend_set);
	}

	public Set<IJavaElement> GetAssignDependency(IJavaElement ije) {
		return deps.get(ije);
	}

	public IMember GetScopeIElement() {
		return im;
	}

	public Map<IJavaElement, IRForOneInstruction> CopyEnvironment() {
		Map<IJavaElement, IRForOneInstruction> env = new HashMap<IJavaElement, IRForOneInstruction>();
		Set<IJavaElement> ikeys = irs.keySet();
		Iterator<IJavaElement> iitr = ikeys.iterator();
		while (iitr.hasNext()) {
			IJavaElement ije = iitr.next();
			env.put(ije, irs.get(ije).GetLastNode());
		}
		return env;
	}
	
	public Map<IJavaElement, IRForOneInstruction> CopyEnvironment(Set<IJavaElement> elements) {
		Map<IJavaElement, IRForOneInstruction> env = new HashMap<IJavaElement, IRForOneInstruction>();
		Iterator<IJavaElement> iitr = elements.iterator();
		while (iitr.hasNext()) {
			IJavaElement ije = iitr.next();
			env.put(ije, irs.get(ije).GetLastNode());
		}
		return env;
	}

	public void SetSourceMethodElement(IJavaElement source_method_receiver_element) {
		this.source_method_receiver_element = source_method_receiver_element;
		InitializeIRTreeElement(this.source_method_receiver_element);
	}
	
	public void SetControlLogicHolderElement(IJavaElement control_logic_holder_element) {
		this.control_logic_holder_element = control_logic_holder_element;
		control_logic_element_ir = new IRTreeForOneControlElement(control_logic_holder_element, this);
	}
	
	public void InitializeIRTreeElement(IJavaElement ije) {
		IRTreeForOneElement ir_ele = irs.get(ije);
		if (ir_ele == null) {
			ir_ele = new IRTreeForOneElement(ije, this);
		}
		irs.put(ije, ir_ele);
	}
	
	public IRTreeForOneElement GetSourceMethodInvocations() {
		return irs.get(source_method_receiver_element);
	}
	
//	private void AddDependency(IIRNode source, IIRNode target, EdgeConnectionType et) {
//		Set<StaticConnection> rset = in_connects.get(target);
//		if (rset == null) {
//			rset = new HashSet<StaticConnection>();
//			in_connects.put(target, rset);
//		}
//		rset.add(new StaticConnection(source, target, et));
//	}
//
//	public void AddSequentialDependency(IIRNode source, IIRNode target) {
//		AddDependency(source, target, EdgeBaseType.Sequential.getType(), 0);
//	}
//
//	public void AddSelfDependency(IIRNode source, IIRNode target) {
//		AddDependency(source, target, EdgeBaseType.Self.getType(), 0);
//	}
//
//	public void AddBranchDependency(IIRNode source, IIRNode target) {
//		AddDependency(source, target, EdgeBaseType.Branch.getType(), 0);
//	}
	
}
