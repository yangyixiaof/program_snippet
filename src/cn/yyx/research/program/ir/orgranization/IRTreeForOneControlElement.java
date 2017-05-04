package cn.yyx.research.program.ir.orgranization;

import java.util.Stack;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.ir.storage.node.execution.SkipSelfTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRBranchControlType;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneBranchControl;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class IRTreeForOneControlElement extends IRTreeForOneElement {
	
	// HashMap<IJavaElement, > 
	protected Stack<IRForOneBranchControl> branchs_var_instr_order = new Stack<IRForOneBranchControl>();
	
	public IRTreeForOneControlElement(IJavaElement ije, IRCode parent_env) {
		super(ije, parent_env);
	}
	
	@Override
	public void SwitchDirection(IRForOneInstruction switch_to_last_node) {
		super.SwitchDirection(switch_to_last_node);
		// TODO switch logic needs to be completed.
		IRForOneBranchControl irfobc = new IRForOneBranchControl(im, parent_env, SkipSelfTask.class, IRBranchControlType.Branch);
		GoForwardANode(irfobc);
		
	}
	
}
