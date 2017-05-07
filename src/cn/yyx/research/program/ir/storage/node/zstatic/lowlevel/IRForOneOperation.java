package cn.yyx.research.program.ir.storage.node.zstatic.lowlevel;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.zstatic.highlevel.IRCode;

public class IRForOneOperation extends IRForOneInstruction {
	
	private String ir = null;
	
	public IRForOneOperation(IRCode parent_env, IJavaElement im, String ir, Class<? extends IIRNodeTask> task_class) {
		super(im, parent_env, task_class);
		this.setIr(ir);
	}

	public String getIr() {
		return ir;
	}

	public void setIr(String ir) {
		this.ir = ir;
	}

//	@Override
//	public Map<IIRNode, Set<StaticConnection>> PrepareOutNodes() {
//		Map<IIRNode, Set<StaticConnection>> irmap = new HashMap<IIRNode, Set<StaticConnection>>();
//		irmap.put(this, IRGeneratorForOneProject.GetInstance().GetOutConnection(this));
//		return irmap;
//	}
//
//	@Override
//	public Map<IIRNode, Set<StaticConnection>> PrepareInNodes() {
//		Map<IIRNode, Set<StaticConnection>> irmap = new HashMap<IIRNode, Set<StaticConnection>>();
//		irmap.put(this, IRGeneratorForOneProject.GetInstance().GetInConnection(this));
//		return irmap;
//	}
	
}
