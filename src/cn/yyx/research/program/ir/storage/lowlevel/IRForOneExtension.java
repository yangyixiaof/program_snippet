package cn.yyx.research.program.ir.storage.lowlevel;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.IMember;

import cn.yyx.research.program.ir.storage.highlevel.IRForOneCloseBlockUnit;

public class IRForOneExtension extends IRForOneUnit {
	
	private List<IRForOneCloseBlockUnit> methods = new LinkedList<IRForOneCloseBlockUnit>();
	
	public IRForOneExtension(IMember im, int start, int end, Collection<IRForOneCloseBlockUnit> methods, IRInstrKind ir_kind) {
		super(im, start, end, ir_kind);
		this.AddMethods(methods);
	}

	public Iterator<IRForOneCloseBlockUnit> MethodIterator() {
		return methods.iterator();
	}

	public void AddMethods(Collection<IRForOneCloseBlockUnit> methods) {
		this.methods.addAll(methods);
	}
	
}
