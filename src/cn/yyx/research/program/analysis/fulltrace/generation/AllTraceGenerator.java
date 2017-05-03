package cn.yyx.research.program.analysis.fulltrace.generation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;

import cn.yyx.research.program.analysis.fulltrace.FullTrace;
import cn.yyx.research.program.ir.generation.IRGeneratorForOneProject;
import cn.yyx.research.program.ir.orgranization.IRTreeForOneElement;
import cn.yyx.research.program.ir.storage.node.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneMethod;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneSourceMethodInvocation;

public class AllTraceGenerator {
	
	private IMethod root = null;
	private Map<IRForOneSourceMethodInvocation, IMethod> method_invocation = new HashMap<IRForOneSourceMethodInvocation, IMethod>();
	
	private List<FullTrace> traces = new LinkedList<FullTrace>();
	
	public boolean SelectOneMethod(IRForOneSourceMethodInvocation irfomi, Set<IMethod> methods, boolean is_last)
	{
		if (irfomi != null) {
			if (method_invocation.containsKey(irfomi)) {
				return false;
			}
		}
		Iterator<IMethod> mitr = methods.iterator();
		while (mitr.hasNext())
		{
			IMethod im = mitr.next();
			if (irfomi == null) {
				root = im;
				method_invocation.clear();
			} else {
				method_invocation.put(irfomi, im);
			}
			IRForOneMethod irfom = IRGeneratorForOneProject.GetInstance().GetMethodIR(im);
			if (irfom == null) {
				continue;
			} else {
				IRTreeForOneElement irtree = irfom.GetSourceMethodInvocations();
				IRForOneInstruction root_instr = irtree.GetRootNode();
				List<IRForOneSourceMethodInvocation> method_invokes = SearchAllSourceMethodInvocation(root_instr);
				Iterator<IRForOneSourceMethodInvocation> mi_itr = method_invokes.iterator();
				boolean over_choice = false;
				while (mi_itr.hasNext())
				{
					IRForOneSourceMethodInvocation source_mi = mi_itr.next();
					boolean has_choice = SelectOneMethod(source_mi, new HashSet<IMethod>(source_mi.GetAllMethods()), is_last && !mi_itr.hasNext());
					over_choice = over_choice || has_choice;
				}
				if (!over_choice && is_last)
				{
					CodeOnOneTraceGenerator cootg = new CodeOnOneTraceGenerator(root, method_invocation);
					FullTrace one_full_trace = cootg.Execute();
					traces.add(one_full_trace);
				}
			}
		}
		return true;
	}
	
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
				Set<IRForOneInstruction> set = IRGeneratorForOneProject.GetInstance().GetOutINodesByContainingSpecificType(irfoi, EdgeBaseType.Self.getType());
				Iterator<IRForOneInstruction> sitr = set.iterator();
				while (sitr.hasNext())
				{
					IRForOneInstruction si = sitr.next();
					if (si instanceof IRForOneSourceMethodInvocation && !result.contains(si))
					{
						result.add((IRForOneSourceMethodInvocation)si);
						new_level.add(si);
					}
				}
			}
			level.clear();
			level.addAll(new_level);
		}
		return result;
	}
	
}
