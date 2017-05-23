package cn.yyx.research.program.ir.generation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

import cn.yyx.research.program.eclipse.searchutil.EclipseSearchForIMember;
import cn.yyx.research.program.ir.ast.ASTSearch;
import cn.yyx.research.program.ir.search.IRSearchMethodRequestor;
import cn.yyx.research.program.ir.storage.node.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnection;
import cn.yyx.research.program.ir.storage.node.execution.DefaultINodeTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneOperation;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneSourceMethodInvocation;

public class IRGeneratorHelper {
	// necessary. remember to add virtual node to each parameter.
	// Solved. Solution is to add every virtual node when an IRTreeForOneElement
	// is created. Important!!! every parameter needs a virtual node with
	// skip_self_task and require_self flag.
	// Solved. all things need to be checked that the only one self true node needs
	// to add a distinct node.
	// can only be invoked in end_visit_method_invocation.
	public static void GenerateMethodInvocationIR(IRGeneratorForOneLogicBlock irgfob, List<Expression> nlist,
			IMethod parent_im, IMethod im, Expression expr, String identifier, ASTNode node) {

		// if (imb != null && imb.getDeclaringClass() != null &&
		// imb.getDeclaringClass().isFromSource()) {
		//
		// ITypeBinding itb = imb.getReturnType();
		// if (itb.isPrimitive() && itb.getQualifiedName().equals("void"))
		// {
		// //
		// }
		// IJavaElement jele = imb.getJavaElement();
		IRCode irc = irgfob.irc;
		HashMap<ASTNode, Map<IJavaElement, IRForOneInstruction>> temp_statement_instr_order = irgfob.method_parameter_element_instr_order;
		HashMap<ASTNode, Map<IJavaElement, Boolean>> temp_statement_instr_is_self = irgfob.method_parameter_element_instr_is_self;
		// Set<IJavaElement> temp_statement_environment_set =
		// irgfob.temp_statement_environment_set;
		// Map<IJavaElement, Integer> all_count = irgfob.all_count;
		HashMap<IJavaElement, IRForOneInstruction> branch_dependency = irgfob.branchs_var_instr_order.peek();
		IJavaElement source_method_receiver_element = irgfob.source_method_virtual_holder_element;

		if (im != null) {// && jele instanceof IMethod
			// source method invocation.
			Collection<IMethod> methods = null;
			try {
				IRSearchMethodRequestor sr = new IRSearchMethodRequestor(
						IRGeneratorForOneProject.GetInstance().getJava_project(), im);
				EclipseSearchForIMember search = new EclipseSearchForIMember();
				search.SearchForWhereTheMethodIsConcreteImplementated(im, sr);
				methods = sr.GetMethods();
			} catch (CoreException e) {
				e.printStackTrace();
			}
			if (methods != null && methods.size() > 0) {
				Map<IRForOneInstruction, List<Integer>> para_order_instr_index_map = new HashMap<IRForOneInstruction, List<Integer>>();
				if (parent_im != null) {
					Iterator<IMethod> mitr = methods.iterator();
					while (mitr.hasNext()) {
						IMethod callee = mitr.next();
						IRGeneratorForOneProject.GetInstance().AddCalleeCaller(callee, parent_im);
					}
				}
				IRForOneSourceMethodInvocation now = new IRForOneSourceMethodInvocation(im.getElementName(), irc, source_method_receiver_element,
						methods, DefaultINodeTask.class, para_order_instr_index_map);
				Iterator<Expression> nitr = nlist.iterator();
				int idx = -1;
				while (nitr.hasNext()) {
					idx++;
					Expression nexpr = nitr.next();
					Map<IJavaElement, IRForOneInstruction> jele_order = temp_statement_instr_order.get(nexpr);
					Map<IJavaElement, Boolean> jele_is_self = temp_statement_instr_is_self.get(nexpr);
					Set<IJavaElement> ele_keys = jele_order.keySet();
					Iterator<IJavaElement> eitr = ele_keys.iterator();
					while (eitr.hasNext()) {
						IJavaElement ije = eitr.next();
						IRForOneInstruction source = jele_order.get(ije);
						List<Integer> order_instrs = para_order_instr_index_map.get(source);
						if (order_instrs == null) {
							order_instrs = new LinkedList<Integer>();
							para_order_instr_index_map.put(source, order_instrs);
						}
						order_instrs.add(idx);
						
						boolean is_self = jele_is_self.get(ije);
						if (is_self) {
							StaticConnection conn = new StaticConnection(source, now, EdgeBaseType.Self.Value());
							IRGeneratorForOneProject.GetInstance().RegistConnection(conn);
						}
					}
				}

				HandleNodeSelfAndBranchDependency(irc, source_method_receiver_element, now, branch_dependency);
				irc.GoForwardOneIRTreeNode(source_method_receiver_element, now);
			}
			// }
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

	public static void GenerateNoVariableBindingIR(IRGeneratorForOneLogicBlock irgfob, ASTNode node,
			Set<IJavaElement> member_set, String code) {
		IRCode irc = irgfob.irc;
		HashMap<IJavaElement, ASTNode> all_happen = irgfob.all_happen;
		HashMap<IJavaElement, IRForOneInstruction> branch_dependency = irgfob.branchs_var_instr_order.peek();

		Set<IJavaElement> temp_bindings = member_set;
		Iterator<IJavaElement> titr = temp_bindings.iterator();

		List<IRForOneOperation> ops = new LinkedList<IRForOneOperation>();
		while (titr.hasNext()) {
			IJavaElement ije = titr.next();
			ASTNode im_node = all_happen.get(ije);
			if (im_node != null && ASTSearch.ASTNodeContainsAnASTNode(node, im_node)) {
				// int start = exact_node.getStartPosition();
				// int end = start + exact_node.getLength() - 1;
				// IRInstrKind ir_kind = IRInstrKind.ComputeKind(1);
				IRForOneOperation now = new IRForOneOperation(irc, ije, code, DefaultINodeTask.class);
				ops.add(now);
				irc.GoForwardOneIRTreeNode(ije, now);
				HandleNodeSelfAndBranchDependency(irc, ije, now, branch_dependency);
			}
		}
		HandleEachElementInSameOperationDependency(ops);
	}

	public static void GenerateGeneralIR(IRGeneratorForOneLogicBlock irgfob, ASTNode node, String code) {
		IRCode irc = irgfob.irc;
		// Map<IJavaElement, Integer> all_count = irgfob.all_count;
		HashMap<IJavaElement, ASTNode> all_happen = irgfob.all_happen;
		HashMap<IJavaElement, IRForOneInstruction> branch_dependency = null;
		if (!irgfob.branchs_var_instr_order.isEmpty()) {
			branch_dependency = irgfob.branchs_var_instr_order.peek();
		}
		HashSet<IJavaElement> temp_statement_set = irgfob.temp_statement_environment_set;
		Set<IJavaElement> concern = new HashSet<IJavaElement>(temp_statement_set);
//		Iterator<IJavaElement> oitr = temp_statement_set.iterator();
//		while (oitr.hasNext()) {
//			IJavaElement ije = oitr.next();
//			Set<IJavaElement> dep = irc.GetAssignDependency(ije);
//			if (dep != null) {
//				concern.addAll(dep);
//			}
//		}
		Iterator<IJavaElement> titr = concern.iterator();
		if (!titr.hasNext()) {
			// ConstantUniqueElement.FetchConstantElement(code);
			// do nothing.
		}

		List<IRForOneOperation> ops = new LinkedList<IRForOneOperation>();
		while (titr.hasNext()) {
			IJavaElement im = titr.next();
			ASTNode im_node = all_happen.get(im);
			if (im_node != null && ASTSearch.ASTNodeContainsAnASTNode(node, im_node)) {
				//	Integer count = all_count.get(im);
				//	if (count != null && count >= 0) {
				//		count++;
				//	if (count > IRGeneratorForOneLogicBlock.GetMaxLevel()) {
				//		count = -1;
				//	} else {
						// int start = exact_node.getStartPosition();
						// int end = start + exact_node.getLength() - 1;
						// IRInstrKind ir_kind = IRInstrKind.ComputeKind(count);
						IRForOneOperation now = new IRForOneOperation(irc, im, code, DefaultINodeTask.class);
						ops.add(now);
						HandleNodeSelfAndBranchDependency(irc, im, now, branch_dependency);

						irc.GoForwardOneIRTreeNode(im, now);
				//	}
				//		all_count.put(im, count);
				// 	}
			}
		}
		HandleEachElementInSameOperationDependency(ops);
	}

	private static void HandleNodeSelfAndBranchDependency(IRCode irc, IJavaElement ije, IRForOneInstruction now,
			HashMap<IJavaElement, IRForOneInstruction> branch_dependency) {
		irc.GoForwardOneIRTreeNode(ije, now);
		if (branch_dependency == null) {
			return;
		}
		Set<IJavaElement> bkeys = branch_dependency.keySet();
		Iterator<IJavaElement> bitr = bkeys.iterator();
		while (bitr.hasNext()) {
			IJavaElement bim = bitr.next();
			IRForOneInstruction pt = branch_dependency.get(bim);
			if (pt != null) {
				IRGeneratorForOneProject.GetInstance()
						.RegistConnection(new StaticConnection(pt, now, EdgeBaseType.Branch.Value()));
			}
		}
	}

	public static void AddMethodReturnVirtualReceiveDependency(IRCode irc, IJavaElement ije,
			IRForOneSourceMethodInvocation irfomi) {
		IRForOneInstruction irfoo = irc.GetLastIRTreeNode(ije); // new
																// IRForOneOperation(irc,
																// ije,
																// IRMeta.VirtualMethodReturn);
		// irfoo.PutConnectionMergeTask(AllOutDirectionConnection.GetAllOutDirectionConnection(),
		// new SkipSelfTask());
		// HandleNodeSelfAndBranchDependency(irc, ije, irfoo, null);
		if (irfoo != null) {
			IRGeneratorForOneProject.GetInstance()
					.RegistConnection(new StaticConnection(irfomi, irfoo, EdgeBaseType.Sequential.Value()));
		}
		// irfomi.PutConnectionMergeTask(conn, new MethodReturnPassTask());
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

	public static void HandleEachElementInSameOperationDependency(List<IRForOneOperation> ops) {
		Iterator<IRForOneOperation> oitr = ops.iterator();
		while (oitr.hasNext()) {
			IRForOneOperation irfop = oitr.next();
			Iterator<IRForOneOperation> oitr_inner = ops.iterator();
			while (oitr_inner.hasNext()) {
				IRForOneOperation irfop_inner = oitr_inner.next();
				if (irfop == irfop_inner) {
					break;
				}
				IRGeneratorForOneProject.GetInstance().RegistConnection(
						new StaticConnection(irfop_inner, irfop, EdgeBaseType.SameOperations.Value()));
				IRGeneratorForOneProject.GetInstance().RegistConnection(
						new StaticConnection(irfop, irfop_inner, EdgeBaseType.SameOperations.Value()));
			}
		}
	}

}
