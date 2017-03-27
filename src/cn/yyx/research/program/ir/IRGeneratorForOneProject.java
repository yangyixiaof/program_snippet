package cn.yyx.research.program.ir;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;

import cn.yyx.research.program.ir.storage.IRForOneClass;

public class IRGeneratorForOneProject {
	
	private Set<IMethod> visited_methods = new HashSet<IMethod>();
	private Map<String, IRForOneClass> class_irs = new HashMap<String, IRForOneClass>();
	
	public IRGeneratorForOneProject() {
	}
	
	
	
}
