package cn.yyx.research.program.ir;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;

import cn.yyx.research.program.eclipse.searchutil.JavaSearch;
import cn.yyx.research.program.ir.ast.ASTSearch;
import cn.yyx.research.program.ir.search.IRSearchRequestor;
import cn.yyx.research.program.ir.storage.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.lowlevel.IRForOneMethodInvocation;
import cn.yyx.research.program.ir.storage.lowlevel.IRForOneOperation;

public class IRGeneratorHelper {

	// can only be invoked in end_visit_method_invocation.
	public static void HandleMethodInvocation(IRCode irc, List<Expression> nlist, IMethodBinding imb,
			Expression expr, String identifier, ASTNode node,
			HashMap<IMember, HashMap<ASTNode, Integer>> temp_statement_instr_order_set,
			HashMap<IMember, Integer> temp_statement_environment_set) {

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
					Set<IMember> ims = temp_statement_instr_order_set.keySet();
					Iterator<IMember> iitr = ims.iterator();
					while (iitr.hasNext()) {
						IMember im = iitr.next();
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
							while (aitr.hasNext())
							{
								ASTNode ast = aitr.next();
								if (ASTSearch.ASTNodeContainsAnASTNode(nexpr, ast))
								{
									Integer aidx = ast_order.get(ast);
									if (aidx != null)
									{
										if (max < aidx)
										{
											max = aidx;
										}
									}
								}
							}
							if (max >= 0)
							{
								para_order_instr_index_map.put(idx, max);
								IRForOneMethodInvocation irfoe = new IRForOneMethodInvocation(im, methods, para_order_instr_index_map);
								irc.AddOneIRUnit(im, irfoe);
							}
						}
					}
				}
			}
		} else {
			IRGeneratorHelper.GenerateGeneralIR(node, node, irc, temp_statement_environment_set,
					IRMeta.MethodInvocation + identifier);
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
	//
	// public static void GenerateNoVariableBindingIR(ASTNode node, ASTNode
	// exact_node, IRCode irfom,
	// HashSet<IBinding> bind_set, String code) {
	// Set<IBinding> temp_bindings = bind_set;
	// Iterator<IBinding> titr = temp_bindings.iterator();
	// while (titr.hasNext()) {
	// IBinding ib = titr.next();
	// if (ASTSearch.ASTNodeContainsABinding(node, ib)) {
	// // int start = exact_node.getStartPosition();
	// // int end = start + exact_node.getLength() - 1;
	// // IRInstrKind ir_kind = IRInstrKind.ComputeKind(1);
	// irfom.AddOneIRUnit(ib, new IRForOneOperation(irfom.getIm(), code));
	// }
	// }
	//
	// }

	public static void GenerateGeneralIR(ASTNode node, ASTNode exact_node, IRCode irfom,
			Map<IMember, Integer> temp_statement_set, String code) {
		Set<IMember> temp_bindings = temp_statement_set.keySet();
		Iterator<IMember> titr = temp_bindings.iterator();
		while (titr.hasNext()) {
			IMember im = titr.next();
			if (ASTSearch.ASTNodeContainsAMember(node, im)) {
				Integer count = temp_statement_set.get(im);
				if (count != null && count >= 0) {
					count++;
					if (count > IRGeneratorForOneLogicBlock.GetMaxLevel()) {
						count = -1;
					} else {
						// int start = exact_node.getStartPosition();
						// int end = start + exact_node.getLength() - 1;
						// IRInstrKind ir_kind = IRInstrKind.ComputeKind(count);
						irfom.AddOneIRUnit(im, new IRForOneOperation(im, code));
					}
					temp_statement_set.put(im, count);
				}
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
