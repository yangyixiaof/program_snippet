package cn.yyx.research.program.ir.generation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import cn.yyx.research.program.eclipse.searchutil.EclipseSearchForIMember;
import cn.yyx.research.program.ir.generation.structure.ElementBranchInfo;
import cn.yyx.research.program.ir.search.IRSearchMethodRequestor;
import cn.yyx.research.program.ir.storage.connection.ConnectionInfo;
import cn.yyx.research.program.ir.storage.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.connection.StaticConnection;
import cn.yyx.research.program.ir.storage.connection.detail.MethodParameterIndexConnection;
import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.execution.DefaultINodeTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneEmptyConstructorInvocation;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneOperation;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneSentinel;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneSourceMethodInvocation;
import cn.yyx.research.program.systemutil.ReflectionInvoke;

public class IRGeneratorHelper {
	// necessary. remember to add virtual node to each parameter.
	// Solved. Solution is to add every virtual node when an IRTreeForOneElement
	// is created. Important!!! every parameter needs a virtual node with
	// skip_self_task and require_self flag.
	// Solved. all things need to be checked that the only one self true node
	// needs
	// to add a distinct node.
	// can only be invoked in end_visit_method_invocation.
	private static void HandleInstructionsUnderParentInfix(IRGeneratorForOneLogicBlock irgfob,
			IRForOneInstruction now) {
		if (irgfob.most_parent_infix != null) {
			// source_method_receiver_element
			Set<IRForOneInstruction> instrs = irgfob.instrs_under_most_parent_infix.get(now.getIm());
			if (instrs == null) {
				instrs = new HashSet<IRForOneInstruction>();
				irgfob.instrs_under_most_parent_infix.put(now.getIm(), instrs);
			}
			instrs.add(now);
		}
	}

	public static Object CreateIRInstruction(IRGeneratorForOneLogicBlock irgfob, Class<?> cls, Object[] objs) {
		IRForOneInstruction now = (IRForOneInstruction) ReflectionInvoke.InvokeConstructor(cls, objs);
		HandleInstructionsUnderParentInfix(irgfob, now);
		return now;
	}

