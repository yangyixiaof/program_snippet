package cn.yyx.research.program.analysis.fulltrace.generation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.IMethod;

import cn.yyx.research.program.ir.generation.IRGeneratorForOneProject;
import cn.yyx.research.program.ir.orgranization.IRTreeForOneElement;
import cn.yyx.research.program.ir.storage.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneMethod;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneSourceMethodInvocation;

public class InvokeMethodSelector {
	
	private IMethod root = null;
	// private Object mock_root = new Object();
	// the key of key-value of two maps below are IRForOneSourceMethodInvocation or Object.
	private Map<IRForOneSourceMethodInvocation, IMethod> method_invocation = new HashMap<IRForOneSourceMethodInvocation, IMethod>();
//	private Map<Object, Iterator<IMethod>> method_invocation_iterator = new HashMap<Object, Iterator<IMethod>>();
	private Stack<MethodHolder> im_stack = new Stack<MethodHolder>();
	private MethodHolder last_pop = null;
	private List<MethodSelection> method_selections = new LinkedList<MethodSelection>();
	
	private Set<IMethod> root_methods = null;
	
	public InvokeMethodSelector(Set<IMethod> methods) {
		this.root_methods = methods;
	}
	
	public void StartSelectMethodsProcess() {
		if (root_methods != null && !root_methods.isEmpty()) {
			im_stack.push(new MethodHolder(null, root_methods.iterator()));
			SelectAllMethods();
		}
	}
	
	private void SelectAllMethods() {
		while (!im_stack.isEmpty()) {
			MethodHolder mh = im_stack.pop();
			SelectOneMethod(mh);
		}
	}
	
	// IRForOneSourceMethodInvocation irfomi, Set<IMethod> methods, boolean is_last
	private void SelectOneMethod(MethodHolder mh)
	{
//		Object key = irfomi;
//		if (irfomi == null) {
//			key = mock_root;
//		}
		
		if (mh.irfomi != null) {
			if (method_invocation.containsKey(mh.irfomi)) {
				return;
			}
		}
		
		while (mh.imitr.hasNext())
		{
			IMethod im = mh.imitr.next();
			if (mh.irfomi == null) {
				root = im;
				method_invocation.clear();
				last_pop = null;
			} else {
				method_invocation.put(mh.irfomi, im);
			}
			if (im_stack.isEmpty()) {
				last_pop = mh;
			}
			IRForOneMethod irfom = IRGeneratorForOneProject.GetInstance().GetMethodIR(im);
			if (irfom == null) {
				continue;
			} else {
				IRTreeForOneElement irtree = irfom.GetSourceMethodInvocations();
				if (irtree != null) {
					IRForOneInstruction root_instr = irtree.GetRootNode();
					List<IRForOneSourceMethodInvocation> method_invokes = SearchAllSourceMethodInvocation(root_instr);
					Iterator<IRForOneSourceMethodInvocation> mi_itr = method_invokes.iterator();
//					boolean over_choice = false;
					while (mi_itr.hasNext())
					{
						IRForOneSourceMethodInvocation source_mi = mi_itr.next();
						List<IMethod> methods = source_mi.GetAllMethods();
						MethodHolder n_mh = null;
						if (methods != null && !methods.isEmpty()) {
							n_mh = new MethodHolder(source_mi, methods.iterator());
							im_stack.push(n_mh);
							last_pop = null;
							// SelectOneMethod();
						}
//						SelectOneMethod(source_mi, new HashSet<IMethod>(), is_last && !mi_itr.hasNext());
//						boolean has_choice = ...;
//						over_choice = over_choice || has_choice;
					}
					SelectAllMethods();
				}
			}
			if (last_pop != null && mh == last_pop) {
				MethodSelection ms = new MethodSelection(root, method_invocation);
				method_selections.add(ms);
			}
		}
		
//		Iterator<IMethod> mitr = methods.iterator();
//		while (mitr.hasNext())
//		{
//			IMethod im = mitr.next();
//			if (irfomi == null) {
//				root = im;
//				method_invocation.clear();
//			} else {
//				method_invocation.put(irfomi, im);
//			}
//			IRForOneMethod irfom = IRGeneratorForOneProject.GetInstance().GetMethodIR(im);
//			if (irfom == null) {
//				continue;
//			} else {
//				IRTreeForOneElement irtree = irfom.GetSourceMethodInvocations();
//				if (irtree != null) {
//					IRForOneInstruction root_instr = irtree.GetRootNode();
//					List<IRForOneSourceMethodInvocation> method_invokes = SearchAllSourceMethodInvocation(root_instr);
//					Iterator<IRForOneSourceMethodInvocation> mi_itr = method_invokes.iterator();
//					boolean over_choice = false;
//					while (mi_itr.hasNext())
//					{
//						IRForOneSourceMethodInvocation source_mi = mi_itr.next();
//						boolean has_choice = SelectOneMethod(source_mi, new HashSet<IMethod>(source_mi.GetAllMethods()), is_last && !mi_itr.hasNext());
//						over_choice = over_choice || has_choice;
//					}
//					if (!over_choice && is_last)
//					{
//						MethodSelection ms = new MethodSelection(root, method_invocation);
//						method_selections.add(ms);
//					}
//				}
//			}
//			if (irfomi == null) {
//				root = null;
//			} else {
//				method_invocation.remove(irfomi);
//			}
//		}
		return;
	}
	
	/**
	 * The order is reversed.
	 * @param root_instr
	 * @return
	 */
	private List<IRForOneSourceMethodInvocation> SearchAllSourceMethodInvocation(IRForOneInstruction root_instr)
	{
		List<IRForOneSourceMethodInvocation> result = new LinkedList<IRForOneSourceMethodInvocation>();
		Set<IRForOneInstruction> level = new HashSet<IRForOneInstruction>();
		level.add(root_instr);
		while (!level.isEmpty())
		{
			HashSet<IRForOneInstruction> new_level = new HashSet<IRForOneInstruction>();
			Iterator<IRForOneInstruction> itr = level.iterator();
			while (itr.hasNext())
			{
				IRForOneInstruction irfoi = itr.next();
				Set<IRForOneInstruction> set = IRGeneratorForOneProject.GetInstance().GetOutINodesByContainingSpecificType(irfoi, EdgeBaseType.Self.Value());
				Iterator<IRForOneInstruction> sitr = set.iterator();
				while (sitr.hasNext())
				{
					IRForOneInstruction si = sitr.next();
					if (si instanceof IRForOneSourceMethodInvocation && !result.contains(si))
					{
						result.add(0, (IRForOneSourceMethodInvocation)si);
						new_level.add(si);
					}
				}
			}
			level.clear();
			level.addAll(new_level);
		}
		return result;
	}

	public List<MethodSelection> GetMethodSelections() {
		return method_selections;
	}
	
	class MethodHolder {
		IRForOneSourceMethodInvocation irfomi = null;
		Iterator<IMethod> imitr = null;
		
		public MethodHolder(IRForOneSourceMethodInvocation irfomi, Iterator<IMethod> imitr) {
			this.irfomi = irfomi;
			this.imitr = imitr;
		}
		
	}
	
}
