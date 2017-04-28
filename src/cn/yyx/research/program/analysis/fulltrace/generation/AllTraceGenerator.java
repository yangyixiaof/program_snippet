package cn.yyx.research.program.analysis.fulltrace.generation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;

import cn.yyx.research.program.ir.generation.IRGeneratorForOneProject;
import cn.yyx.research.program.ir.orgranization.IRTreeForOneElement;
import cn.yyx.research.program.ir.storage.node.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneMethod;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneMethodInvocation;

public class AllTraceGenerator {
	
	private IMethod root = null;
	private Map<IRForOneMethodInvocation, IMethod> method_invocation = new HashMap<IRForOneMethodInvocation, IMethod>();
	
	public void SelectOneMethod(IRForOneMethodInvocation irfomi, Set<IMethod> methods)
	{
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
				// TODO
				Set<IRForOneMethodInvocation> method_invokes = SearchAllSourceMethodInvocation(root_instr);
				
			}
		}
	}
	
	private Set<IRForOneMethodInvocation> SearchAllSourceMethodInvocation(IRForOneInstruction root_instr)
	{
		Set<IRForOneMethodInvocation> result = new HashSet<IRForOneMethodInvocation>();
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
					if (si instanceof IRForOneMethodInvocation && !result.contains(si))
					{
						result.add((IRForOneMethodInvocation)si);
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
