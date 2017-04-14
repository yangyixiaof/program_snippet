package cn.yyx.research.program.ir.generator;

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
import cn.yyx.research.program.ir.element.ConstantUniqueElement;
import cn.yyx.research.program.ir.element.UnresolvedLambdaUniqueElement;
import cn.yyx.research.program.ir.element.UnresolvedTypeElement;
import cn.yyx.research.program.ir.storage.highlevel.IRForOneClass;
import cn.yyx.research.program.ir.storage.highlevel.IRForOneMethod;

public class IRGeneratorForOneProject {
	
	private IJavaProject java_project = null;
	
	private Map<IType, IRForOneClass> class_irs = new HashMap<IType, IRForOneClass>();
	private Map<IMethod, IRForOneMethod> method_irs = new HashMap<IMethod, IRForOneMethod>();
	
	private static IRGeneratorForOneProject irgfop = null;
	
	private IRGeneratorForOneProject(IJavaProject java_project) {
		this.java_project = java_project;
	}
	
	public static void Initial(IJavaProject java_project)
	{
		if (irgfop != null)
		{
			irgfop.Clear();
		}
		ConstantUniqueElement.Clear();
		UnresolvedLambdaUniqueElement.Clear();
		UnresolvedTypeElement.Clear();
		irgfop = null;
		System.gc();
		try {
			System.out.println("================= Prepare Analysis Resources =================");
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		irgfop = new IRGeneratorForOneProject(java_project);
	}
	
	private void Clear() {
		class_irs.clear();
		method_irs.clear();
		java_project = null;
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
	
//	private void SelfAddToMethodIR(IMethod it, IRForOneMethod irfocbu)
//	{
//		method_irs.put(it, irfocbu);
//	}
//	
//	private void SelfAddToClassIR(IType im, IRForOneClass irfoc)
//	{
//		class_irs.put(im, irfoc);
//	}
//	
//	public static void AddToMethodIR(IMethod it, IRForOneMethod irfocbu)
//	{
//		irgfop.SelfAddToMethodIR(it, irfocbu);
//	}
//	
//	public static void AddToClassIR(IType im, IRForOneClass irfoc)
//	{
//		irgfop.SelfAddToClassIR(im, irfoc);
//	}
	
	public static IRForOneClass FetchITypeIR(IType im)
	{
		return irgfop.SelfFetchITypeIR(im);
	}
	
	public static IRForOneMethod FetchIMethodIR(IMethod im)
	{
		return irgfop.SelfFetchIMethodIR(im);
	}
	
	public IRForOneClass SelfFetchITypeIR(IType it)
	{
		IRForOneClass irclass = class_irs.get(it);
		if (irclass == null)
		{
			irclass = new IRForOneClass(it);
			class_irs.put(it, irclass);
		}
		return irclass;
	}
	
	public IRForOneMethod SelfFetchIMethodIR(IMethod im)
	{
		IRForOneMethod irmethod = method_irs.get(im);
		if (irmethod == null)
		{
			irmethod = new IRForOneMethod(im);
			method_irs.put(im, irmethod);
		}
		return irmethod;
	}
	
}
