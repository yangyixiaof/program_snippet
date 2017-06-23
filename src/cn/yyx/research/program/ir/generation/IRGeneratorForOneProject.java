package cn.yyx.research.program.ir.generation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;

import cn.yyx.research.program.eclipse.jdtutil.JDTParser;
import cn.yyx.research.program.eclipse.searchutil.EclipseSearchForICompilationUnits;
import cn.yyx.research.program.ir.element.ConstantUniqueElement;
import cn.yyx.research.program.ir.element.UnresolvedLambdaUniqueElement;
import cn.yyx.research.program.ir.element.UnresolvedNameOrFieldAccessElement;
import cn.yyx.research.program.ir.element.UnresolvedTypeElement;
import cn.yyx.research.program.ir.exception.NotCastConnectionDetailException;
import cn.yyx.research.program.ir.orgranization.IRTreeForOneControlElement;
import cn.yyx.research.program.ir.storage.connection.ConnectionInfo;
import cn.yyx.research.program.ir.storage.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.connection.EdgeTypeUtil;
import cn.yyx.research.program.ir.storage.connection.JudgeType;
import cn.yyx.research.program.ir.storage.connection.StaticConnection;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneClass;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneConstructor;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneField;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneMethod;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneBranchControl;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;
import cn.yyx.research.program.ir.visual.node.IVNode;
import cn.yyx.research.program.ir.visual.node.connection.IVConnection;
import cn.yyx.research.program.ir.visual.node.container.IVNodeContainer;

public class IRGeneratorForOneProject implements IVNodeContainer {
	// Solved. two things: first, mark whether a method is constructor and its
	// IType. second, test caller-roots method and JavaSearch Engine.
	// Solved. remember to check if searched method are null, if null, what to
	// handle?
	private IJavaProject java_project = null;

	private HashMap<IType, IRForOneClass> class_irs = new HashMap<IType, IRForOneClass>();
	private HashMap<IMethod, IRForOneMethod> method_irs = new HashMap<IMethod, IRForOneMethod>();

	private Map<String, UnresolvedTypeElement> unresolved_type_element_cache = new TreeMap<String, UnresolvedTypeElement>();
	private Map<String, UnresolvedNameOrFieldAccessElement> unresolved_name_or_field_access_element_cache = new TreeMap<String, UnresolvedNameOrFieldAccessElement>();
	private Map<String, UnresolvedLambdaUniqueElement> unresolved_lambda_unique_element_cache = new TreeMap<String, UnresolvedLambdaUniqueElement>();
	private Map<String, ConstantUniqueElement> constant_unique_element_cache = new TreeMap<String, ConstantUniqueElement>();

	private Map<IRForOneInstruction, Map<IRForOneInstruction, StaticConnection>> in_connects = new HashMap<IRForOneInstruction, Map<IRForOneInstruction, StaticConnection>>();
	private Map<IRForOneInstruction, Map<IRForOneInstruction, StaticConnection>> out_connects = new HashMap<IRForOneInstruction, Map<IRForOneInstruction, StaticConnection>>();

	private Map<IMethod, Set<IMethod>> callee_callers = new HashMap<IMethod, Set<IMethod>>();

	private Map<IRForOneBranchControl, Set<IRForOneBranchControl>> children_of_control = new HashMap<IRForOneBranchControl, Set<IRForOneBranchControl>>();

	private static IRGeneratorForOneProject irgfop = null;
	
	public void AddCalleeCaller(IMethod callee, IMethod caller) {
		Set<IMethod> callers = callee_callers.get(callee);
		if (callers == null) {
			callers = new HashSet<IMethod>();
			callee_callers.put(callee, callers);
		}
		if (caller != null) {
			callers.add(caller);
		}
	}

	public Map<IMethod, Set<IMethod>> GetInverseCallGraph() {
		return callee_callers;
	}

	public StaticConnection GetSpecifiedConnection(IRForOneInstruction source, IRForOneInstruction target) {
		Map<IRForOneInstruction, StaticConnection> ocnnts = out_connects.get(source);
		if (ocnnts == null) {
			return null;
		}
		StaticConnection conn = ocnnts.get(target);
		return conn;
	}

