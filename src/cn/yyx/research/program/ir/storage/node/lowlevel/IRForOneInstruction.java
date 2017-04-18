package cn.yyx.research.program.ir.storage.node.lowlevel;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.ir.connection.Connection;
import cn.yyx.research.program.ir.storage.node.IIRNode;

public abstract class IRForOneInstruction implements IIRNode {
	
	private IJavaElement im = null;
	
	private Set<Connection> in_connects = new HashSet<Connection>();
	private Set<Connection> out_connects = new HashSet<Connection>();
	// private int start = -1;
	// private int end = -1;
	// private IRInstrKind ir_kind = IRInstrKind.Weak;
	
	public IRForOneInstruction(IJavaElement im) {
		// , int start, int end, IRInstrKind ir_kind
		this.setIm(im);
//		this.setStart(start);
//		this.setEnd(end);
//		this.setIr_kind(ir_kind);
	}
	
	public IJavaElement getIm() {
		return im;
	}

	public void setIm(IJavaElement im) {
		this.im = im;
	}
	
//	public void AddParent(IRForOneJavaInstruction parent)
//	{
//		parents.add(parent);
//		parent.AddChild(this);
//	}
//	
//	private void AddChild(IRForOneJavaInstruction child)
//	{
//		children.add(child);
//	}

//	public int getStart() {
//		return start;
//	}
//
//	public void setStart(int start) {
//		this.start = start;
//	}
//
//	public int getEnd() {
//		return end;
//	}
//
//	public void setEnd(int end) {
//		this.end = end;
//	}
//
//	public IRInstrKind getIr_kind() {
//		return ir_kind;
//	}
//
//	public void setIr_kind(IRInstrKind ir_kind) {
//		this.ir_kind = ir_kind;
//	}
	
}
