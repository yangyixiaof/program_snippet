package cn.yyx.research.program.ir.visual.dot.generation;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.ir.generation.IRGeneratorForOneProject;
import cn.yyx.research.program.ir.orgranization.IRTreeForOneControlElement;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.visual.node.IVNode;

public class GenerateDotForEachIRCodeInIRProject {
	
	String dot_generation_dir = null;
	
	public GenerateDotForEachIRCodeInIRProject(String dot_generation_dir) {
		this.dot_generation_dir = dot_generation_dir;
		File dir = new File(dot_generation_dir);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}
	
	public void GenerateDots() {
		int idx = 0;
		List<IRCode> ircodes = IRGeneratorForOneProject.GetInstance().GetAllIRCodes();
		Iterator<IRCode> iitr = ircodes.iterator();
		while (iitr.hasNext()) {
			idx++;
			IRCode irc = iitr.next();
			IRTreeForOneControlElement control_ir = irc.GetControlLogicHolderElementIR();
			HashSet<IVNode> pc = new HashSet<IVNode>();
			pc.add(control_ir.GetRoot());
			Set<IJavaElement> eles = irc.GetAllElements();
			Iterator<IJavaElement> eitr = eles.iterator();
			while (eitr.hasNext()) {
				IJavaElement ije = eitr.next();
				pc.add(irc.GetFirstIRTreeNode(ije));
			}
			CommonDotGenerator cdg = new CommonDotGenerator(pc, IRGeneratorForOneProject.GetInstance());
			cdg.GenerateDot(dot_generation_dir + "/" + "IRCode" + idx + ".dot");
		}
	}
	
}