	private Set<IRForOneInstruction> GetINodesByJudgeType(
			Map<IRForOneInstruction, Map<IRForOneInstruction, StaticConnection>> connects, IRForOneInstruction iirn,
			JudgeType jt, int judged_type) {
		HashSet<IRForOneInstruction> result = new HashSet<IRForOneInstruction>();
		Map<IRForOneInstruction, StaticConnection> is = connects.get(iirn);
		if (is != null) {
			Set<IRForOneInstruction> ikeys = is.keySet();
			Iterator<IRForOneInstruction> iitr = ikeys.iterator();
			while (iitr.hasNext()) {
				IRForOneInstruction iir = iitr.next();
				StaticConnection sc = is.get(iir);
				if (jt.MeetCondition(sc.getInfo().getType(), judged_type)) {
					result.add(iir);
				}
			}
		}
		return result;
	}

	private Set<StaticConnection> GetStaticConnectionsByJudgeType(
			Map<IRForOneInstruction, Map<IRForOneInstruction, StaticConnection>> connects, IRForOneInstruction iirn,
			JudgeType jt, int judged_type) {
		HashSet<StaticConnection> result = new HashSet<StaticConnection>();
		Map<IRForOneInstruction, StaticConnection> is = connects.get(iirn);
		if (is != null) {
			Set<IRForOneInstruction> ikeys = is.keySet();
			Iterator<IRForOneInstruction> iitr = ikeys.iterator();
			while (iitr.hasNext()) {
				IRForOneInstruction iir = iitr.next();
				StaticConnection sc = is.get(iir);
				if (jt.MeetCondition(sc.getInfo().getType(), judged_type)) {
					result.add(sc);
				}
			}
		}
		return result;
	}

	public Set<IRForOneInstruction> GetOutINodes(IRForOneInstruction iirn) {
		return GetINodesByJudgeType(out_connects, iirn, (judge, judged) -> {
			return !EdgeTypeUtil.OnlyHasSpecificType(judge, judged);
		}, EdgeBaseType.SameOperations.Value());
	}

	public Set<IRForOneInstruction> GetInINodes(IRForOneInstruction iirn) {
		return GetINodesByJudgeType(in_connects, iirn, (judge, judged) -> {
			return !EdgeTypeUtil.OnlyHasSpecificType(judge, judged);
		}, EdgeBaseType.SameOperations.Value());
	}

	public Set<IRForOneInstruction> GetOutINodesByContainingSpecificType(IRForOneInstruction iirn, int type) {
		return GetINodesByJudgeType(out_connects, iirn, (judge, judged) -> {
			return EdgeTypeUtil.HasSpecificType(judge, judged);
		}, type);
	}

	public Set<IRForOneInstruction> GetInINodesByContainingSpecificType(IRForOneInstruction iirn, int type) {
		return GetINodesByJudgeType(in_connects, iirn, (judge, judged) -> {
			return EdgeTypeUtil.HasSpecificType(judge, judged);
		}, type);
	}

	public Set<StaticConnection> GetOutConnections(IRForOneInstruction iirn) {
		return GetStaticConnectionsByJudgeType(out_connects, iirn, (judge, judged) -> {
			return !EdgeTypeUtil.OnlyHasSpecificType(judge, judged);
		}, EdgeBaseType.SameOperations.Value());
	}

	public Set<StaticConnection> GetInConnections(IRForOneInstruction iirn) {
		return GetStaticConnectionsByJudgeType(in_connects, iirn, (judge, judged) -> {
			return !EdgeTypeUtil.OnlyHasSpecificType(judge, judged);
		}, EdgeBaseType.SameOperations.Value());
	}
	
	public Set<StaticConnection> GetOutConnectionsByContainingSpecificType(IRForOneInstruction iirn, int type) {
		return GetStaticConnectionsByJudgeType(out_connects, iirn, (judge, judged) -> {
			return EdgeTypeUtil.HasSpecificType(judge, judged);
		}, type);
	}

	public Set<StaticConnection> GetInConnectionsByContainingSpecificType(IRForOneInstruction iirn, int type) {
		return GetStaticConnectionsByJudgeType(in_connects, iirn, (judge, judged) -> {
			return EdgeTypeUtil.HasSpecificType(judge, judged);
		}, type);
	}

