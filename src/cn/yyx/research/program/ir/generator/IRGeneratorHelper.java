package cn.yyx.research.program.ir.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;

import cn.yyx.research.program.eclipse.searchutil.JavaSearch;
import cn.yyx.research.program.ir.IRMeta;
import cn.yyx.research.program.ir.ast.ASTSearch;
import cn.yyx.research.program.ir.search.IRSearchRequestor;
import cn.yyx.research.program.ir.storage.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.lowlevel.IRForOneJavaInstruction;
import cn.yyx.research.program.ir.storage.lowlevel.IRForOneMethodInvocation;
import cn.yyx.research.program.ir.storage.lowlevel.IRForOneOperation;

public class IRGeneratorHelper {

	// can only be invoked in end_visit_method_invocation.
	public static void GenerateMethodInvocationIR(IRCode irc, List<Expression> nlist, IMethodBinding imb,
			Expression expr, String identifier, ASTNode node,
			HashMap<IJavaElement, HashMap<ASTNode, Integer>> temp_statement_instr_order_set,
			Set<IJavaElement> temp_statement_environment_set,
			Map<IJavaElement, Integer> all_count,
			HashMap<IJavaElement, Integer> branch_dependency) {

		if (imb != null && imb.getDeclaringClass() != null && imb.getDeclaringClass().isFromSource()) {
			// source method invocation.
			IJavaElement jele = imb.getJavaElement();
			if (jele != null && jele instanceof IMethod) {
				Collection<IMethod> methods = null;
				IMethod imethod = (IMethod) jele;
				try {
					IRSearchRequestor sr = new IRSearchRequestor();
					JavaSearch.SearchForWhereTheMethodIsInvoked(imethod, true, sr);
					methods = sr.GetMethods();
				} catch (CoreException e) {
					e.printStackTrace();
				}
				if (methods != null && methods.size() > 0) {
					Set<IJavaElement> ims = temp_statement_instr_order_set.keySet();
					Iterator<IJavaElement> iitr = ims.iterator();
					while (iitr.hasNext()) {
						IJavaElement im = iitr.next();
						Map<Integer, Integer> para_order_instr_index_map = new HashMap<Integer, Integer>();
						HashMap<ASTNode, Integer> ast_order = temp_statement_instr_order_set.get(im);
						int idx = 0;
						Iterator<Expression> nitr = nlist.iterator();
						while (nitr.hasNext()) {
							idx++;
							Expression nexpr = nitr.next();
							int max = -1;
							Set<ASTNode> astkeys = ast_order.keySet();
							Iterator<ASTNode> aitr = astkeys.iterator();
							while (aitr.hasNext()) {
								ASTNode ast = aitr.next();
								if (ASTSearch.ASTNodeContainsAnASTNode(nexpr, ast)) {
									Integer aidx = ast_order.get(ast);
									if (aidx != null) {
										if (max < aidx) {
											max = aidx;
										}
									}
								}
							}
							if (max >= 0) {
								para_order_instr_index_map.put(idx, max);
							}
						}
						IRForOneMethodInvocation now = new IRForOneMethodInvocation(im, methods,
								para_order_instr_index_map);

						HandleNodeDependency(irc, im, now, branch_dependency);

						irc.AddOneIRUnit(im, now);
					}
				}
			}
		} else {
			IRGeneratorHelper.GenerateGeneralIR(irc, node, temp_statement_environment_set, all_count,
					IRMeta.MethodInvocation + identifier, branch_dependency);
		}

		// // initialize parameters of the node.
		// Map<IBinding, Integer> invoke_parameter_order = new HashMap<IBinding,
		// Integer>();
		// // @SuppressWarnings("unchecked")
		// // List<Expression> nlist = node.arguments();
		// Iterator<Expression> itr = nlist.iterator();
		// int idx = 0;
		// while (itr.hasNext()) {
		// idx++;
		// Expression e = itr.next();
		// if (e instanceof Name) {
		// Name n = (Name) e;
		// IBinding nbind = n.resolveBinding();
		// if (nbind != null && nbind instanceof ITypeBinding && nbind
		// instanceof IVariableBinding) {
		// invoke_parameter_order.put(nbind, idx);
		// }
		// }
		// }
		//
		// IBinding ib = null;
		// // IMethodBinding imb = node.resolveMethodBinding();
		// // Expression expr = node.getExpression();
		// String code = identifier.toString();
		// if (expr == null) {
		// // set to meta--invoke other user defined function.
		// code = IRMeta.User_Defined_Function;
		// } else {
		// // set data_dependency in IRForOneMethod.
		// if (expr instanceof Name) {
		// Name n = (Name) expr;
		// ib = n.resolveBinding();
		// if (ib instanceof ITypeBinding || ib instanceof IVariableBinding) {
		// irc.AddDataDependency(ib, invoke_parameter_order.keySet());
		// }
		// }
		// }
		//
		// if (imb == null || !imb.getDeclaringClass().isFromSource()) {
		// // null or is from binary.
		//
		// } else {
		// // is from source.
		// // need to be handled specifically.
		// if (ib != null) {
		// code = IRMeta.User_Defined_Function;
		// IRGeneratorHelper.GenerateSourceMethodInvocationIR(ib, imb, node,
		// node, irc, invoke_parameter_order,
		// IRMeta.MethodInvocation + code);
		// }
		// }
	}

