package cn.yyx.research.program.ir;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

import cn.yyx.research.program.ir.storage.highlevel.IRForOneClass;
import cn.yyx.research.program.ir.storage.highlevel.IRForOneCloseBlockUnit;

public class IRGeneratorForOneProject {
	
	private IJavaProject java_project = null;
	
	private Map<IType, IRForOneClass> class_irs = new HashMap<IType, IRForOneClass>();
	private Map<IMethod, IRForOneCloseBlockUnit> method_irs = new HashMap<IMethod, IRForOneCloseBlockUnit>();
	
	private static IRGeneratorForOneProject irgfop = null;
	
	private IRGeneratorForOneProject(IJavaProject java_project) {
		this.java_project = java_project;
	}
	
	public static void Initial(IJavaProject java_project)
	{
		irgfop = new IRGeneratorForOneProject(java_project);
	}
	
	public static void GenerateForAll()
	{
		// TODO
		
	}
	
	private void SelfAddToMethodIR(IMethod it, IRForOneCloseBlockUnit irfocbu)
	{
		method_irs.put(it, irfocbu);
	}
	
	private void SelfAddToClassIR(IType im, IRForOneClass irfoc)
	{
		class_irs.put(im, irfoc);
	}
	
	public static void AddToMethodIR(IMethod it, IRForOneCloseBlockUnit irfocbu)
	{
		irgfop.SelfAddToMethodIR(it, irfocbu);
	}
	
	public static void AddToClassIR(IType im, IRForOneClass irfoc)
	{
		irgfop.SelfAddToClassIR(im, irfoc);
	}
	
}
