package cn.yyx.research.program.ir.method;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.IMethod;

public class IRForOneExtension extends IRForOneUnit {
	
	private List<IRForOneMethod> methods = new LinkedList<IRForOneMethod>();
	
	public IRForOneExtension(IMethod im, int start, int end, Collection<IRForOneMethod> methods, IRInstrKind ir_kind) {
		super(im, start, end, ir_kind);
		this.AddMethods(methods);
	}

	public Iterator<IRForOneMethod> MethodIterator() {
		return methods.iterator();
	}

	public void AddMethods(Collection<IRForOneMethod> methods) {
		this.methods.addAll(methods);
	}
	
}
