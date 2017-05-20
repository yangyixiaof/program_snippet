package cn.yyx.research.program.ir.visual.dot.generation;

import java.util.Iterator;
import java.util.List;

import cn.yyx.research.program.ir.orgranization.IRTreeForOneControlElement;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;

public class GenerateDotForEachIRCodeInIRProject {
	
	List<IRCode> ircodes = null;
	
	public GenerateDotForEachIRCodeInIRProject(List<IRCode> ircodes, String dot_generation_dir) {
		this.ircodes = ircodes;
	}
	
	public void GenerateDots() {
		Iterator<IRCode> iitr = ircodes.iterator();
		while (iitr.hasNext()) {
			IRCode irc = iitr.next();
			IRTreeForOneControlElement control_ir = irc.GetControlLogicHolderElementIR();
			
		}
	}
	
}
