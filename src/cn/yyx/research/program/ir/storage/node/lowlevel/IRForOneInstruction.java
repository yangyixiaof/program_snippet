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
	
	protected IRForOneInstruction group = null;
	
	// private int start = -1;
	// private int end = -1;
	// private IRInstrKind ir_kind = IRInstrKind.Weak;
	
	public IRForOneInstruction(IJavaElement im, IRCode parent_env, Class<? extends IIRNodeTask> task_class) {
		// , int start, int end, IRInstrKind ir_kind
		this.setIm(im);
		this.setParentEnv(parent_env);
		iirtask = (IIRNodeTask) ReflectionInvoke.InvokeConstructor(task_class, new Object[]{this});
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

	public IRForOneInstruction GetGroup() {
		return group;
	}

	public void SetGroup(IRForOneInstruction group) {
		this.group = group;
	}
	
}
