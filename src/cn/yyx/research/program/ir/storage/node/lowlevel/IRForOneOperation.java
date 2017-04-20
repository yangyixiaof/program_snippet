package cn.yyx.research.program.ir.storage.node.lowlevel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.ir.storage.node.IIRNode;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnection;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;

public class IRForOneOperation extends IRForOneInstruction {
	
	private String ir = null;
	
	public IRForOneOperation(IRCode parent_env, IJavaElement im, String ir) {
		super(im, parent_env);
		this.setIr(ir);
	}

	public String getIr() {
		return ir;
	}

	public void setIr(String ir) {
		this.ir = ir;
	}

	@Override
	public Map<IIRNode, Set<StaticConnection>> PrepareOutNodes() {
		Map<IIRNode, Set<StaticConnection>> irmap = new HashMap<IIRNode, Set<StaticConnection>>();
		irmap.put(this, parent_env.GetOutConnects(this));
		return irmap;
	}

	@Override
	public Map<IIRNode, Set<StaticConnection>> PrepareInNodes() {
		Map<IIRNode, Set<StaticConnection>> irmap = new HashMap<IIRNode, Set<StaticConnection>>();
		irmap.put(this, parent_env.GetInConnects(this));
		return irmap;
	}
	
}
