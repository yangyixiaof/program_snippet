package cn.yyx.research.program.ir.storage.node.lowlevel;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.ir.storage.node.IIREnergyNode;
import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.visual.node.IVNode;
import cn.yyx.research.program.systemutil.ReflectionInvoke;

public abstract class IRForOneInstruction implements IIREnergyNode, IVNode {
	
	protected IJavaElement im = null;
	protected IRCode parent_env = null;
	
	protected IIRNodeTask iirtask = null;
	protected int accept_type = 0;
	protected int require_type = 0;
	
	// private int start = -1;
	// private int end = -1;
	// private IRInstrKind ir_kind = IRInstrKind.Weak;
	
	public IRForOneInstruction(IJavaElement im, IRCode parent_env, Class<? extends IIRNodeTask> task_class) {
		// , int start, int end, IRInstrKind ir_kind
		this.setIm(im);
		this.setParentEnv(parent_env);
		ReflectionInvoke.InvokeConstructor(task_class, new Object[]{this});
		if (iirtask == null) {
			System.err.println("IIRTask not initialized, serious errors, the system will exit.");
			System.exit(1);
		}
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
	
	public IIRNodeTask GetOutConnectionMergeTask()
	{
		return iirtask;
	}
	
	public void SetOutConnectionMergeTask(IIRNodeTask iirtask)
	{
		this.iirtask = iirtask;
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
	
	@Override
	public void SetRequireType(int require_type) {
		this.require_type = require_type;
	}
	
	@Override
	public int GetRequireType() {
		return require_type;
	}
	
	@Override
	public void SetAcceptType(int accept_type) {
		this.accept_type = accept_type;
	}
	
	@Override
	public int GetAcceptType() {
		return accept_type;
	}
	
}
