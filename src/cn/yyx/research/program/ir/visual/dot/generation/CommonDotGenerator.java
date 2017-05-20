package cn.yyx.research.program.ir.visual.dot.generation;

import java.util.Set;

import cn.yyx.research.program.ir.visual.node.IVNode;
import cn.yyx.research.program.ir.visual.node.container.IVNodeContainer;

public class CommonDotGenerator {
	
	Set<IVNode> pc = null;
	IVNodeContainer ivc = null;
	
	public CommonDotGenerator(Set<IVNode> pc, IVNodeContainer ivc) {
		this.pc = pc;
		this.ivc = ivc;
	}
	
	public void GenerateDot(String dot_file) {
		
	}
	
}