	private void OneDirectionRegist(StaticConnection conn, IRForOneInstruction source, IRForOneInstruction target,
			Map<IRForOneInstruction, Map<IRForOneInstruction, StaticConnection>> o_connects) {
		Map<IRForOneInstruction, StaticConnection> outs = o_connects.get(source);
		if (outs == null) {
			outs = new HashMap<IRForOneInstruction, StaticConnection>();
			o_connects.put(source, outs);
		}
		StaticConnection origin_conn = outs.get(target);
		StaticConnection new_conn = conn;
		if (origin_conn != null) {
			try {
				new_conn = new_conn.HorizontalMerge(origin_conn);
			} catch (NotCastConnectionDetailException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		outs.put(target, new_conn);
	}

	public void RegistConnection(StaticConnection conn) {
		IRForOneInstruction source = conn.getSource();
		IRForOneInstruction target = conn.getTarget();
		OneDirectionRegist(conn, target, source, in_connects);
		OneDirectionRegist(conn, source, target, out_connects);
	}

	// Solved. source type is dependent on unresolved operations, how to model
	// that dependency?

	public ConstantUniqueElement FetchConstantUniqueElement(String represent) {
		ConstantUniqueElement yce = constant_unique_element_cache.get(represent);
		if (yce == null) {
			yce = new ConstantUniqueElement(represent);
			constant_unique_element_cache.put(represent, yce);
		}
		return yce;
	}

	public UnresolvedLambdaUniqueElement FetchUnresolvedLambdaUniqueElement(String represent, IMember parent_im) {
		// Map<IJavaElement, IRForOneInstruction> env
		UnresolvedLambdaUniqueElement yce = unresolved_lambda_unique_element_cache.get(represent);
		if (yce == null) {
			yce = new UnresolvedLambdaUniqueElement(represent, parent_im);// , env
			unresolved_lambda_unique_element_cache.put(represent, yce);
		}
		return yce;
	}

	public UnresolvedTypeElement FetchUnresolvedTypeElement(String represent) {
		UnresolvedTypeElement yce = unresolved_type_element_cache.get(represent);
		if (yce == null) {
			yce = new UnresolvedTypeElement(represent);
			unresolved_type_element_cache.put(represent, yce);
		}
		return yce;
	}
	
	public UnresolvedNameOrFieldAccessElement FetchUnresolvedNameOrFieldAccessElement(String represent) {
		UnresolvedNameOrFieldAccessElement yce = unresolved_name_or_field_access_element_cache.get(represent);
		if (yce == null) {
			yce = new UnresolvedNameOrFieldAccessElement(represent);
			unresolved_name_or_field_access_element_cache.put(represent, yce);
		}
		return yce;
	}

	public static IRGeneratorForOneProject GetInstance() {
		return irgfop;
	}

	private IRGeneratorForOneProject(IJavaProject java_project) {
		this.setJava_project(java_project);
	}

	private static void Initial(IJavaProject java_project) {
		if (irgfop != null) {
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
		setJava_project(null);
	}

	public static void GenerateForAllICompilationUnits(IJavaProject java_project) throws JavaModelException {
		Initial(java_project);
		List<ICompilationUnit> units = EclipseSearchForICompilationUnits.SearchForAllICompilationUnits(java_project);
		// System.err.println("unit_size:" + units.size());
		for (final ICompilationUnit icu : units) {
			CompilationUnit cu = JDTParser.CreateJDTParser(java_project).ParseICompilationUnit(icu);
			IRGeneratorForClassesInICompilationUnit irgfcicu = new IRGeneratorForClassesInICompilationUnit();
			cu.accept(irgfcicu);
		}
		GetInstance().HandleToRemoveNoIRsElementForAllIRCode();
		GetInstance().HandleToAddAllChildrenSetForAllControl();
	}

	public IRForOneClass FetchITypeIR(IType it) {
		IRForOneClass irclass = class_irs.get(it);
		if (irclass == null) {
			irclass = new IRForOneClass(it);
			class_irs.put(it, irclass);
		}
		return irclass;
	}

	public IRForOneMethod FetchIConstructorIR(IMethod im, IType it) {
		IRForOneMethod irmethod = method_irs.get(im);
		if (irmethod == null) {
			irmethod = new IRForOneConstructor(im, it);
			method_irs.put(im, irmethod);
		}
		return irmethod;
	}

	public IRForOneMethod FetchIMethodIR(IMethod im) {
		IRForOneMethod irmethod = method_irs.get(im);
		if (irmethod == null) {
			irmethod = new IRForOneMethod(im);
			method_irs.put(im, irmethod);
		}
		return irmethod;
	}

	public Set<IType> GetAllClasses() {
		return class_irs.keySet();
	}

	public Set<IMethod> GetAllMethods() {
		return method_irs.keySet();
	}

	public IRForOneClass GetClassIR(IType itp) {
		return class_irs.get(itp);
	}

	public IRForOneMethod GetMethodIR(IMethod imd) {
		return method_irs.get(imd);
	}

	public IJavaProject getJava_project() {
		return java_project;
	}

	private void setJava_project(IJavaProject java_project) {
		this.java_project = java_project;
	}

	public void HandleToAddAllChildrenSetForAllControl() {
		List<IRCode> ircs = GetAllIRCodes();
		Iterator<IRCode> iitr = ircs.iterator();
		while (iitr.hasNext()) {
			IRCode irc = iitr.next();
			IRTreeForOneControlElement control_ir = irc.GetControlLogicHolderElementIR();
			IRForOneBranchControl control_root = control_ir.GetRoot();
			GetAllChildrenOfControl(control_root);
		}
	}
	
	public void HandleToRemoveNoIRsElementForAllIRCode() {
		List<IRCode> ircs = GetAllIRCodes();
		Iterator<IRCode> iitr = ircs.iterator();
		while (iitr.hasNext()) {
			IRCode irc = iitr.next();
			irc.RemoveNoIRsElement();
		}
	}

	public List<IRCode> GetAllIRCodes() {
		Collection<IRForOneClass> types = class_irs.values();
		Iterator<IRForOneClass> titr = types.iterator();
		List<IRCode> ircs = new LinkedList<IRCode>();
		while (titr.hasNext()) {
			IRForOneClass irfoc = titr.next();
			IRForOneField fl = irfoc.GetFieldLevel();
			if (fl != null) {
				ircs.add(fl);
			}
			ircs.addAll(irfoc.GetMethodLevel());
		}
		return ircs;
	}

	private Set<IRForOneBranchControl> GetAllChildrenOfControl(IRForOneBranchControl control) {
		Set<IRForOneBranchControl> children = new HashSet<IRForOneBranchControl>();
		Set<IRForOneInstruction> outs = GetOutINodesByContainingSpecificType(control,
				EdgeBaseType.BranchControl.Value());
		Iterator<IRForOneInstruction> oitr = outs.iterator();
		while (oitr.hasNext()) {
			IRForOneInstruction out = oitr.next();
			IRForOneBranchControl irfobc = (IRForOneBranchControl) out;
			children.addAll(GetAllChildrenOfControl(irfobc));
		}
		children_of_control.put(control, children);
		return children;
	}

	public Set<IRForOneBranchControl> GetChildrenOfControl(IRForOneBranchControl control) {
		return children_of_control.get(control);
	}

	@Override
	public Set<IVConnection> GetOutConnection(IVNode source) {
		Set<IVConnection> result = new HashSet<IVConnection>();
		Map<IRForOneInstruction, StaticConnection> out_map = out_connects.get(source);
		if (out_map != null) {
			Set<IRForOneInstruction> okeys = out_map.keySet();
			Iterator<IRForOneInstruction> oitr = okeys.iterator();
			while (oitr.hasNext()) {
				IRForOneInstruction irfoi = oitr.next();
				StaticConnection sc = out_map.get(irfoi);
				IVConnection ivc = null;
				try {
					ivc = new IVConnection(source, irfoi, (ConnectionInfo)sc.getInfo().clone());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
					System.exit(1);
				}
				result.add(ivc);
			}
		}
		return result;
	}

}
