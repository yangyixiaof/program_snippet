package cn.yyx.research.program.analysis.fulltrace.generation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;

import cn.yyx.research.program.ir.generation.IRGeneratorForOneProject;
import cn.yyx.research.program.ir.orgranization.IRTreeForOneElement;
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
				IRForOneInstruction root = irtree.GetRootNode();
				// TODO
			}
		}
	}
	
}