	public static IRForOneSourceMethodInvocation GenerateMethodInvocationIR(IRGeneratorForOneLogicBlock irgfob,
			List<Expression> nlist, IMethod parent_im, IMethod im, IMethodBinding imb, Expression expr, String identifier, ASTNode node) {
		IRForOneSourceMethodInvocation now = null;
		IRCode irc = irgfob.irc;
		HashMap<ASTNode, Map<IJavaElement, IRForOneInstruction>> temp_statement_instr_order = irgfob.method_parameter_element_instr_order;
		// HashMap<ASTNode, Map<IJavaElement, Boolean>>
		// temp_statement_instr_is_self =
		// irgfob.method_parameter_element_instr_is_self;
		// Set<IJavaElement> temp_statement_environment_set =
		// irgfob.temp_statement_environment_set;
		// Map<IJavaElement, Integer> all_count = irgfob.all_count;
		// HashMap<IJavaElement, IRForOneInstruction> branch_dependency = null;
		// if (!irgfob.branch_var_instr_order.isEmpty()) {
		//	branch_dependency = irgfob.branch_var_instr_order.peek();
		// }
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
				if (parent_im != null) {
					Iterator<IMethod> mitr = methods.iterator();
					while (mitr.hasNext()) {
						IMethod callee = mitr.next();
						IRGeneratorForOneProject.GetInstance().AddCalleeCaller(callee, parent_im);
					}
				}
				Map<IRForOneInstruction, List<Integer>> para_order_instr_index_map = new HashMap<IRForOneInstruction, List<Integer>>();

				now = (IRForOneSourceMethodInvocation) CreateIRInstruction(irgfob, IRForOneSourceMethodInvocation.class,
						new Object[] { im.getElementName(), irc, source_method_receiver_element, methods,
								DefaultINodeTask.class, para_order_instr_index_map });

				// handle para_order_instr_index_map.
				Iterator<Expression> nitr = nlist.iterator();
				int idx = -1;
				while (nitr.hasNext()) {
					idx++;
					Expression nexpr = nitr.next();
					Map<IJavaElement, IRForOneInstruction> jele_order = temp_statement_instr_order.get(nexpr);
					// Map<IJavaElement, Boolean> jele_is_self =
					// temp_statement_instr_is_self.get(nexpr);
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

						IRGeneratorForOneProject.GetInstance()
								.RegistConnection(new StaticConnection(source, now, new ConnectionInfo(EdgeBaseType.Sequential.Value())));
					}
				}
			}
		} else {
			ITypeBinding itb = imb.getDeclaringClass();
			IType decl_type = null;
			if (itb != null && itb.isFromSource()) {
				IJavaElement jele = itb.getJavaElement();
				if (jele != null && jele instanceof IType) {
					decl_type = (IType)jele;
				}
			}
			if (decl_type != null) {
				now = (IRForOneSourceMethodInvocation) CreateIRInstruction(irgfob,
						IRForOneEmptyConstructorInvocation.class, new Object[] { decl_type, decl_type.getElementName(), irc,
								source_method_receiver_element, DefaultINodeTask.class });
			}
		}
		if (now != null) {
			// add every connection to now method for each IJavaElement in
			// current environment.
			Map<IJavaElement, IRForOneInstruction> curr_env = irc.CopyEnvironment();
			Set<IJavaElement> curr_keys = curr_env.keySet();
			Iterator<IJavaElement> curr_itr = curr_keys.iterator();
			while (curr_itr.hasNext()) {
				IJavaElement cije = curr_itr.next();
				IRForOneInstruction cirfoi = curr_env.get(cije);
				if (!(cirfoi instanceof IRForOneSentinel)) {
					IRGeneratorForOneProject.GetInstance()
							.RegistConnection(new StaticConnection(cirfoi, now, new ConnectionInfo(EdgeBaseType.Sequential.Value())));
				}
			}

			// handle dependency and barrier.
			HandleNodeSelfAndSourceMethodAndBranchDependency(irc, source_method_receiver_element, now,
					irgfob.branch_var_instr_order, irgfob.source_invocation_barrier, irgfob.element_has_set_branch,
					irgfob.element_has_set_source_method_barrier);
			irgfob.source_invocation_barrier.pop();
			irgfob.source_invocation_barrier.push(now);
			irgfob.element_has_set_source_method_barrier.clear();
		}
		return now;
	}

	public static void GenerateNoVariableBindingIR(IRGeneratorForOneLogicBlock irgfob, ASTNode node,
			Set<IJavaElement> member_set, String code) {
		IRCode irc = irgfob.irc;
		// HashMap<IJavaElement, ASTNode> all_happen = irgfob.all_happen;
		// HashMap<IJavaElement, IRForOneInstruction> branch_dependency = irgfob.branch_var_instr_order.peek();

		Set<IJavaElement> temp_bindings = member_set;
		Iterator<IJavaElement> titr = temp_bindings.iterator();

		List<IRForOneInstruction> ops = new LinkedList<IRForOneInstruction>();
		while (titr.hasNext()) {
			IJavaElement ije = titr.next();
			// ASTNode im_node = all_happen.get(ije);
			//  && ASTSearch.ASTNodeContainsAnASTNode(node, im_node)
			// if (im_node != null) {
				// int start = exact_node.getStartPosition();
				// int end = start + exact_node.getLength() - 1;
				// IRInstrKind ir_kind = IRInstrKind.ComputeKind(1);
				IRForOneInstruction now = (IRForOneInstruction) CreateIRInstruction(irgfob, IRForOneOperation.class,
						new Object[] { irc, ije, code, DefaultINodeTask.class });
				ops.add(now);
				// irc.GoForwardOneIRTreeNode(ije, now);
				HandleNodeSelfAndSourceMethodAndBranchDependency(irc, ije, now, irgfob.branch_var_instr_order,
						irgfob.source_invocation_barrier, irgfob.element_has_set_branch,
						irgfob.element_has_set_source_method_barrier);
			// }
		}
		HandleEachElementInSameOperationDependency(ops);
	}

	public static List<IRForOneInstruction> GenerateGeneralIR(IRGeneratorForOneLogicBlock irgfob, 
			String code) {
		return GenerateGeneralIR(irgfob, code, DefaultINodeTask.class);
	}
	
	public static List<IRForOneInstruction> GenerateGeneralIR(IRGeneratorForOneLogicBlock irgfob, Set<IJavaElement> temp_statement_set, 
			String code) {
		return GenerateGeneralIR(irgfob, temp_statement_set, code, DefaultINodeTask.class);
	}
	
	public static List<IRForOneInstruction> GenerateGeneralIR(IRGeneratorForOneLogicBlock irgfob, Set<IJavaElement> temp_statement_set, 
			String code, boolean handle_same_operations) {
		return GenerateGeneralIR(irgfob, temp_statement_set, code, DefaultINodeTask.class, handle_same_operations);
	}
	
