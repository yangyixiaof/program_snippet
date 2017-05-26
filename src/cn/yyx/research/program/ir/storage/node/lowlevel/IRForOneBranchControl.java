package cn.yyx.research.program.ir.storage.node.lowlevel;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;

public class IRForOneBranchControl extends IRForOneInstruction {
	
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
		return "Branch:" + branch_type.name();
	}
	
}
