package cn.yyx.research.program.ir.storage.node.lowlevel;

import java.lang.reflect.Constructor;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.ir.storage.node.IIRNode;
import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;

public abstract class IRForOneInstruction implements IIRNode {
	
	protected IJavaElement im = null;
	protected IRCode parent_env = null;
	
	protected IIRNodeTask iirtask = null;
	
	// private int start = -1;
	// private int end = -1;
	// private IRInstrKind ir_kind = IRInstrKind.Weak;
	
	public IRForOneInstruction(IJavaElement im, IRCode parent_env, Class<? extends IIRNodeTask> task_class) {
		// , int start, int end, IRInstrKind ir_kind
		this.setIm(im);
		this.setParentEnv(parent_env);
		Constructor<?> cons[] = task_class.getConstructors();
		for (Constructor<?> con : cons) {
			Class<?>[] para_types = con.getParameterTypes();
			if (para_types != null && para_types.length == 1 && IIRNodeTask.class.isAssignableFrom(para_types[0])) {
				try {
					iirtask = (IIRNodeTask) con.newInstance(this);
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Error in new IIRTask.");
					System.exit(1);
				}
			}
		}
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
	
}
