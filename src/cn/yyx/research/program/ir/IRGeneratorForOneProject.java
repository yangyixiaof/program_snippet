package cn.yyx.research.program.ir;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;

import cn.yyx.research.program.eclipse.jdtutil.JDTParser;
import cn.yyx.research.program.eclipse.searchutil.JavaSearch;
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
	
	public static void GenerateForAllICompilationUnits() throws JavaModelException
	{
		irgfop.SelfGenerateForAllICompilationUnits();
	}
	
	private void SelfGenerateForAllICompilationUnits() throws JavaModelException
	{
		List<ICompilationUnit> units = JavaSearch.SearchForAllICompilationUnits(java_project);
		// System.err.println("unit_size:" + units.size());
		for (final ICompilationUnit icu : units) {
			CompilationUnit cu = JDTParser.CreateJDTParser(java_project).ParseICompilationUnit(icu);
			IRGeneratorForClassesInICompilationUnit irgfcicu = new IRGeneratorForClassesInICompilationUnit();
			cu.accept(irgfcicu);
		}
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
	
	public static void AddITypeIR(IType im, IRForOneClass irfoc)
	{
		irgfop.SelfAddToClassIR(im, irfoc);
	}
	
	public static void AddIMethodIR(IMethod im, IRForOneCloseBlockUnit irfoc)
	{
		irgfop.SelfAddIMethodIR(im, irfoc);
	}
	
	public void SelfAddITypeIR(IType it, IRForOneClass irfoc)
	{
		class_irs.put(it, irfoc);
	}
	
	public void SelfAddIMethodIR(IMethod im, IRForOneCloseBlockUnit irfocbu)
	{
		method_irs.put(im, irfocbu);
	}
	
}
