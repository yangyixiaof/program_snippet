package cn.yyx.research.program.ir.storage.node.lowlevel;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;

public class IRForOneBranchControl extends IRForOneInstruction {
	
	private static IRForOneBranchControl empty_branch_control = new IRForOneBranchControl(null, null, null, null);
	public static IRForOneBranchControl GetEmptyControlHolder() {
		return empty_branch_control;
	}
	
	protected IRBranchControlType branch_type = null;
	
	public IRForOneBranchControl(IJavaElement im, IRCode parent_env, Class<? extends IIRNodeTask> task_class, IRBranchControlType branch_type) {
		super(im, parent_env, task_class);
		this.branch_type = branch_type;
	}
	
	public IRBranchControlType GetBranchType()
	{
		return branch_type;
	}

	@Override
	public String ToVisual() {
		return im.getElementName() + "^Branch:" + branch_type.Value();
	}
	
	@Override
	public String toString() {
		return ToVisual();
	}
	
}
