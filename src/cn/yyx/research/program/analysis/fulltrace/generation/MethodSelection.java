package cn.yyx.research.program.analysis.fulltrace.generation;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IMethod;

import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneSourceMethodInvocation;

public class MethodSelection {
	
	private IMethod root = null;
	private Map<IRForOneSourceMethodInvocation, IMethod> method_invocation = new HashMap<IRForOneSourceMethodInvocation, IMethod>();
	
	public MethodSelection(IMethod root, Map<IRForOneSourceMethodInvocation, IMethod> method_invocation) {
		this.root = root;
		this.method_invocation.putAll(method_invocation);
	}
	
	public IMethod GetRoot()
	{
		return root;
	}
	
	public IMethod GetMethodSelection(IRForOneSourceMethodInvocation irfosm)
	{
		return method_invocation.get(irfosm);
	}
	
}