	// public static void GenerateSwitchCaseIR(ASTNode node, ASTNode sc, IRCode
	// irfom, HashSet<IBinding> binds)
	// {
	// String code = IRMeta.Switch_Case_Default;
	// if (!sc.toString().startsWith("default")) {
	// SwitchCase r_sc = (SwitchCase)sc;
	// code = IRMeta.Switch_Case_Relation + r_sc.getExpression().toString();
	// }
	// IRGeneratorHelper.GenerateNoVariableBindingIR(node.getParent(), node,
	// irfom, binds, code);
	// }

	public static void GenerateNoVariableBindingIR(ASTNode node, ASTNode exact_node, IRCode irc,
			Set<IJavaElement> member_set, String code, HashMap<IJavaElement, Integer> branch_dependency) {
		Set<IJavaElement> temp_bindings = member_set;
		Iterator<IJavaElement> titr = temp_bindings.iterator();
		while (titr.hasNext()) {
			IJavaElement im = titr.next();
			if (ASTSearch.ASTNodeContainsAMember(node, im)) {
				// int start = exact_node.getStartPosition();
				// int end = start + exact_node.getLength() - 1;
				// IRInstrKind ir_kind = IRInstrKind.ComputeKind(1);
				IRForOneOperation now = new IRForOneOperation(im, code);
				irc.AddOneIRUnit(im, now);
				HandleNodeDependency(irc, im, now, branch_dependency);
			}
		}
	}

	public static void GenerateGeneralIR(IRCode irc, ASTNode node, 
			Set<IJavaElement> temp_statement_set, Map<IJavaElement, Integer> all_count, String code, HashMap<IJavaElement, Integer> branch_dependency) {
		Set<IJavaElement> concern = new HashSet<IJavaElement>(temp_statement_set);
		Iterator<IJavaElement> oitr = temp_statement_set.iterator();
		while (oitr.hasNext())
		{
			IJavaElement ije = oitr.next();
			Set<IJavaElement> dep = irc.GetAssignDependency(ije);
			if (dep != null)
			{
				concern.addAll(dep);
			}
		}
		Iterator<IJavaElement> titr = concern.iterator();
		if (!titr.hasNext())
		{
			// ConstantUniqueElement.FetchConstantElement(code);
			// do nothing.
		}
		while (titr.hasNext()) {
			IJavaElement im = titr.next();
			if (ASTSearch.ASTNodeContainsAMember(node, im)) {
				Integer count = all_count.get(im);
				if (count != null && count >= 0) {
					count++;
					if (count > IRGeneratorForOneLogicBlock.GetMaxLevel()) {
						count = -1;
					} else {
						// int start = exact_node.getStartPosition();
						// int end = start + exact_node.getLength() - 1;
						// IRInstrKind ir_kind = IRInstrKind.ComputeKind(count);
						IRForOneOperation now = new IRForOneOperation(im, code);

						HandleNodeDependency(irc, im, now, branch_dependency);

						irc.AddOneIRUnit(im, now);
					}
					all_count.put(im, count);
				}
			}
		}

	}

	private static void HandleNodeDependency(IRCode irc, IJavaElement im, IRForOneJavaInstruction now,
			HashMap<IJavaElement, Integer> branch_dependency) {
		IRForOneJavaInstruction last_instr = irc.GetLastIRUnit(im);
		now.AddParent(last_instr);
		Set<IJavaElement> bkeys = branch_dependency.keySet();
		Iterator<IJavaElement> bitr = bkeys.iterator();
		while (bitr.hasNext()) {
			IJavaElement bim = bitr.next();
			Integer idx = branch_dependency.get(bim);
			IRForOneJavaInstruction pt = irc.GetIRUnitByIndex(bim, idx);
			if (pt != null) {
				now.AddParent(pt);
			}
		}
	}

	// private static void GenerateSourceMethodInvocationIR(IBinding ib,
	// IMethodBinding imb, ASTNode node,
	// ASTNode exact_node, IRForOneMethod irfom, Map<IBinding, Integer>
	// temp_statement_set, String code) {
	// int start = exact_node.getStartPosition();
	// int end = start + exact_node.getLength() - 1;
	// Integer count = temp_statement_set.get(ib);
	// count++;
	// IRInstrKind ir_kind = IRInstrKind.ComputeKind(count);
	// temp_statement_set.put(ib, count);
	// IJavaElement jele = imb.getJavaElement();
	// Collection<IRForOneMethod> methods = null;
	// if (jele != null && jele instanceof IMethod) {
	// IMethod imethod = (IMethod) jele;
	// try {
	// IRSearchRequestor sr = new IRSearchRequestor();
	// JavaSearch.SearchForWhereTheMethodIsInvoked(imethod, true, sr);
	// methods = sr.GetMethods();
	// } catch (CoreException e) {
	// e.printStackTrace();
	// }
	// }
	// if (methods != null && methods.size() > 0) {
	// IRForOneMethodInvocation irfoe = new
	// IRForOneMethodInvocation(irfom.getIm(), start, end, methods, ir_kind);
	// irfom.AddOneIRUnit(ib, irfoe);
	// }
	// }

}
