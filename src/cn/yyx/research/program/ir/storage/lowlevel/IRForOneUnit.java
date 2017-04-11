package cn.yyx.research.program.ir.storage.lowlevel;

import org.eclipse.jdt.core.IJavaElement;

public class IRForOneUnit {
	
	private IJavaElement im = null;
	// private int start = -1;
	// private int end = -1;
	// private IRInstrKind ir_kind = IRInstrKind.Weak;
	
	public IRForOneUnit(IJavaElement im) {
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
