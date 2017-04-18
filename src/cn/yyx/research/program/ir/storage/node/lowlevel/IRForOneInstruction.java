package cn.yyx.research.program.ir.storage.node.lowlevel;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.ir.storage.node.IIRNode;
import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.connection.Connection;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;

public abstract class IRForOneInstruction implements IIRNode {
	
	protected IJavaElement im = null;
	protected IRCode parent_env = null;
	
	protected Map<Connection, IIRNodeTask> conn_task = new HashMap<Connection, IIRNodeTask>();
	// private int start = -1;
	// private int end = -1;
	// private IRInstrKind ir_kind = IRInstrKind.Weak;
	
	public IRForOneInstruction(IJavaElement im, IRCode parent_env) {
		// , int start, int end, IRInstrKind ir_kind
		this.setIm(im);
		this.setParentEnv(parent_env);
//		this.setStart(start);
//		this.setEnd(end);
//		this.setIr_kind(ir_kind);
	}
	
	public IJavaElement getIm() {
		return im;
	}

	private void setIm(IJavaElement im) {
		this.im = im;
	}

	public IRCode getParentEnv() {
		return parent_env;
	}

	private void setParentEnv(IRCode parent_env) {
		this.parent_env = parent_env;
	}
	
	@Override
	public void PutConnectionMergeTask(Connection conn, IIRNodeTask run) {
		conn_task.put(conn, run);
	}

	@Override
	public IIRNodeTask GetConnectionMergeTask(Connection conn) {
		return conn_task.get(conn);
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
