package cn.yyx.research.program.ir.storage.lowlevel;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IMember;

public class IRForOneJavaInstruction extends IRForOneUnit {
	
	private Set<IRForOneJavaInstruction> parents = new HashSet<IRForOneJavaInstruction>();
	private Set<IRForOneJavaInstruction> children = new HashSet<IRForOneJavaInstruction>();
	
	public IRForOneJavaInstruction(IMember im) {
		super(im);
	}
	
	public void AddParent(IRForOneJavaInstruction parent)
	{
		parents.add(parent);
		parent.AddChild(this);
	}
	
	private void AddChild(IRForOneJavaInstruction child)
	{
		children.add(child);
	}
	
}