//	public static List<IRForOneInstruction> GenerateGeneralIR(IRGeneratorForOneLogicBlock irgfob, Set<IJavaElement> temp_statement_set, 
//			String code, Class<? extends IIRNodeTask> task_class, boolean handle_same_operations) {
//		return GenerateGeneralIR(irgfob, temp_statement_set, code, task_class, handle_same_operations);
//	}

	public static List<IRForOneInstruction> GenerateGeneralIR(IRGeneratorForOneLogicBlock irgfob, 
			String code, Class<? extends IIRNodeTask> task_class, boolean handle_same_operations) {
		return GenerateGeneralIR(irgfob, code, task_class, IRForOneOperation.class, handle_same_operations);
	}
	
	public static List<IRForOneInstruction> GenerateGeneralIR(IRGeneratorForOneLogicBlock irgfob, 
			String code, Class<? extends IIRNodeTask> task_class) {
		return GenerateGeneralIR(irgfob, code, task_class, IRForOneOperation.class);
	}
	
	public static List<IRForOneInstruction> GenerateGeneralIR(IRGeneratorForOneLogicBlock irgfob, Set<IJavaElement> temp_statement_set, 
			String code, Class<? extends IIRNodeTask> task_class, boolean handle_same_operations) {
		return GenerateGeneralIR(irgfob, temp_statement_set, code, task_class, IRForOneOperation.class, handle_same_operations);
	}
	
	public static List<IRForOneInstruction> GenerateGeneralIR(IRGeneratorForOneLogicBlock irgfob, Set<IJavaElement> temp_statement_set, 
			String code, Class<? extends IIRNodeTask> task_class) {
		return GenerateGeneralIR(irgfob, temp_statement_set, code, task_class, IRForOneOperation.class, true);
	}
	
	public static List<IRForOneInstruction> GenerateGeneralIR(IRGeneratorForOneLogicBlock irgfob, 
			String code, Class<? extends IIRNodeTask> task_class,
			Class<? extends IRForOneInstruction> operation_class, boolean handle_same_operations) {
		return GenerateGeneralIR(irgfob, irgfob.CurrentElements(), code, task_class, operation_class, handle_same_operations);
	}
	
	public static List<IRForOneInstruction> GenerateGeneralIR(IRGeneratorForOneLogicBlock irgfob, 
			String code, Class<? extends IIRNodeTask> task_class,
			Class<? extends IRForOneInstruction> operation_class) {
		return GenerateGeneralIR(irgfob, irgfob.CurrentElements(), code, task_class, operation_class, true);
	}
	
	public static List<IRForOneInstruction> GenerateGeneralIR(IRGeneratorForOneLogicBlock irgfob, Set<IJavaElement> temp_statement_set, 
			String code, Class<? extends IIRNodeTask> task_class,
			Class<? extends IRForOneInstruction> operation_class) {
		return GenerateGeneralIR(irgfob, temp_statement_set, code, task_class, operation_class, true);
	}
	
	public static List<IRForOneInstruction> GenerateGeneralIR(IRGeneratorForOneLogicBlock irgfob, Set<IJavaElement> temp_statement_set, 
			String code, Class<? extends IIRNodeTask> task_class,
			Class<? extends IRForOneInstruction> operation_class, boolean handle_same_operations) {
		IRCode irc = irgfob.irc;
		// Map<IJavaElement, Integer> all_count = irgfob.all_count;
		// HashMap<IJavaElement, ASTNode> all_happen = irgfob.all_happen;
//		HashMap<IJavaElement, IRForOneInstruction> branch_dependency = null;
//		if (!irgfob.branch_var_instr_order.isEmpty()) {
//			branch_dependency = irgfob.branch_var_instr_order.peek();
//		}
		// Set<IJavaElement> temp_statement_set = irgfob.temp_statement_environment_set;
		Set<IJavaElement> concern = new HashSet<IJavaElement>(temp_statement_set);
		// Iterator<IJavaElement> oitr = temp_statement_set.iterator();
		// while (oitr.hasNext()) {
		// IJavaElement ije = oitr.next();
		// Set<IJavaElement> dep = irc.GetAssignDependency(ije);
		// if (dep != null) {
		// concern.addAll(dep);
		// }
		// }
		Iterator<IJavaElement> titr = concern.iterator();
		if (!titr.hasNext()) {
			// ConstantUniqueElement.FetchConstantElement(code);
			// do nothing.
		}

		List<IRForOneInstruction> ops = new LinkedList<IRForOneInstruction>();
		while (titr.hasNext()) {
			IJavaElement im = titr.next();
			// ASTNode im_node = all_happen.get(im);
			// && ASTSearch.ASTNodeContainsAnASTNode(node, im_node)
			// if (im_node != null) {
				IRForOneInstruction now = (IRForOneInstruction) CreateIRInstruction(irgfob, operation_class,
						new Object[] { irc, im, code, task_class });
				ops.add(now);
				HandleNodeSelfAndSourceMethodAndBranchDependency(irc, im, now, irgfob.branch_var_instr_order,
						irgfob.source_invocation_barrier, irgfob.element_has_set_branch,
						irgfob.element_has_set_source_method_barrier);
			// }
		}
		if (handle_same_operations) {
			HandleEachElementInSameOperationDependency(ops);
		}
		return ops;
	}
	
	/**
	 * 
	 * @param irgfob
	 * @param new_all_list contains receiver expression, if does have, the first element is null.
	 * @return
	 */
	public static void GenerateBinaryORUnResolvedMethodIR(IRGeneratorForOneLogicBlock irgfob, List<Expression> new_all_list, List<IRForOneInstruction> ops) {
		Map<IJavaElement, IRForOneInstruction> last_op = new HashMap<IJavaElement, IRForOneInstruction>();
		Iterator<IRForOneInstruction> opsitr = ops.iterator();
		while (opsitr.hasNext()) {
			IRForOneInstruction irop = opsitr.next();
			last_op.put(irop.getIm(), irop);
		}
		
		Iterator<Expression> nitr = new_all_list.iterator();
		int index = -1;
		while (nitr.hasNext()) {
			index++;
			Expression nexpr = nitr.next();
			if (nexpr != null) {
				Map<IJavaElement, IRForOneInstruction> jele_order = irgfob.method_parameter_element_instr_order.get(nexpr);
				Set<IJavaElement> jkeys = jele_order.keySet();
				Iterator<IJavaElement> jkitr = jkeys.iterator();
				while (jkitr.hasNext()) {
					IJavaElement ije = jkitr.next();
					IRForOneInstruction irfoi = jele_order.get(ije);
					IRForOneInstruction last_oi = last_op.get(ije);
					IRGeneratorForOneProject.GetInstance()
						.RegistConnection(new StaticConnection(irfoi, last_oi, new ConnectionInfo(EdgeBaseType.Sequential.Value(), new MethodParameterIndexConnection(nexpr, index))));
				}
			}
		}
	}
	
	public static void HandleNodeSelfDependency(IRCode irc, IJavaElement ije,
			IRForOneInstruction now) {
		irc.GoForwardOneIRTreeNode(ije, now);
	}
	
	public static void HandleSourceMethodAndBranchDependency(IRCode irc, IJavaElement ije,
			IRForOneInstruction now, Stack<HashMap<IJavaElement, IRForOneInstruction>> branch_dependency,
			Stack<IRForOneInstruction> source_method_barrier, Stack<ElementBranchInfo> element_has_set_branch,
			HashMap<IJavaElement, Boolean> element_has_set_source_method_barrier) {
		HandleSourceMethodAndBranchDependencyRefined(irc, ije, now, branch_dependency.isEmpty() ? null : branch_dependency.peek(), source_method_barrier.isEmpty() ? null : source_method_barrier.peek(), element_has_set_branch.isEmpty()? null : element_has_set_branch.peek(), element_has_set_source_method_barrier);
	}
	
	private static void HandleSourceMethodAndBranchDependencyRefined(IRCode irc, IJavaElement ije,
			IRForOneInstruction now, HashMap<IJavaElement, IRForOneInstruction> branch_dependency,
			IRForOneInstruction source_method_barrier, ElementBranchInfo element_has_set_branch,
			HashMap<IJavaElement, Boolean> element_has_set_source_method_barrier) {
		if (branch_dependency != null) {
			Boolean has_set = element_has_set_branch.ElementMainBranchHasSet(ije);
			if (has_set == null) {
				Set<IJavaElement> bkeys = branch_dependency.keySet();
				Iterator<IJavaElement> bitr = bkeys.iterator();
				while (bitr.hasNext()) {
					IJavaElement bim = bitr.next();
					if (bim == ije) {
						continue;
					}
					IRForOneInstruction pt = branch_dependency.get(bim);
					if (pt != null) {
						IRGeneratorForOneProject.GetInstance()
								.RegistConnection(new StaticConnection(pt, now, new ConnectionInfo(EdgeBaseType.Branch.Value())));
					}
				}
				element_has_set_branch.SetElementMainBranch(ije);
			}
		}
		if (element_has_set_branch != null) {
			if (element_has_set_branch.ElementChanged()) {
				if (!element_has_set_branch.ChangeIsApplied(ije)) {
					element_has_set_branch.SetChangeApplied(ije);
					Collection<IRForOneInstruction> chgd_instrs = element_has_set_branch.ChangedElements();
					Iterator<IRForOneInstruction> citr = chgd_instrs.iterator();
					while (citr.hasNext()) {
						IRForOneInstruction instr = citr.next();
						IRGeneratorForOneProject.GetInstance()
							.RegistConnection(new StaticConnection(instr, now, new ConnectionInfo(EdgeBaseType.Branch.Value())));
					}
				}
			}
		}
		if (source_method_barrier != null) {
			Boolean has_set = element_has_set_source_method_barrier.get(ije);
			if (has_set == null) {
				IRGeneratorForOneProject.GetInstance().RegistConnection(
						new StaticConnection(source_method_barrier, now, new ConnectionInfo(EdgeBaseType.Barrier.Value())));
				element_has_set_source_method_barrier.put(ije, true);
			}
		}
	}
	
	public static void HandleNodeSelfAndSourceMethodAndBranchDependency(IRCode irc, IJavaElement ije,
			IRForOneInstruction now, Stack<HashMap<IJavaElement, IRForOneInstruction>> branch_dependency,
			Stack<IRForOneInstruction> source_method_barrier, Stack<ElementBranchInfo> element_has_set_branch, // HashMap<IJavaElement, Boolean>
			HashMap<IJavaElement, Boolean> element_has_set_source_method_barrier) {
		HandleNodeSelfDependency(irc, ije, now);
		HandleSourceMethodAndBranchDependency(irc, ije, now, branch_dependency, source_method_barrier, element_has_set_branch, element_has_set_source_method_barrier);
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
					.RegistConnection(new StaticConnection(irfomi, irfoo, new ConnectionInfo(EdgeBaseType.Sequential.Value())));
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

	public static void HandleEachElementInSameOperationDependency(List<IRForOneInstruction> ops) {
		Iterator<IRForOneInstruction> oitr = ops.iterator();
		while (oitr.hasNext()) {
			IRForOneInstruction irfop = oitr.next();
			Iterator<IRForOneInstruction> oitr_inner = ops.iterator();
			while (oitr_inner.hasNext()) {
				IRForOneInstruction irfop_inner = oitr_inner.next();
				if (irfop == irfop_inner) {
					break;
				}
				IRGeneratorForOneProject.GetInstance().RegistConnection(
						new StaticConnection(irfop_inner, irfop, new ConnectionInfo(EdgeBaseType.SameOperations.Value())));
				IRGeneratorForOneProject.GetInstance().RegistConnection(
						new StaticConnection(irfop, irfop_inner, new ConnectionInfo(EdgeBaseType.SameOperations.Value())));
			}
		}
	}

}
