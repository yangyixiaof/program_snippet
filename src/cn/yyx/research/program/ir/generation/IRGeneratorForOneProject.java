package cn.yyx.research.program.ir.generation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;

import cn.yyx.research.program.eclipse.jdtutil.JDTParser;
import cn.yyx.research.program.eclipse.searchutil.JavaSearch;
import cn.yyx.research.program.ir.element.ConstantUniqueElement;
import cn.yyx.research.program.ir.element.UnresolvedLambdaUniqueElement;
import cn.yyx.research.program.ir.element.UnresolvedTypeElement;
import cn.yyx.research.program.ir.orgranization.IRTreeNode;
import cn.yyx.research.program.ir.storage.node.IIRNode;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnection;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneClass;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneMethod;

public class IRGeneratorForOneProject {
	
	private IJavaProject java_project = null;
	
	private HashMap<IType, IRForOneClass> class_irs = new HashMap<IType, IRForOneClass>();
	private HashMap<IMethod, IRForOneMethod> method_irs = new HashMap<IMethod, IRForOneMethod>();
	
	private Map<String, UnresolvedTypeElement> unresolved_type_element_cache = new TreeMap<String, UnresolvedTypeElement>();
	private Map<String, UnresolvedLambdaUniqueElement> unresolved_lambda_unique_element_cache = new TreeMap<String, UnresolvedLambdaUniqueElement>();
	private Map<String, ConstantUniqueElement> constant_unique_element_cache = new TreeMap<String, ConstantUniqueElement>();
	
	private Map<IIRNode, Map<IIRNode, StaticConnection>> in_connects = new HashMap<IIRNode, Map<IIRNode, StaticConnection>>();
	private Map<IIRNode, Map<IIRNode, StaticConnection>> out_connects = new HashMap<IIRNode, Map<IIRNode, StaticConnection>>();
	
	public StaticConnection GetSpecifiedConnection(IIRNode source, IIRNode target) {
		Map<IIRNode, StaticConnection> ocnnts = out_connects.get(source);
		if (ocnnts == null) {
			return null;
		}
		StaticConnection conn = ocnnts.get(target);
		return conn;
	}
	
	public Set<StaticConnection> GetOutConnection(IIRNode iirn)
	{
		HashSet<StaticConnection> result = new HashSet<StaticConnection>();
		Map<IIRNode, StaticConnection> ios = out_connects.get(iirn);
		if (ios != null)
		{
			result.addAll(ios.values());
		}
		return result;
	}
	
	public Set<StaticConnection> GetInConnection(IIRNode iirn)
	{
		HashSet<StaticConnection> result = new HashSet<StaticConnection>();
		Map<IIRNode, StaticConnection> iis = in_connects.get(iirn);
		if (iis != null)
		{
			result.addAll(iis.values());
		}
		return result;
	}
	
	private void OneDirectionRegist(StaticConnection conn, IIRNode source, IIRNode target, Map<IIRNode, Map<IIRNode, StaticConnection>> in_connects)
	{
		Map<IIRNode, StaticConnection> ins = in_connects.get(target);
		if (ins == null)
		{
			ins = new TreeMap<IIRNode, StaticConnection>();
			in_connects.put(target, ins);
		}
		StaticConnection origin_conn = ins.get(source);
		StaticConnection new_conn = conn;
		if (origin_conn != null)
		{
			new_conn = new_conn.MergeStaticConnection(origin_conn);
		}
		ins.put(source, new_conn);
	}
	
	public void RegistConnection(StaticConnection conn)
	{
		IIRNode source = conn.getSource();
		IIRNode target = conn.getTarget();
		OneDirectionRegist(conn, source, target, in_connects);
		OneDirectionRegist(conn, target, source, out_connects);
	}
	
	// Solved. source type is dependent on unresolved operations, how to model that dependency?
	
	public ConstantUniqueElement FetchConstantUniqueElement(String represent)
	{
		ConstantUniqueElement yce = constant_unique_element_cache.get(represent);
		if (yce == null) {
			yce = new ConstantUniqueElement(represent);
			constant_unique_element_cache.put(represent, yce);
		}
		return yce;
	}
	
	public UnresolvedLambdaUniqueElement FetchUnresolvedLambdaUniqueElement(String represent, IMember parent_im, Map<IJavaElement, IRTreeNode> env)
	{
		UnresolvedLambdaUniqueElement yce = unresolved_lambda_unique_element_cache.get(represent);
		if (yce == null) {
			yce = new UnresolvedLambdaUniqueElement(represent, parent_im, env);
			unresolved_lambda_unique_element_cache.put(represent, yce);
		}
		return yce;
	}
	
	public UnresolvedTypeElement FetchUnresolvedTypeElement(String represent)
	{
		UnresolvedTypeElement yce = unresolved_type_element_cache.get(represent);
		if (yce == null) {
			yce = new UnresolvedTypeElement(represent);
			unresolved_type_element_cache.put(represent, yce);
		}
		return yce;
	}
	
	private static IRGeneratorForOneProject irgfop = null;
	
	public static IRGeneratorForOneProject GetInstance()
	{
		return irgfop;
	}
	
	private IRGeneratorForOneProject(IJavaProject java_project) {
		this.java_project = java_project;
	}
	
	public static void Initial(IJavaProject java_project)
	{
		if (irgfop != null)
		{
			irgfop.Clear();
		}
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
		unresolved_type_element_cache.clear();
		unresolved_lambda_unique_element_cache.clear();
		constant_unique_element_cache.clear();
		class_irs.clear();
		method_irs.clear();
		java_project = null;
	}
	
	private void GenerateForAllICompilationUnits() throws JavaModelException
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
	
	public IRForOneClass FetchITypeIR(IType it)
	{
		IRForOneClass irclass = class_irs.get(it);
		if (irclass == null)
		{
			irclass = new IRForOneClass(it);
			class_irs.put(it, irclass);
		}
		return irclass;
	}
	
	public IRForOneMethod FetchIMethodIR(IMethod im)
	{
		IRForOneMethod irmethod = method_irs.get(im);
		if (irmethod == null)
		{
			irmethod = new IRForOneMethod(im);
			method_irs.put(im, irmethod);
		}
		return irmethod;
	}
	
	public Map<IType, IRForOneClass> GetClassIR()
	{
		return class_irs;
	}
	
	public Map<IMethod, IRForOneMethod> GetMethodIR()
	{
		return method_irs;
	}
	
}
