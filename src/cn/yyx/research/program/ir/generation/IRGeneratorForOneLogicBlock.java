package cn.yyx.research.program.ir.generation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.*;

import cn.yyx.research.program.ir.IRConstantMeta;
import cn.yyx.research.program.ir.IRMeta;
import cn.yyx.research.program.ir.ast.ASTSearch;
import cn.yyx.research.program.ir.bind.BindingManager;
import cn.yyx.research.program.ir.element.ControlLogicHolderElement;
import cn.yyx.research.program.ir.element.SourceMethodHolderElement;
import cn.yyx.research.program.ir.element.UncertainReferenceElement;
import cn.yyx.research.program.ir.element.UnresolvedNameOrFieldAccessElement;
import cn.yyx.research.program.ir.element.UnresolvedTypeElement;
import cn.yyx.research.program.ir.element.VirtualDefinedElement;
import cn.yyx.research.program.ir.generation.state.IJavaElementState;
import cn.yyx.research.program.ir.generation.state.NodeIJavaElement;
import cn.yyx.research.program.ir.generation.state.NodeIJavaElementStack;
import cn.yyx.research.program.ir.generation.structure.IndexInfoRunner;
import cn.yyx.research.program.ir.generation.structure.NodeConnectionDetailPair;
import cn.yyx.research.program.ir.generation.traversal.task.IRASTNodeTask;
import cn.yyx.research.program.ir.orgranization.IRTreeForOneControlElement;
import cn.yyx.research.program.ir.orgranization.IRTreeForOneElement;
import cn.yyx.research.program.ir.storage.connection.ConnectionInfo;
import cn.yyx.research.program.ir.storage.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.connection.StaticConnection;
import cn.yyx.research.program.ir.storage.connection.detail.InfixExpressionIndexConnection;
import cn.yyx.research.program.ir.storage.node.execution.DefaultINodeTask;
import cn.yyx.research.program.ir.storage.node.execution.RequireHandleTask;
import cn.yyx.research.program.ir.storage.node.execution.SkipSelfTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneMethod;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneBranchControl;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneMethodBarrier;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneOperation;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneSourceMethodInvocation;

public class IRGeneratorForOneLogicBlock extends IRGeneratorForValidation {

	public static int max_level = Integer.MAX_VALUE; // Integer.MAX_VALUE partly
														// means infinite.
	public static final int un_exist = -100;
	// Solved. remember to add remember nodes.
	// Solved. return and assign right should add special task.
	// Solved. variable declarations should be removed, only assignment in it
	// should be retained.
	// Solved. Important!!!!!!!, dependencies between different variables seem
	// not handled, some are not needed but some are needed.

	// Solved. switch case mechanism is not as tree or graph and is sequential
	// which is not right.

	// for return statements, all nodes related to return should be recorded.

	// name must be resolved and ensure it is a variable, a global variable or a
	// type.
	// for method invocation's parameters.
	// Solved. this element is not assigned. should be assigned in
	// HandleIJavaElement.
	// above used for method invocation only.
	// Solved. this element is not assigned. should be assigned in
	// HandleIJavaElement.

	protected HashMap<ASTNode, Set<IJavaElement>> node_element_memory = new HashMap<ASTNode, Set<IJavaElement>>();

	protected NodeIJavaElementStack node_element_stack = new NodeIJavaElementStack();

	private void PushNodeIJavaElementStack(ASTNode node, Set<IJavaElement> ijes) {
		node_element_stack.Push(node, ijes);
	}

	private NodeIJavaElement PopNodeIJavaElementStack() {
		NodeIJavaElement node_ele = node_element_stack.Pop();
		if (!node_element_stack.IsEmpty()) {
			NodeIJavaElement now_node_ele = node_element_stack.Peek();
			now_node_ele.Merge(node_ele, all_happen);
		}
		ASTNode node = node_ele.GetNode();
		Set<IJavaElement> node_ijes = node_ele.GetIJavaElementSet();
		if (node_element_memory.containsKey(node)) {
			Set<IJavaElement> ijes = node_element_memory.get(node);
			if (ijes == null) {
				ijes = new HashSet<IJavaElement>();
				node_element_memory.put(node, ijes);
			}
			ijes.addAll(node_ijes);
		}
		return node_ele;
	}

	// protected HashSet<IJavaElement> temp_statement_expression_environment_set
	// = new HashSet<IJavaElement>();
	// protected HashSet<IJavaElement> temp_statement_environment_set = new
	// HashSet<IJavaElement>();
	// protected HashMap<IJavaElement, Integer> all_count = new
	// HashMap<IJavaElement, Integer>();
	protected HashMap<IJavaElement, ASTNode> all_happen = new HashMap<IJavaElement, ASTNode>();

	protected InfixExpression most_parent_infix = null;
	protected Map<IJavaElement, Set<IRForOneInstruction>> instrs_under_most_parent_infix = new HashMap<IJavaElement, Set<IRForOneInstruction>>();
	// check if all_happen is all right assigned. yes.

	// these two variables are all be handled when encountering source method
	// invocation.
	// this variable is initialized in Construction method. so this is already
	// be initialized.
	protected IJavaElement control_logic_holder_element = null; // already
																// assigned.
	protected IJavaElement source_method_virtual_holder_element = null; // already
	// assigned.
	// this should be handled. this is no need anymore.
	// protected HashMap<ASTNode, IJavaElement> source_method_return_element =
	// new HashMap<ASTNode, IJavaElement>();

	private Set<IJavaElement> SearchAndRememberAllElementsInASTNodeInJustEnvironment(Expression expr) {
		// HashSet<IJavaElement> result = new HashSet<IJavaElement>();
		// result.addAll(temp_statement_expression_environment_set);
		// Set<ASTNode> tkeys = node_element_memory.keySet();
		// Iterator<ASTNode> titr = tkeys.iterator();
		// List<ASTNode> remove_nodes = new LinkedList<ASTNode>();
		// while (titr.hasNext()) {
		// ASTNode astnode = titr.next();
		// if (ASTSearch.ASTNodeContainsAnASTNode(expr, astnode)) {
		// remove_nodes.add(astnode);
		// Set<IJavaElement> set = node_element_memory.get(astnode);
		// result.addAll(set);
		// }
		// }
		// Iterator<ASTNode> rnitr = remove_nodes.iterator();
		// while (rnitr.hasNext()) {
		// ASTNode an = rnitr.next();
		// node_element_memory.remove(an);
		// }
		// remove_nodes.clear();
		// node_element_memory.put(expr, result);

		return node_element_memory.get(expr);
	}

	// ASTNode node, boolean remember
	protected void ExpressionOverHandle() {
		// if (remember) {
		// temp_statement_expression_element_memory.put(node,
		// new
		// HashSet<IJavaElement>(temp_statement_expression_environment_set));
		// }

		// temp_statement_expression_environment_set.clear();
	}

	protected void StatementOverHandle() {
		// no need to do that anymore.
		// temp_statement_instr_order.clear();

		// temp_statement_expression_environment_set.clear();
		// temp_statement_environment_set.clear();
	}

	protected HashMap<IJavaElement, Boolean> element_has_set_branch = new HashMap<IJavaElement, Boolean>();
	protected HashMap<IJavaElement, Boolean> element_has_set_source_method_barrier = new HashMap<IJavaElement, Boolean>();
	protected Stack<HashMap<IJavaElement, IRForOneInstruction>> branch_var_instr_order = new Stack<HashMap<IJavaElement, IRForOneInstruction>>();
	protected Stack<IRForOneInstruction> source_invocation_barrier = new Stack<IRForOneInstruction>();
	{
		source_invocation_barrier.add(null);
	}

	public Set<IJavaElement> CurrentElements() {
		return node_element_stack.Peek().GetIJavaElementSet();
	}

	protected void PushBranchInstructionOrder(Map<IJavaElement, IRForOneInstruction> branch_instrs) {
		HashMap<IJavaElement, IRForOneInstruction> t_hash = new HashMap<IJavaElement, IRForOneInstruction>(
				branch_instrs);
		IRTreeForOneControlElement holder_ir = irc.GetControlLogicHolderElementIR();
		IRForOneBranchControl control_node = holder_ir.GetControlNode();
		if (control_node != null) {
			t_hash.put(control_logic_holder_element, control_node);
		}
		branch_var_instr_order.push(t_hash);
		source_invocation_barrier.add(null);
		element_has_set_branch.clear();
	}

	protected void PopBranchInstructionOrder() {
		branch_var_instr_order.pop();
		source_invocation_barrier.pop();
		element_has_set_branch.clear();
	}

	protected IRCode irc = null;

	// protected Queue<IRTask> undone_tasks = new LinkedList<IRTask>();

	protected IRASTNodeTask post_visit_task = new IRASTNodeTask();
	protected IRASTNodeTask pre_visit_task = new IRASTNodeTask();

	// protected Map<ASTNode, HashSet<IJavaElement>> ast_block_bind = new HashMap<ASTNode, HashSet<IJavaElement>>();

	// protected Stack<HashSet<IBinding>> switch_case_bind = new
	// Stack<HashSet<IBinding>>();
	// protected Stack<LinkedList<ASTNode>> switch_case = new
	// Stack<LinkedList<ASTNode>>();
	protected IMethod parent_im = null;

	public IRGeneratorForOneLogicBlock(IMethod im, IRCode irc) {
		this.irc = irc;
		this.parent_im = im;
		this.source_method_virtual_holder_element = new SourceMethodHolderElement(
				irc.GetScopeIElement().getElementName().toString() + "&mholder");
		this.control_logic_holder_element = new ControlLogicHolderElement(
				irc.GetScopeIElement().getElementName().toString() + "&clogic");
		this.irc.SetSourceMethodElement(this.source_method_virtual_holder_element);
		this.irc.SetControlLogicHolderElement(this.control_logic_holder_element);
	}

	@Override
	public void preVisit(ASTNode node) {
//		if (node instanceof Block) {
//			ast_block_bind.put(node, new HashSet<IJavaElement>());
//		}

		pre_visit_task.ProcessAndRemoveTask(node);
		PushNodeIJavaElementStack(node, null);
		super.preVisit(node);
	}

	// post handling statements.
	@Override
	public void postVisit(ASTNode node) {
//		if (node instanceof Block) {
//			ast_block_bind.remove(node);
//		}
		if (node instanceof Statement) {
			StatementOverHandle();
		}
		
		PopNodeIJavaElementStack();
		post_visit_task.ProcessAndRemoveTask(node);
		super.postVisit(node);
	}

	private void HandleMethodDeclarationParameters(IRCode ircode, List<SimpleName> sns) {
		Iterator<SimpleName> itr = sns.iterator();
		while (itr.hasNext()) {
			SimpleName sn = itr.next();
			IBinding ib = sn.resolveBinding();
			if ((ib != null) && (ib instanceof IVariableBinding)) {
				IVariableBinding ivb = (IVariableBinding) ib;
				IJavaElement ije = ivb.getJavaElement();
				if (ije instanceof IJavaElement) {
					irc.AddParameter((IJavaElement) ije);
				}
			} else {
				irc.AddParameter(null);
			}
		}
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		super.Validation(node);
		List<SimpleName> sns = new LinkedList<SimpleName>();
		@SuppressWarnings("unchecked")
		List<SingleVariableDeclaration> svds = node.parameters();
		Iterator<SingleVariableDeclaration> sitr = svds.iterator();
		while (sitr.hasNext()) {
			VariableDeclaration vd = sitr.next();
			sns.add(vd.getName());
		}
		HandleMethodDeclarationParameters(irc, sns);
		Block body = node.getBody();
		if (body == null) {
			post_visit_task.Put(node, new Runnable() {
				@Override
				public void run() {
					StatementOverHandle();
				}
			});
		} else {
			pre_visit_task.Put(body, new Runnable() {
				@Override
				public void run() {
					StatementOverHandle();
				}
			});
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(LambdaExpression node) {
		boolean handled = false;
		IMethodBinding imb = node.resolveMethodBinding();
		if (imb != null) {
			IJavaElement jele = imb.getJavaElement();
			if (jele != null && jele instanceof IMethod) {
				IMethod im = (IMethod) jele;
				HandleIJavaElement(im, node);
				handled = true;

				// handle method declaration.
				List<SimpleName> sns = new LinkedList<SimpleName>();
				@SuppressWarnings("unchecked")
				List<VariableDeclaration> paras = node.parameters();
				Iterator<VariableDeclaration> pitr = paras.iterator();
				while (pitr.hasNext()) {
					VariableDeclaration vd = pitr.next();
					sns.add(vd.getName());
				}
				IRForOneMethod irfom = IRGeneratorForOneProject.GetInstance().FetchIMethodIR(im);
				HandleMethodDeclarationParameters(irfom, sns);
				IRGeneratorForOneLogicBlock irgfocb = new IRGeneratorForOneLogicBlock(null, irfom);
				node.getBody().accept(irgfocb);
			}
		}
		if (!handled) {
			HandleIJavaElement(IRGeneratorForOneProject.GetInstance().FetchUnresolvedLambdaUniqueElement(
					node.toString(), (IMember) irc.GetScopeIElement(), irc.CopyEnvironment()), node);
		}
		return false;
	}

	// method invocation.

	private IJavaElement WholeExpressionIsAnElement(ASTNode expr) {
		Iterator<IJavaElement> titr = CurrentElements().iterator();
		while (titr.hasNext()) {
			IJavaElement ije = titr.next();
			ASTNode happen = all_happen.get(ije);
			if (expr.equals(happen)) {
				return ije;
			}
		}
		return null;
	}

	protected HashMap<ASTNode, Map<IJavaElement, IRForOneInstruction>> method_parameter_element_instr_order = new HashMap<ASTNode, Map<IJavaElement, IRForOneInstruction>>();
	// protected HashMap<ASTNode, Map<IJavaElement, Boolean>>
	// method_parameter_element_instr_is_self = new HashMap<ASTNode,
	// Map<IJavaElement, Boolean>>();

	// private void RecordASTNodePreEnvironment(ASTNode node)
	// {
	// Map<IJavaElement, Integer> env = irc.CopyEnvironment();
	// method_parameter_element_instr_order.put(node, env);
	// }
	//
	// private boolean
	// CompareASTNodePreEnvironmentToJudgeIfDirectTransfer(ASTNode node)
	// {
	// if (temp_statement_expression_environment_set.size() == 1)
	// {
	// Map<IJavaElement, Integer> origin_env =
	// method_parameter_element_instr_order.get(node);
	// Iterator<IJavaElement> titr =
	// temp_statement_expression_environment_set.iterator();
	// IJavaElement ije = titr.next();
	// List<IRForOneInstruction> list = irc.GetOneAllIRUnits(ije);
	// if (list != null && list.size() > 0)
	// {
	// int idx = list.size() - 1;
	// Integer ori_idx = origin_env.get(ije);
	// if (ori_idx == idx)
	// {
	// return true;
	// }
	// }
	// }
	// return false;
	// }
	
	private void HandlePreMethodInvocationTask(Expression expr) {
		// pre_visit_task.put(expr, new Runnable() {
		// @Override
		// public void run() {
		// RecordASTNodePreEnvironment(expr);
		// }
		// });
		node_element_memory.put(expr, null);
		post_visit_task.Put(expr, new Runnable() {
			@Override
			public void run() {
				IJavaElement w_ije = WholeExpressionIsAnElement(expr);
				Map<IJavaElement, Boolean> new_is_self_env = new HashMap<IJavaElement, Boolean>();
				// method_parameter_element_instr_is_self.put(expr,
				// new_is_self_env);
				Map<IJavaElement, IRForOneInstruction> new_env = new HashMap<IJavaElement, IRForOneInstruction>();
				Iterator<IJavaElement> titr = SearchAndRememberAllElementsInASTNodeInJustEnvironment(expr)
						.iterator();
				// temp_statement_expression_environment_set.iterator()
				while (titr.hasNext()) {
					IJavaElement ije = titr.next();
					IRForOneInstruction last_instr = irc.GetLastIRTreeNode(ije);
					if (last_instr != null) {
						new_env.put(ije, last_instr);
						boolean direct_transfer = false;
						if (ije == w_ije) {
							direct_transfer = true;
						}
						new_is_self_env.put(ije, direct_transfer);
					}
				}
				method_parameter_element_instr_order.put(expr, new_env);
				ExpressionOverHandle();
				// node_element_memory.remove(expr);
			}
		});
	}

	private void PreMethodInvocation(List<Expression> exprs, Expression expr_receiver) {
		// temp_statement_instr_order
		if (expr_receiver != null) {
			HandlePreMethodInvocationTask(expr_receiver);
		}
		Iterator<Expression> eitr = exprs.iterator();
		while (eitr.hasNext()) {
			Expression expr = eitr.next();
			HandlePreMethodInvocationTask(expr);
		}
	}

	private void PostMethodInvocation(IMethod parent_im, IMethodBinding imb, List<Expression> nlist, Expression expr,
			String identifier, ASTNode node) {
		ITypeBinding dec_class = imb.getDeclaringClass();
		boolean from_source = false;
		if (dec_class != null) {
			from_source = true;
		}
		IRForOneSourceMethodInvocation now = null;
		IJavaElement jele = imb.getJavaElement();
		if (imb != null && dec_class != null && from_source
				&& ((jele == null && imb.isConstructor()) || (jele != null && jele instanceof IMethod))) {
			// source method invocation.
			if (jele == null) {
				now = IRGeneratorHelper.GenerateMethodInvocationIR(this, nlist, parent_im, null, imb, expr, identifier,
						node);
			} else {
				now = IRGeneratorHelper.GenerateMethodInvocationIR(this, nlist, parent_im, (IMethod) jele, imb, expr,
						identifier, node);
			}
		}
		
		Set<IJavaElement> curr_eles = new HashSet<IJavaElement>(CurrentElements());
		
		if (now != null) {
			UncertainReferenceElement ure = new UncertainReferenceElement(node.toString());
			HandleIJavaElement(ure, node);
			IRGeneratorHelper.AddMethodReturnVirtualReceiveDependency(irc, ure, now);
			
			// add barriers and corresponding sequential edges.
			List<IRForOneInstruction> ops = IRGeneratorHelper.GenerateGeneralIR(this, curr_eles, node,
					IRMeta.MethodInvocation + identifier, SkipSelfTask.class, IRForOneMethodBarrier.class);
			Iterator<IRForOneInstruction> opitr = ops.iterator();
			while (opitr.hasNext()) {
				IRForOneInstruction irfop = opitr.next();
				IRGeneratorForOneProject.GetInstance()
						.RegistConnection(new StaticConnection(now, irfop, new ConnectionInfo(EdgeBaseType.Sequential.Value())));
			}
		} else {
			// add binary or unresolved method-invoke.
			List<IRForOneInstruction> ops = IRGeneratorHelper.GenerateGeneralIR(this, curr_eles, node,
					IRMeta.MethodInvocation + identifier, SkipSelfTask.class);
			List<Expression> new_all_list = new LinkedList<Expression>();
			new_all_list.add(expr);
			new_all_list.addAll(nlist);
			IRGeneratorHelper.GenerateBinaryORUnResolvedMethodIR(this, new_all_list, ops);
		}

		// clear.
		if (expr != null) {
			node_element_memory.remove(expr);
		}
		Iterator<Expression> nitr = nlist.iterator();
		while (nitr.hasNext()) {
			Expression nexpr = nitr.next();
			method_parameter_element_instr_order.remove(nexpr);
			node_element_memory.remove(nexpr);
		}
		// method_parameter_element_instr_is_self.clear();
	}

	@Override
	public boolean visit(MethodInvocation node) {
		super.Validation(node);
		@SuppressWarnings("unchecked")
		List<Expression> exprs = (List<Expression>) node.arguments();
		PreMethodInvocation(exprs, node.getExpression());
		return super.visit(node);
	}

	@Override
	public void endVisit(MethodInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> nlist = (List<Expression>) node.arguments();
		PostMethodInvocation(parent_im, node.resolveMethodBinding(), nlist, node.getExpression(),
				node.getName().toString(), node);
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		super.Validation(node);
		@SuppressWarnings("unchecked")
		List<Expression> exprs = (List<Expression>) node.arguments();
		PreMethodInvocation(exprs, null);
		return super.visit(node);
	}

	@Override
	public void endVisit(SuperMethodInvocation node) {
		TreatSuperClassElement(node);
		@SuppressWarnings("unchecked")
		List<Expression> nlist = (List<Expression>) node.arguments();
		PostMethodInvocation(parent_im, node.resolveMethodBinding(), nlist, null, node.getName().toString(), node);
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> exprs = (List<Expression>) node.arguments();
		PreMethodInvocation(exprs, null);
		return super.visit(node);
	}

	@Override
	public void endVisit(SuperConstructorInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> nlist = (List<Expression>) node.arguments();
		PostMethodInvocation(parent_im, node.resolveConstructorBinding(), nlist, null, "super", node);
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> exprs = (List<Expression>) node.arguments();
		PreMethodInvocation(exprs, null);
		return super.visit(node);
	}

	@Override
	public void endVisit(ConstructorInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> nlist = (List<Expression>) node.arguments();
		PostMethodInvocation(parent_im, node.resolveConstructorBinding(), nlist, null, "this", node);
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		super.Validation(node);
		@SuppressWarnings("unchecked")
		List<Expression> nlist = (List<Expression>) node.arguments();
		PreMethodInvocation(nlist, node.getExpression());
		if (node.getAnonymousClassDeclaration() != null) {
			pre_visit_task.Put(node, new Runnable() {
				@Override
				public void run() {
					PostMethodInvocation(parent_im, node.resolveConstructorBinding(), nlist, node.getExpression(),
							"new#" + node.getType(), node);
				}
			});
		}
		return super.visit(node);
	}

	@Override
	public void endVisit(ClassInstanceCreation node) {
		if (node.getAnonymousClassDeclaration() == null) {
			@SuppressWarnings("unchecked")
			List<Expression> nlist = (List<Expression>) node.arguments();
			PostMethodInvocation(parent_im, node.resolveConstructorBinding(), nlist, null, "new#" + node.getType(),
					node);
		}
	}

	// handling statements.

	// handling branches.

	// Solved. all branches and loops should handle parallel connections.
	protected Map<ASTNode, Map<IJavaElement, IRForOneInstruction>> switch_record = new HashMap<ASTNode, Map<IJavaElement, IRForOneInstruction>>();

	private void PreVisitToGoNewBranchInSwitch(ASTNode all_in_control) {
		IRTreeForOneControlElement holder_ir = irc.GetControlLogicHolderElementIR();
		holder_ir.GoToOneBranch(all_in_control);
	}

	private void PostVisitToHandleMergeListInSwitch(ASTNode all_in_control, Set<IJavaElement> eles) {
		Iterator<IJavaElement> eitr = eles.iterator();
		while (eitr.hasNext()) {
			IJavaElement ije = eitr.next();
			irc.SwitchDirection(ije, switch_record.get(all_in_control).get(ije));
			Map<IJavaElement, List<NodeConnectionDetailPair>> merge = node_to_merge.get(all_in_control);
			if (merge == null) {
				merge = new HashMap<IJavaElement, List<NodeConnectionDetailPair>>();
				node_to_merge.put(all_in_control, merge);
			}
			List<NodeConnectionDetailPair> merge_list = merge.get(ije);
			if (merge_list == null) {
				merge_list = new LinkedList<NodeConnectionDetailPair>();
				merge.put(ije, merge_list);
			}
			merge_list.add(new NodeConnectionDetailPair(irc.GetLastIRTreeNode(ije), null));
		}
	}

	private void SwitchAndPrepareMergeInBranch(ASTNode all_in_control, ASTNode branch_first_stat,
			ASTNode branch_last_stat, List<ASTNode> first_to_last, boolean clear, boolean just_one_branch) {
		final IRGeneratorForOneLogicBlock this_ref = this;
		if (branch_first_stat != null) {
			pre_visit_task.Put(branch_first_stat, new Runnable() {
				@Override
				public void run() {
					PreVisitToGoNewBranchInSwitch(all_in_control);
					for (ASTNode node : first_to_last) {
						node_element_memory.put(node, null);
					}
					// ast_block_bind.put(branch_first_stat, new HashSet<IJavaElement>());
				}
			});
			post_visit_task.Put(branch_last_stat, new Runnable() {
				@Override
				public void run() {
					Set<IJavaElement> eles = new HashSet<IJavaElement>();
					for (ASTNode node : first_to_last) {
						eles.addAll(node_element_memory.remove(node));
					}
					// Set<IJavaElement> eles = node_element_memory.get(branch_first_stat);
					// node_element_memory.remove(branch_first_stat);
					
					PostVisitToHandleMergeListInSwitch(all_in_control, eles);
					
					if (clear) {
						StatementOverHandle();
					}

					// add virtual branch and virtual corresponding node.
					if (just_one_branch) {
						PreVisitToGoNewBranchInSwitch(all_in_control);
						Iterator<IJavaElement> eleitr = eles.iterator();
						while (eleitr.hasNext()) {
							IJavaElement eije = eleitr.next();
							IRForOneOperation irfop = (IRForOneOperation) IRGeneratorHelper.CreateIRInstruction(
									this_ref, IRForOneOperation.class,
									new Object[] { irc, eije, "nothing.", SkipSelfTask.class });
							IRTreeForOneElement itree = irc.GetIRTreeForOneElement(eije);
							itree.GoForwardANode(irfop);
						}
						PostVisitToHandleMergeListInSwitch(all_in_control, eles);
					}
				}
			});
		}
	}

	private void PreVisitBranch(ASTNode all_in_control, String branch_code, Expression judge,
			List<ASTNode> branch_first_stats, List<ASTNode> branch_last_stats, LinkedList<LinkedList<ASTNode>> branch_first_to_last, boolean clear, boolean just_one_branch) {
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.Put(judge, new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(this_ref, judge, branch_code);
				switch_record.put(all_in_control, irc.CopyEnvironment());

				Map<IJavaElement, IRForOneInstruction> branch_instrs = irc.CopyEnvironment(CurrentElements());
				PushBranchInstructionOrder(branch_instrs);

				IRTreeForOneControlElement holder_ir = irc.GetControlLogicHolderElementIR();
				holder_ir.EnteredOneLogicBlock(all_in_control, branch_instrs);

				if (clear) {
					StatementOverHandle();
				}
			}
		});
		Iterator<ASTNode> bfitr = branch_first_stats.iterator();
		Iterator<ASTNode> blitr = branch_last_stats.iterator();
		Iterator<LinkedList<ASTNode>> btitr = branch_first_to_last.iterator();
		while (bfitr.hasNext()) {
			ASTNode bfast = bfitr.next();
			ASTNode blast = blitr.next();
			List<ASTNode> bts = btitr.next();
			SwitchAndPrepareMergeInBranch(all_in_control, bfast, blast, bts, clear, just_one_branch);
		}
	}

	private void HandleMerge(ASTNode all_in_control) {
		List<IRForOneInstruction> ops = new LinkedList<IRForOneInstruction>();
		Map<IJavaElement, List<NodeConnectionDetailPair>> merge = node_to_merge.remove(all_in_control);
		Iterator<IJavaElement> itr = merge.keySet().iterator();
		while (itr.hasNext()) {
			IJavaElement ije = itr.next();
			IRForOneInstruction irfop = (IRForOneInstruction) IRGeneratorHelper.CreateIRInstruction(this,
					IRForOneOperation.class, new Object[] { irc, ije, IRMeta.BranchOver, DefaultINodeTask.class });
			ops.add(irfop);
			List<NodeConnectionDetailPair> merge_list = merge.get(ije);
			MergeListParallelToOne(merge_list, ije, irfop);
		}
		IRGeneratorHelper.HandleEachElementInSameOperationDependency(ops);
		merge.clear();
	}

	private void PostVisitBranch(ASTNode all_in_control, boolean clear) {
		IRTreeForOneControlElement control_ir = irc.GetControlLogicHolderElementIR();
		control_ir.ExitOneLogicBlock(all_in_control);
		PopBranchInstructionOrder();
		switch_record.remove(all_in_control);
		HandleMerge(all_in_control);
		if (clear) {
			StatementOverHandle();
		}
	}

	@Override
	public boolean visit(IfStatement node) {
		boolean just_one_branch = false;
		LinkedList<ASTNode> nslist = new LinkedList<ASTNode>();
		LinkedList<ASTNode> nelist = new LinkedList<ASTNode>();
		LinkedList<LinkedList<ASTNode>> ntlist = new LinkedList<LinkedList<ASTNode>>();
		if (node.getThenStatement() != null) {
			nslist.add(node.getThenStatement());
			nelist.add(node.getThenStatement());
			LinkedList<ASTNode> to = new LinkedList<ASTNode>();
			ntlist.add(to);
			to.add(node.getThenStatement());
		} else {
			just_one_branch = true;
		}
		if (node.getElseStatement() != null) {
			nslist.add(node.getElseStatement());
			nelist.add(node.getElseStatement());
			LinkedList<ASTNode> to = new LinkedList<ASTNode>();
			ntlist.add(to);
			to.add(node.getElseStatement());
		} else {
			just_one_branch = true;
		}
		PreVisitBranch(node, IRMeta.If, node.getExpression(), nslist, nelist, ntlist, true, just_one_branch);
		// Statement thenstat = node.getThenStatement();
		// if (thenstat != null)
		// {
		// ast_block_bind.put(thenstat, new HashSet<IBinding>());
		// post_visit_task.put(thenstat, new Runnable() {
		// @Override
		// public void run() {
		// IRGeneratorHelper.GenerateNoVariableBindingIR(thenstat, thenstat,
		// irc, ast_block_bind.get(thenstat), IRMeta.IfThen);
		// ast_block_bind.remove(thenstat);
		// }
		// });
		// }
		//
		// Statement elsestat = node.getElseStatement();
		// if (elsestat != null)
		// {
		// ast_block_bind.put(elsestat, new HashSet<IBinding>());
		// post_visit_task.put(elsestat, new Runnable() {
		// @Override
		// public void run() {
		// IRGeneratorHelper.GenerateNoVariableBindingIR(elsestat, elsestat,
		// irc, ast_block_bind.get(elsestat), IRMeta.IfElse);
		// ast_block_bind.remove(elsestat);
		// }
		// });
		// }

		return super.visit(node);
	}

	@Override
	public void endVisit(IfStatement node) {
		PostVisitBranch(node, true);
		super.endVisit(node);
	}

	// highly related to IfStatement.
	@Override
	public boolean visit(ConditionalExpression node) {
		LinkedList<ASTNode> nslist = new LinkedList<ASTNode>();
		nslist.add(node.getThenExpression());
		nslist.add(node.getElseExpression());
		LinkedList<ASTNode> nelist = new LinkedList<ASTNode>();
		nelist.add(node.getThenExpression());
		nelist.add(node.getElseExpression());
		LinkedList<LinkedList<ASTNode>> ntlist = new LinkedList<LinkedList<ASTNode>>();
		{
			LinkedList<ASTNode> to = new LinkedList<ASTNode>();
			ntlist.add(to);
			to.add(node.getThenExpression());
		}
		{
			LinkedList<ASTNode> to = new LinkedList<ASTNode>();
			ntlist.add(to);
			to.add(node.getElseExpression());
		}
		PreVisitBranch(node, IRMeta.ConditionExpression, node.getExpression(), nslist, nelist, ntlist, false, false);
		// IRGeneratorForOneLogicBlock this_ref = this;
		// post_visit_task.Put(node.getExpression(), new Runnable() {
		// @Override
		// public void run() {
		// IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getExpression(),
		// IRMeta.If);
		// PushBranchInstructionOrder();
		// }
		// });

		// post_visit_task.put(node.getThenExpression(), new Runnable() {
		// @Override
		// public void run() {
		// IRGeneratorHelper.GenerateGeneralIR(node.getThenExpression(),
		// node.getThenExpression(), irc, temp_statement_environment_set,
		// IRMeta.IfThen);
		// }
		// });
		//
		// post_visit_task.put(node.getElseExpression(), new Runnable() {
		// @Override
		// public void run() {
		// IRGeneratorHelper.GenerateGeneralIR(node.getElseExpression(),
		// node.getElseExpression(), irc, temp_statement_environment_set,
		// IRMeta.IfElse);
		// }
		// });

		return super.visit(node);
	}

	@Override
	public void endVisit(ConditionalExpression node) {
		PostVisitBranch(node, false);
		super.endVisit(node);
	}

	protected Stack<Set<IJavaElement>> switch_judge_members = new Stack<Set<IJavaElement>>();
	// protected Map<ASTNode, Integer> switch_case_flag = new HashMap<ASTNode,
	// Integer>();
	// protected Map<SwitchStatement, SwitchCase> last_switch_case = new
	// HashMap<SwitchStatement, SwitchCase>();

	class TwoPairList {
		List<ASTNode> branch_first_stats = new LinkedList<ASTNode>();
		List<ASTNode> branch_last_stats = new LinkedList<ASTNode>();
		LinkedList<LinkedList<ASTNode>> branch_first_to_last = new LinkedList<LinkedList<ASTNode>>();
	}

	private TwoPairList SearchForBranchBlocks(SwitchStatement node) {
		TwoPairList tpl = new TwoPairList();
		@SuppressWarnings("unchecked")
		List<Statement> stmts = node.statements();
		Iterator<Statement> sitr = stmts.iterator();
		Statement previous = null;
		while (sitr.hasNext()) {
			Statement stmt = sitr.next();
			if (stmt instanceof SwitchCase) {
				tpl.branch_first_stats.add(stmt);
				tpl.branch_first_to_last.add(new LinkedList<ASTNode>());
				if (previous != null) {
					tpl.branch_last_stats.add(previous);
				}
			}
			tpl.branch_first_to_last.getLast().add(stmt);
			previous = stmt;
		}
		if (previous != null) {
			tpl.branch_last_stats.add(previous);
		}
		if (tpl.branch_first_stats.size() != tpl.branch_last_stats.size() || tpl.branch_first_stats.size() != tpl.branch_first_to_last.size()) {
			System.err.println("What the fuck! size is different.");
			System.exit(1);
		}
		return tpl;
	}

	@Override
	public boolean visit(SwitchStatement node) {
		// switch_case_bind.push(new HashSet<IBinding>());
		// switch_case.push(new LinkedList<ASTNode>());
		//
		// post_visit_task.put(node.getExpression(), new Runnable() {
		// @Override
		// public void run() {
		// IRGeneratorHelper.GenerateGeneralIR(node, node.getExpression(), irc,
		// temp_statement_environment_set,
		// IRMeta.Switch, branchs_var_instr_order.peek());
		// }
		// });

		// node_to_merge.put(node, new HashMap<IJavaElement,
		// List<IRForOneInstruction>>());
		// IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.Put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				switch_judge_members.push(new HashSet<IJavaElement>(CurrentElements()));
				// IRGeneratorHelper.GenerateGeneralIR(this_ref, node,
				// IRMeta.Switch);
				// PushBranchInstructionOrder();
				// switch_record.put(node, irc.CopyEnvironment());
				// StatementOverHandle();
			}
		});
		TwoPairList tpl = SearchForBranchBlocks(node);
		PreVisitBranch(node, IRMeta.Switch, node.getExpression(), tpl.branch_first_stats, tpl.branch_last_stats, tpl.branch_first_to_last, true,
				false);
		return super.visit(node);
	}

	// private void PopSwitchBranch(SwitchStatement node) {
	// // branch flag
	// Integer flag = switch_case_flag.get(node);
	// if (flag == null) {
	// flag = 0;
	// }
	// flag = (flag + 1) % 2;
	// switch_case_flag.put(node, flag);
	// if (flag == 0) {
	// PopBranchInstructionOrder();
	// }
	// }
	//
	// private void ClearSwitchEnvironment(SwitchStatement node) {
	// switch_case_flag.remove(node);
	// switch_judge_members.pop();
	// switch_record.remove(node);
	// last_switch_case.remove(node);
	// node_to_merge.get(node).clear();
	// node_to_merge.remove(node);
	// }
	//
	// private void HandleLastSwitchCase(SwitchStatement switch_statement,
	// SwitchCase node) {
	// SwitchCase sc = last_switch_case.get(switch_statement);
	// if (sc == null) {
	// if (node != null) {
	// last_switch_case.put(switch_statement, node);
	// }
	// } else {
	// HashSet<IJavaElement> bind_elements = ast_block_bind.get(sc);
	// Map<IJavaElement, List<IRForOneInstruction>> merge =
	// node_to_merge.get(switch_statement);
	// PrepareCurrentEnvironmentToMerge(bind_elements, merge);
	//
	// ast_block_bind.remove(sc);
	// if (node != null) {
	// last_switch_case.put(switch_statement, node);
	// ast_block_bind.put(node, new HashSet<IJavaElement>());
	// }
	// }
	// }
	//
	// closely related expressions.
	@Override
	public boolean visit(SwitchCase node) {
		// HashSet<IBinding> binds = switch_case_bind.peek();
		// LinkedList<ASTNode> slist = switch_case.peek();
		// if (slist.size() > 0) {
		// ASTNode sc = slist.get(0);
		// IRGeneratorHelper.GenerateSwitchCaseIR(node, sc, irc, binds);
		//
		// slist.removeFirst();
		// binds.clear();
		// }
		// slist.add(node);

		IRGeneratorForOneLogicBlock this_ref = this;
		// SwitchStatement switch_statement = (SwitchStatement)
		// node.getParent();
		//
		// // handle previous switch_case.
		// HandleLastSwitchCase(switch_statement, node);
		//
		// // switch to direction.
		// Map<IJavaElement, IRForOneInstruction> env =
		// switch_record.get(switch_statement);
		// Set<IJavaElement> ekys = env.keySet();
		// Iterator<IJavaElement> eitr = ekys.iterator();
		// while (eitr.hasNext()) {
		// IJavaElement ije = eitr.next();
		// irc.SwitchDirection(ije, env.get(ije));
		// }
		//
		// PopSwitchBranch(switch_statement);
		Expression expr = node.getExpression();
		if (expr != null) {
			post_visit_task.Put(expr, new Runnable() {
				@Override
				public void run() {
					IRGeneratorHelper.GenerateGeneralIR(this_ref, node, IRMeta.Switch_Case_Cause);
					StatementOverHandle();
				}
			});
		} else {
			Set<IJavaElement> members = switch_judge_members.peek();
			IRGeneratorHelper.GenerateNoVariableBindingIR(this_ref, node, members, IRMeta.Switch_Case_Default);
			StatementOverHandle();
		}
		return super.visit(node);
	}
	//
	// @Override
	// public void endVisit(SwitchCase node) {
	// // IRGeneratorHelper.GenerateGeneralIR(node, node.getExpression(), irc,
	// // temp_statement_environment_set,
	// // IRMeta.Switch_Case_Cause);
	// // super.endVisit(node);
	// }

	@Override
	public void endVisit(SwitchStatement node) {
		// HashSet<IBinding> binds = switch_case_bind.pop();
		// LinkedList<ASTNode> slist = switch_case.pop();
		// if (slist.size() > 0) {
		// ASTNode sc = switch_case.peek().get(0);
		// IRGeneratorHelper.GenerateSwitchCaseIR(node, sc, irc, binds);
		// }
		//
		// binds.clear();
		// slist.clear();

		// HandleLastSwitchCase(node, null);
		//
		// HandleMerge(node);
		//
		// PopSwitchBranch(node);
		// ClearSwitchEnvironment(node);
		// StatementOverHandle();
		PostVisitBranch(node, true);
		switch_judge_members.pop();
	}

	// loop statements begin.
	@Override
	public boolean visit(WhileStatement node) {
		// ast_block_bind.put(node, new HashSet<IBinding>());
		List<ASTNode> nslist = new LinkedList<ASTNode>();
		nslist.add(node.getBody());
		List<ASTNode> nelist = new LinkedList<ASTNode>();
		nelist.add(node.getBody());
		LinkedList<LinkedList<ASTNode>> ntlist = new LinkedList<LinkedList<ASTNode>>();
		LinkedList<ASTNode> to = new LinkedList<ASTNode>();
		to.add(node.getBody());
		ntlist.add(to);
		PreVisitBranch(node, IRMeta.While, node.getExpression(), nslist, nelist, ntlist, true, true);
		// IRGeneratorForOneLogicBlock this_ref = this;
		// post_visit_task.Put(node.getExpression(), new Runnable() {
		// @Override
		// public void run() {
		// IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getExpression(),
		// IRMeta.While);
		// PushBranchInstructionOrder(GetBranchInstructions());
		// StatementOverHandle();
		// }
		// });
		return super.visit(node);
	}

	@Override
	public void endVisit(WhileStatement node) {
		PostVisitBranch(node, true);
		// PopBranchInstructionOrder();
		super.endVisit(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		// ast_block_bind.put(node, new HashSet<IBinding>());
		List<ASTNode> nslist = new LinkedList<ASTNode>();
		nslist.add(node.getBody());
		List<ASTNode> nelist = new LinkedList<ASTNode>();
		nelist.add(node.getBody());
		LinkedList<LinkedList<ASTNode>> ntlist = new LinkedList<LinkedList<ASTNode>>();
		LinkedList<ASTNode> to = new LinkedList<ASTNode>();
		to.add(node.getBody());
		ntlist.add(to);
		PreVisitBranch(node, IRMeta.While, node.getExpression(), nslist, nelist, ntlist, true, true);
		// IRGeneratorForOneLogicBlock this_ref = this;
		// post_visit_task.Put(node.getExpression(), new Runnable() {
		// @Override
		// public void run() {
		// IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getExpression(),
		// IRMeta.DoWhile);
		// PushBranchInstructionOrder(GetBranchInstructions());
		// StatementOverHandle();
		// }
		// });
		return super.visit(node);
	}

	@Override
	public void endVisit(DoStatement node) {
		// PopBranchInstructionOrder();
		PostVisitBranch(node, true);
		super.endVisit(node);
	}

	private void RememberExpressionList(List<Expression> exp_list) {
		for (Expression exp : exp_list) {
			node_element_memory.put(exp, null);
		}
	}

	private void ForgetExpressionList(List<Expression> exp_list) {
		for (Expression exp : exp_list) {
			node_element_memory.remove(exp);
		}
	}

	private Set<IJavaElement> GetAllElementsFromExpressionList(List<Expression> exp_list) {
		Set<IJavaElement> ele_set = new HashSet<IJavaElement>();
		Iterator<Expression> eitr = exp_list.iterator();
		while (eitr.hasNext()) {
			Expression expr = eitr.next();
			Set<IJavaElement> one_ele_set = SearchAndRememberAllElementsInASTNodeInJustEnvironment(expr);
			if (one_ele_set != null) {
				ele_set.addAll(one_ele_set);
			}
		}
		return ele_set;
	}

	@Override
	public boolean visit(ForStatement node) {
		// ast_block_bind.put(node, new HashSet<IBinding>());
		IRGeneratorForOneLogicBlock this_ref = this;
		Expression last_expr = null;
		@SuppressWarnings("unchecked")
		List<Expression> ini_list = node.initializers();
		if (ini_list != null) {
			last_expr = ini_list.get(ini_list.size() - 1);
			final Expression temp_last_expr = last_expr;
			// final ASTNode exp = last_expr;
			RememberExpressionList(ini_list);
			if (last_expr != null) {
				post_visit_task.Put(last_expr, new Runnable() {
					@Override
					public void run() {
						// HashSet<IJavaElement> temp =
						// this_ref.temp_statement_environment_set;
						// Set<IJavaElement> ele_set =
						// SearchAndRememberAllElementsInASTNodeInJustEnvironment(
						// temp_last_expr);

						Set<IJavaElement> ele_set = GetAllElementsFromExpressionList(ini_list);

						// this_ref.temp_statement_expression_environment_set
						// ele_set,
						IRGeneratorHelper.GenerateGeneralIR(this_ref, ele_set, temp_last_expr, IRMeta.For_Initial);

						ExpressionOverHandle();
						ForgetExpressionList(ini_list);
						// this_ref.temp_statement_environment_set = temp;
					}
				});
			}
		}
		Expression expr = node.getExpression();
		if (expr != null) {
			last_expr = expr;
			final Expression temp_last_expr = last_expr;
			node_element_memory.put(last_expr, null);
			if (last_expr != null) {
				post_visit_task.Put(last_expr, new Runnable() {
					@Override
					public void run() {
						// HashSet<IJavaElement> temp =
						// this_ref.temp_statement_environment_set;
						// this_ref.temp_statement_environment_set =
						// SearchAndRememberAllElementsInASTNodeInJustEnvironment(
						// temp_last_expr);
						Set<IJavaElement> ele_set = SearchAndRememberAllElementsInASTNodeInJustEnvironment(
								temp_last_expr);
						// this_ref.temp_statement_expression_environment_set;

						IRGeneratorHelper.GenerateGeneralIR(this_ref, ele_set, temp_last_expr, IRMeta.For_Judge);
						// PushBranchInstructionOrder(GetBranchInstructions());

						ExpressionOverHandle();
						node_element_memory.remove(temp_last_expr);
						// this_ref.temp_statement_environment_set = temp;
					}
				});
			}
		}
		@SuppressWarnings("unchecked")
		List<Expression> upd_list = node.updaters();
		if (upd_list != null) {
			last_expr = upd_list.get(upd_list.size() - 1);
			final Expression temp_last_expr = last_expr;
			RememberExpressionList(upd_list);
			if (last_expr != null) {
				post_visit_task.Put(last_expr, new Runnable() {
					@Override
					public void run() {
						// HashSet<IJavaElement> temp =
						// this_ref.temp_statement_environment_set;
						// this_ref.temp_statement_environment_set =
						// SearchAndRememberAllElementsInASTNodeInJustEnvironment(
						// temp_last_expr);

						// Set<IJavaElement> ele_set =
						// SearchAndRememberAllElementsInASTNodeInJustEnvironment(
						// temp_last_expr);
						Set<IJavaElement> ele_set = GetAllElementsFromExpressionList(upd_list);

						// this_ref.temp_statement_expression_environment_set;

						IRGeneratorHelper.GenerateGeneralIR(this_ref, ele_set, temp_last_expr, IRMeta.For_Update);

						ExpressionOverHandle();
						ForgetExpressionList(upd_list);
						// this_ref.temp_statement_environment_set = temp;
					}
				});
			}
		}

		List<ASTNode> nslist = new LinkedList<ASTNode>();
		nslist.add(node.getBody());
		List<ASTNode> nelist = new LinkedList<ASTNode>();
		nelist.add(node.getBody());
		LinkedList<LinkedList<ASTNode>> ntlist = new LinkedList<LinkedList<ASTNode>>();
		LinkedList<ASTNode> to = new LinkedList<ASTNode>();
		to.add(node.getBody());
		ntlist.add(to);
		// here: last_expr could be used because the judge-node-info are nearly not used.
		PreVisitBranch(node, IRMeta.For, last_expr, nslist, nelist, ntlist, true, true);
		return super.visit(node);
	}

	@Override
	public void endVisit(ForStatement node) {
		PostVisitBranch(node, true);
		// Expression expr = node.getExpression();
		// if (expr != null) {
		// PopBranchInstructionOrder();
		// }
		// StatementOverHandle();
		super.endVisit(node);
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		List<ASTNode> nslist = new LinkedList<ASTNode>();
		nslist.add(node.getBody());
		List<ASTNode> nelist = new LinkedList<ASTNode>();
		nelist.add(node.getBody());
		LinkedList<LinkedList<ASTNode>> ntlist = new LinkedList<LinkedList<ASTNode>>();
		LinkedList<ASTNode> to = new LinkedList<ASTNode>();
		to.add(node.getBody());
		ntlist.add(to);
		PreVisitBranch(node, IRMeta.EnhancedFor, node.getExpression(), nslist, nelist, ntlist, true, true);
		// IRGeneratorForOneLogicBlock this_ref = this;
		// post_visit_task.Put(node.getExpression(), new Runnable() {
		// @Override
		// public void run() {
		// IRGeneratorHelper.GenerateGeneralIR(this_ref, node,
		// IRMeta.EnhancedFor);
		// StatementOverHandle();
		// }
		// });
		return super.visit(node);
	}

	@Override
	public void endVisit(EnhancedForStatement node) {
		PostVisitBranch(node, true);
		super.endVisit(node);
	}

	// branches & loops statements end.

	// Solved. missed to consider the label and will be considered in the
	// future.

	private void HandleBreakContinueStatement(ASTNode node, SimpleName label, String code) {
		// ASTNode n = ASTSearch.FindMostCloseLoopNode(node);
		ASTNode break_scope = SearchForLiveScopeOfBreakContinue(node, label);
		// n != null && ast_block_bind.containsKey(n)
		if (break_scope != null) {
			Set<IJavaElement> elements = node_element_stack.GetIJavaElementsFromTopDownToSpecified(break_scope);
			// HashSet<IJavaElement> elements = ast_block_bind.get(n);
			Map<IJavaElement, IRForOneInstruction> eles = irc.CopyEnvironment(elements);
			IRGeneratorForOneLogicBlock this_ref = this;
			post_visit_task.Put(break_scope, new Runnable() {
				@Override
				public void run() {
					IRGeneratorHelper.GenerateNoVariableBindingIR(this_ref, node, elements, code);
					Set<IJavaElement> keys = eles.keySet();
					Iterator<IJavaElement> kitr = keys.iterator();
					while (kitr.hasNext()) {
						IJavaElement ije = kitr.next();
						IRForOneInstruction ir_instr = eles.get(ije);
						IRGeneratorForOneProject.GetInstance().RegistConnection(
								new StaticConnection(ir_instr, irc.GetLastIRTreeNode(ije), new ConnectionInfo(EdgeBaseType.Self.Value())));
					}
				}
			});
		}
	}

	@Override
	public boolean visit(BreakStatement node) {
		HandleBreakContinueStatement(node, node.getLabel(), IRMeta.Break);
		return false;
	}

	@Override
	public boolean visit(ContinueStatement node) {
		HandleBreakContinueStatement(node, node.getLabel(), IRMeta.Continue);
		return false;
	}

	private Map<String, ASTNode> label_scope = new TreeMap<String, ASTNode>();

	@Override
	public boolean visit(LabeledStatement node) {
		// will do in the future. The current structure does not recognize such
		// minor but close relation.
		SimpleName label = node.getLabel();
		if (label != null) {
			label_scope.put(label.toString(), node.getBody());
		}
		return super.visit(node);
	}

	@Override
	public void endVisit(LabeledStatement node) {
		SimpleName label = node.getLabel();
		if (label != null) {
			label_scope.remove(label.toString());
		}
		super.endVisit(node);
	}

	private ASTNode SearchForLiveScopeOfBreakContinue(ASTNode node, SimpleName label) {
		if (label != null) {
			return label_scope.get(label.toString());
		} else {
			return ASTSearch.FindMostCloseLoopNode(node);
		}
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		super.Validation(node);
		// no need to do anything, all things are in
		// VariableDeclarationFragment.
		return super.visit(node);
	}

	// closely related expressions stumbled into.
	@Override
	public boolean visit(VariableDeclarationExpression node) {
		super.Validation(node);
		// no need to do anything, all things are in
		// VariableDeclarationFragment.
		return super.visit(node);
	}

	private void HandleAssign(Expression left, Expression right) {
		if (right == null) {
			return;
		}
		// Set<IJavaElement> temp_copy = new
		// HashSet<IJavaElement>(temp_statement_environment_set);
		// temp_statement_environment_set.clear();
		node_element_memory.put(right, null);
		preVisit(right);
		right.accept(this);
		postVisit(right);
		Map<IJavaElement, IRForOneInstruction> env = irc
				.CopyEnvironment(SearchAndRememberAllElementsInASTNodeInJustEnvironment(right));
		node_element_memory.remove(right);
		// temp_copy.addAll(temp_statement_environment_set);

		StatementOverHandle();

		node_element_memory.put(left, null);
		preVisit(left);
		left.accept(this);
		postVisit(left);
		Set<IJavaElement> left_ijes = SearchAndRememberAllElementsInASTNodeInJustEnvironment(left);
		node_element_memory.remove(left);
		IRGeneratorHelper.GenerateGeneralIR(this, left_ijes, left, IRMeta.LeftHandAssign, SkipSelfTask.class);

		// System.err.println("============= start =============");
		// System.err.println(temp_statement_environment_set);
		// System.err.println("============= over =============");
		Iterator<IJavaElement> itr = left_ijes.iterator();
		while (itr.hasNext()) {
			IJavaElement t_ije = itr.next();
			IRForOneInstruction last_node = irc.GetLastIRTreeNode(t_ije);
			last_node.SetRequireType(EdgeBaseType.Self.Value());

			Set<IJavaElement> ekeys = env.keySet();
			Iterator<IJavaElement> eitr = ekeys.iterator();
			while (eitr.hasNext()) {
				IJavaElement e_ije = eitr.next();
				if (e_ije != t_ije) {
					IRForOneInstruction ir_instr = env.get(e_ije);
					IRGeneratorForOneProject.GetInstance().RegistConnection(
							new StaticConnection(ir_instr, last_node, new ConnectionInfo(EdgeBaseType.Sequential.Value())));
				}
			}
		}

		IJavaElement ije = WholeExpressionIsAnElement(right);
		if (ije != null) {
			IRForOneInstruction last = irc.GetLastIRTreeNode(ije);
			last.SetRequireType(EdgeBaseType.Self.Value());
			// irc.AddAssignDependency(ije, new
			// HashSet<IJavaElement>(env.keySet()));
		}

		// temp_statement_environment_set.addAll(temp_copy);
		// StatementOverHandle();
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		// Solved. how to redirect? same as assignment.
		HandleAssign(node.getName(), node.getInitializer());
		// IRGeneratorForOneLogicBlock this_ref = this;
		// post_visit_task.put(node.getName(), new Runnable() {
		// @Override
		// public void run() {
		// IRGeneratorHelper.GenerateGeneralIR(this_ref, node,
		// IRMeta.VariabledDeclare);
		// StatementOverHandle();
		// }
		// });
		return false;
	}

	@Override
	public void endVisit(VariableDeclarationFragment node) {
		// StatementOverHandle();
		super.endVisit(node);
	}
	
	@Override
	public boolean visit(FieldDeclaration node) {
		super.Validation(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		super.Validation(node);
		// Solved. how to redirect? same as assignment.
		HandleAssign(node.getName(), node.getInitializer());
		// IRGeneratorForOneLogicBlock this_ref = this;
		// post_visit_task.put(node.getName(), new Runnable() {
		// @Override
		// public void run() {
		// IRGeneratorHelper.GenerateGeneralIR(this_ref, node,
		// IRMeta.VariabledDeclare);
		// StatementOverHandle();
		// }
		// });
		return false;
	}

	@Override
	public void endVisit(SingleVariableDeclaration node) {
		// StatementOverHandle();
		super.endVisit(node);
	}

	@Override
	public boolean visit(TryStatement node) {
		// no need to do anything.
		return super.visit(node);
	}

	@Override
	public boolean visit(Block node) {
		// no need to do anything.
		return super.visit(node);
	}

	@Override
	public boolean visit(EmptyStatement node) {
		// no need to do anything.
		return super.visit(node);
	}

	@Override
	public boolean visit(ReturnStatement node) {
		Expression expr = node.getExpression();
		if (expr != null) {
			// pre_visit_task.put(expr, new Runnable() {
			// @Override
			// public void run() {
			// RecordASTNodePreEnvironment(expr);
			// }
			// });
			post_visit_task.Put(expr, new Runnable() {
				@Override
				public void run() {
					IJavaElement ije = WholeExpressionIsAnElement(expr);
					if (ije != null) {
						Iterator<IJavaElement> titr = CurrentElements().iterator();
						while (titr.hasNext()) {
							IJavaElement t_ije = titr.next();
							IRForOneInstruction iru = irc.GetLastIRTreeNode(t_ije);
							if (iru != null) {
								iru.SetAcceptType(EdgeBaseType.Self.Value());
								iru.SetOutConnectionMergeTask(new RequireHandleTask(iru));
							}
						}
					}
				}
			});
		}
		// CompareASTNodePreEnvironmentToJudgeIfDirectTransfer
		return super.visit(node);
	}

	@Override
	public void endVisit(ReturnStatement node) {
		// IRGeneratorHelper.GenerateGeneralIR(this, node, IRMeta.Return);
		Iterator<IJavaElement> titr = CurrentElements().iterator();
		while (titr.hasNext()) {
			IJavaElement ije = titr.next();
			IRForOneInstruction iru = irc.GetLastIRTreeNode(ije);
			if (iru != null) {
				irc.PutOutNodes(ije, iru);
			}
		}
	}

	// need to handle data_dependency.
	@Override
	public boolean visit(Assignment node) {
		// Solved. how to redirect? last node to skip self.

		// Solved. depd needs to record which iirnode left value depends.
		// connections need to be added from left to right.
		// Solved. assign dependency should be extracted as a stand_alone
		// function because var-declare also will also use it.

		HandleAssign(node.getLeftHandSide(), node.getRightHandSide());

		// Expression right_val = node.getRightHandSide();
		// right_val.accept(this);
		// IJavaElement right_jele = WholeExpressionIsAnElement(right_val);
		// if (right_jele != null)
		// {
		// IRForOneInstruction iru = irc.GetLastIRTreeNode(right_jele);
		// iru.SetAcceptType(EdgeBaseType.Self.getType());
		// }
		// HashSet<IJavaElement> depd = new
		// HashSet<IJavaElement>(temp_statement_environment_set);
		// StatementOverHandle();
		// Expression left_val = node.getLeftHandSide();
		// left_val.accept(this);
		// IRGeneratorHelper.GenerateGeneralIR(this, node,
		// IRMeta.LeftHandAssign);
		// IJavaElement left_jele = WholeExpressionIsAnElement(left_val);
		// if (left_jele != null) {
		// IRForOneInstruction iru = irc.GetLastIRTreeNode(left_jele);
		// iru.SetRequireType(EdgeBaseType.Self.getType());
		// iru.SetOutConnectionMergeTask(new SkipSelfTask(iru));
		// } else {
		// // add assign dependency.
		// Iterator<IJavaElement> titr =
		// temp_statement_environment_set.iterator();
		// while (titr.hasNext()) {
		// IJavaElement ijele = titr.next();
		// irc.AddAssignDependency(ijele, new HashSet<IJavaElement>(depd));
		// }
		// }
		// post_visit_task.put(node.getRightHandSide(), new Runnable() {
		// @Override
		// public void run() {
		// IRGeneratorHelper.GenerateGeneralIR(node, node.getRightHandSide(),
		// irc, temp_statement_environment_set,
		// IRMeta.RightHandAssign, branchs_var_instr_order.peek());
		// }
		// });
		return false;
	}

	@Override
	public void endVisit(Assignment node) {
		StatementOverHandle();
		super.endVisit(node);
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.Put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node, IRMeta.Synchronized);
				StatementOverHandle();
			}
		});
		return super.visit(node);
	}

	// handling expressions.

	private IJavaElementState HandleBinding(IBinding ib, ASTNode happen) {
		if (!BindingManager.QualifiedBinding(ib)) {
			return IJavaElementState.HandledWrong;
		}
		IJavaElement jele = ib.getJavaElement();
		return HandleIJavaElement(jele, happen);
	}

	private IJavaElementState HandleIJavaElement(IJavaElement jele, ASTNode happen) {
		// handle loop_bind, just for no variable bind statements such as
		// break and continue.
		// if (!BindingManager.QualifiedBinding(ib)) {
		// return;
		// }
		// IJavaElement jele = ib.getJavaElement();
		if (jele == null) {
			return IJavaElementState.HandledWrong;
		}

		// Solved. this judgment should be implemented in each ASTNode such as
		// FieldAccess, etc.
		// Iterator<IJavaElement> ije_itr = CurrentElements().iterator();
		// while (ije_itr.hasNext()) {
		// IJavaElement ije = ije_itr.next();
		// ASTNode node = all_happen.get(ije);
		// if (ASTSearch.ASTNodeContainsAnASTNode(node, happen)) {
		// return IJavaElementState.NoNeedToHandle;
		// }
		// }

		if (jele instanceof IMember) {
			IMember im = (IMember) jele;
			IType dec_type = im.getDeclaringType();
			if (im instanceof IType) {
				dec_type = (IType) im;
			}
			if (dec_type == null) {
				System.err.println(
						"Strange! the declared type is null. Strange im is:" + im + ";Strange IJavaElement is:" + jele);
				System.exit(1);
			}
			if (dec_type.isBinary()) {
				return IJavaElementState.HandledWrong;
			}
		} else {
			if (!(jele instanceof ILocalVariable) && !(jele instanceof VirtualDefinedElement)) {
				// && !(jele instanceof UnresolvedTypeElement) && !(jele instanceof ConstantUniqueElement)
				return IJavaElementState.HandledWrong;
			}
		}
		irc.GetIRTreeForOneElement(jele);
		
		// Solved. ast_block_bind be replaced by merge of stack or memory if convenient.
//		Set<ASTNode> ks = ast_block_bind.keySet();
//		Iterator<ASTNode> kitr = ks.iterator();
//		while (kitr.hasNext()) {
//			ASTNode an = kitr.next();
//			HashSet<IJavaElement> set = ast_block_bind.get(an);
//			set.add(jele);
//		}

		// handle switch_case_bind
		// if (!switch_case_bind.isEmpty()) {
		// switch_case_bind.peek().add(ib);
		// }

		// next isolated tasks.
		// all_count.put(jele, -1);
		all_happen.put(jele, happen);
		// temp_statement_environment_set.add(jele);
		// temp_statement_expression_environment_set.add(jele);
		CurrentElements().add(jele);
		return IJavaElementState.HandledSuccessful;
	}

	@Override
	public boolean visit(SimpleName node) {
		IBinding ib = node.resolveBinding();
		// IJavaElementState state = null;
		IJavaElementState bind_state = HandleBinding(ib, node);
		if (bind_state == IJavaElementState.HandledWrong) {
			HandleIJavaElement(new UnresolvedNameOrFieldAccessElement(node.toString()), node);
		}
//		handle_binding_state.put(node, state);
		return super.visit(node);
	}
	
//	@Override
//	public void endVisit(SimpleName node) {
//		IJavaElementState state = handle_binding_state.remove(node);
//		if (state == IJavaElementState.HandledWrong) {
//			HandleIJavaElement(new UnresolvedSimpleNameElement(node.toString()), node);
//		}
//		super.endVisit(node);
//	}
	
	Map<ASTNode, IJavaElementState> handle_binding_state = new HashMap<ASTNode, IJavaElementState>();
	
	@Override
	public boolean visit(QualifiedName node) {
		IBinding ib = node.resolveBinding();
		IJavaElementState state = HandleBinding(ib, node);
		// handle_binding_state.put(node, state);
		// state == IJavaElementState.HandledSuccessful || state == IJavaElementState.NoNeedToHandle
		if (state == IJavaElementState.HandledWrong) {
			HandleIJavaElement(new UnresolvedNameOrFieldAccessElement(node.toString()), node);
		}
		// HandleType(node.resolveBinding(), node.toString(), node);
		super.visit(node);
		return false;
	}

//	@Override
//	public void endVisit(QualifiedName node) {
//		IJavaElementState state = handle_binding_state.remove(node);
//		if (state == IJavaElementState.HandledWrong) {
//			HandleIJavaElement(new UnresolvedNameOrFieldAccessElement(node.toString()), node);
//			// IRGeneratorHelper.GenerateGeneralIR(this, node, IRMeta.QualifiedName + node.getName().toString());
//		}
//		super.endVisit(node);
//	}

	@Override
	public boolean visit(FieldAccess node) {
		IVariableBinding ib = node.resolveFieldBinding();
		IJavaElementState state = HandleBinding(ib, node);
		handle_binding_state.put(node, state);
		if (state == IJavaElementState.HandledSuccessful || state == IJavaElementState.NoNeedToHandle) {
			return false;
		}
//		if (ib != null && ib.getJavaElement() != null) {
//			HandleBinding(ib, node);
//			return false;
//		}
		return super.visit(node);
	}

	@Override
	public void endVisit(FieldAccess node) {
//		IVariableBinding ib = node.resolveFieldBinding();
//		if (ib == null || ib.getJavaElement() == null) {
		IJavaElementState state = handle_binding_state.remove(node);
		if (state == IJavaElementState.HandledWrong) {
			// HandleIJavaElement(new UnresolvedNameOrFieldAccessElement(node.toString()), node);
			IRGeneratorHelper.GenerateGeneralIR(this, node, IRMeta.FieldAccess + node.getName().toString());
		}
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		IVariableBinding ib = node.resolveFieldBinding();
		IJavaElementState state = HandleBinding(ib, node);
		handle_binding_state.put(node, state);
		if (state == IJavaElementState.HandledSuccessful || state == IJavaElementState.NoNeedToHandle) {
			return false;
		}
//		if (ib != null && ib.getJavaElement() != null) {
//			HandleBinding(ib, node);
//			return false;
//		}
		return super.visit(node);
	}

	@Override
	public void endVisit(SuperFieldAccess node) {
//		IVariableBinding ib = node.resolveFieldBinding();
//		if (ib == null || ib.getJavaElement() == null) {
		IJavaElementState state = handle_binding_state.remove(node);
		if (state == IJavaElementState.HandledWrong) {
			TreatSuperClassElement(node);
			// HandleIJavaElement(new UnresolvedNameOrFieldAccessElement(node.toString()), node);
			IRGeneratorHelper.GenerateGeneralIR(this, node, IRMeta.FieldAccess + node.getName().toString());
		}
	}

	@Override
	public boolean visit(StringLiteral node) {
		HandleIJavaElement(IRGeneratorForOneProject.GetInstance()
				.FetchConstantUniqueElement(IRConstantMeta.StringConstant + "$" + node.getLiteralValue()), node);
		return super.visit(node);
	}

	@Override
	public boolean visit(NumberLiteral node) {
		HandleIJavaElement(IRGeneratorForOneProject.GetInstance()
				.FetchConstantUniqueElement(IRConstantMeta.NumberConstant + "$" + node.toString()), node);
		return super.visit(node);
	}

	@Override
	public boolean visit(NullLiteral node) {
		HandleIJavaElement(IRGeneratorForOneProject.GetInstance()
				.FetchConstantUniqueElement(IRConstantMeta.NullConstant + "$" + node.toString()), node);
		return super.visit(node);
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		HandleIJavaElement(IRGeneratorForOneProject.GetInstance()
				.FetchConstantUniqueElement(IRConstantMeta.CharConstant + "$" + node.toString()), node);
		return super.visit(node);
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		HandleIJavaElement(IRGeneratorForOneProject.GetInstance()
				.FetchConstantUniqueElement(IRConstantMeta.BooleanConstant + "$" + node.toString()), node);
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeLiteral node) {
		ITypeBinding itb = node.resolveTypeBinding();
		HandleType(itb, IRConstantMeta.TypeConstant + "$" + node.toString(), node);
		// IJavaElementState source_resolved = HandleBinding(itb, node);
		// if (source_resolved == IJavaElementState.HandledWrong) {
		// HandleIJavaElement(IRGeneratorForOneProject.GetInstance()
		// .FetchConstantUniqueElement(IRConstantMeta.TypeConstant + "$" +
		// node.toString()), node);
		// }
		return super.visit(node);
	}

	private void HandleType(IBinding ib, String represent, ASTNode happen) {
		IJavaElementState source_resolved = HandleBinding(ib, happen);
		if (source_resolved == IJavaElementState.HandledWrong) {
			UnresolvedTypeElement ele = IRGeneratorForOneProject.GetInstance().FetchUnresolvedTypeElement(represent);
			HandleIJavaElement(ele, happen);
		}
	}

	@Override
	public boolean visit(ArrayType node) {
		// HandleType(node.resolveBinding(), node.toString(), node);
		return false;
	}

	@Override
	public boolean visit(SimpleType node) {
		// HandleType(node.resolveBinding(), node.toString(), node);
		return false;
	}

	@Override
	public boolean visit(PrimitiveType node) {
		// HandleType(node.resolveBinding(), node.toString(), node);
		return false;
	}

	@Override
	public boolean visit(QualifiedType node) {
		// HandleType(node.resolveBinding(), node.toString(), node);
		return false;
	}

	@Override
	public boolean visit(NameQualifiedType node) {
		// HandleType(node.resolveBinding(), node.toString(), node);
		return false;
	}

	@Override
	public boolean visit(ParameterizedType node) {
		// it won't happen.
		return false;
	}

	@Override
	public boolean visit(WildcardType node) {
		// do not need to handle.
		return false;
	}

	@Override
	public boolean visit(UnionType node) {
		// do not need to handle.
		return false;
	}

	@Override
	public boolean visit(IntersectionType node) {
		// do not need to handle.
		return false;
	}

	@Override
	public void endVisit(PrefixExpression node) {
		IRGeneratorHelper.GenerateGeneralIR(this, node, IRMeta.Prefix + node.getOperator().toString());
	}

	@Override
	public void endVisit(PostfixExpression node) {
		IRGeneratorHelper.GenerateGeneralIR(this, node, IRMeta.Postfix + node.getOperator().toString());
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		// no need to do anything.
		return super.visit(node);
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		super.Validation(node);
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.Put(node.getLeftOperand(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getLeftOperand(), IRMeta.InstanceOfExpression);
			}
		});
		return super.visit(node);
	}

	@Override
	public void endVisit(InstanceofExpression node) {
		IRGeneratorHelper.GenerateGeneralIR(this, node.getRightOperand(), IRMeta.InstanceOfType);
		super.endVisit(node);
	}

	// Reminding: begin to handle infix expression.
	private Map<ASTNode, Map<IJavaElement, List<NodeConnectionDetailPair>>> node_to_merge = new HashMap<ASTNode, Map<IJavaElement, List<NodeConnectionDetailPair>>>();

	private void PrepareCurrentEnvironmentToMerge(Set<IJavaElement> bind_elements,
			Map<IJavaElement, List<NodeConnectionDetailPair>> merge, InfixExpressionIndexConnection conn_detail) {
		Iterator<IJavaElement> itr = bind_elements.iterator();
		while (itr.hasNext()) {
			IJavaElement ije = itr.next();
			List<NodeConnectionDetailPair> list = merge.get(ije);
			if (list == null) {
				list = new LinkedList<NodeConnectionDetailPair>();
				merge.put(ije, list);
			}
			list.add(new NodeConnectionDetailPair(irc.GetLastIRTreeNode(ije), conn_detail));
		}
	}

	// Solved. Add group to each IRForOneInstruction.
	// Infix operator:
	// ===== could swap =====
	// *  TIMES
	// +  PLUS
	// ==  EQUALS
	// !=  NOT_EQUALS
	// ^  XOR
	// |  OR
	// ||  CONDITIONAL_OR
	// &  AND
	// &&  CONDITIONAL_AND
	
//	private boolean IsInfixOperatorMetCommutativeLaw(Operator op) {
//		if (op.equals(Operator.TIMES) || op.equals(Operator.PLUS) || op.equals(Operator.EQUALS) || op.equals(Operator.NOT_EQUALS) || op.equals(Operator.XOR) || op.equals(Operator.OR) || op.equals(Operator.CONDITIONAL_OR) || op.equals(Operator.AND) || op.equals(Operator.CONDITIONAL_AND)) {
//			return true;
//		}
//		return false;
//	}
	
	// ===== could not swap =====
	// /  DIVIDE
	// %  REMAINDER
	// -  MINUS
	// <<  LEFT_SHIFT
	// >>  RIGHT_SHIFT_SIGNED
	// >>>  RIGHT_SHIFT_UNSIGNED
	// <  LESS
	// >  GREATER
	// <=  LESS_EQUALS
	// >=  GREATER_EQUALS
	
	@Override
	public boolean visit(InfixExpression node) {
		if (most_parent_infix == null) {
			most_parent_infix = node;
			instrs_under_most_parent_infix.clear();
		}

		HashMap<IJavaElement, List<NodeConnectionDetailPair>> merge = new HashMap<IJavaElement, List<NodeConnectionDetailPair>>();
		node_to_merge.put(node, merge);

		Map<IJavaElement, IRForOneInstruction> env = irc.CopyEnvironment();

		List<Expression> expr_list = new LinkedList<Expression>();
		expr_list.add(node.getLeftOperand());
		expr_list.add(node.getRightOperand());
		@SuppressWarnings("unchecked")
		List<Expression> exprs = (List<Expression>) node.extendedOperands();
		expr_list.addAll(exprs);
		
		int index = 0;
		Iterator<Expression> eitr = expr_list.iterator();
		while (eitr.hasNext()) {
			index++;
			Expression expr = eitr.next();
			pre_visit_task.Put(expr, new Runnable() {
				@Override
				public void run() {
					
					//just for debugging.
					expr.getFlags();
					
					Set<IJavaElement> ekeys = env.keySet();
					Iterator<IJavaElement> eijeitr = ekeys.iterator();
					while (eijeitr.hasNext()) {
						IJavaElement ije = eijeitr.next();
						IRForOneInstruction irtree_node = env.get(ije);
						irc.SwitchDirection(ije, irtree_node);
					}
				}
			});
			node_element_memory.put(expr, null);
			post_visit_task.Put(expr, new IndexInfoRunner(index) {
				@Override
				public void run() {
					Set<IJavaElement> all_elements = SearchAndRememberAllElementsInASTNodeInJustEnvironment(expr);
					PrepareCurrentEnvironmentToMerge(all_elements, merge, new InfixExpressionIndexConnection(expr, this.getIndex()));
					ExpressionOverHandle();
					node_element_memory.remove(expr);
				}
			});
		}

		// IRGeneratorForOneLogicBlock this_ref = this;
		// post_visit_task.Put(node.getLeftOperand(), new Runnable() {
		// @Override
		// public void run() {
		// IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getLeftOperand(),
		// IRMeta.InfixLeftExpression + node.getOperator().toString());
		// }
		// });
		return super.visit(node);
	}

	private void MergeListParallelToOne(List<NodeConnectionDetailPair> list, IJavaElement ije, IRForOneInstruction irfop) {
		Iterator<NodeConnectionDetailPair> litr = list.iterator();
		while (litr.hasNext()) {
			NodeConnectionDetailPair mcdp = litr.next();
			IRGeneratorForOneProject.GetInstance()
					.RegistConnection(new StaticConnection(mcdp.getInstruction(), irfop, 
							new ConnectionInfo(EdgeBaseType.Self.Value(), mcdp.getDetail())));
		}
		irc.SwitchDirection(ije, irfop);
	}

	@Override
	public void endVisit(InfixExpression node) {
		// IRGeneratorHelper.GenerateGeneralIR(this, node.getRightOperand(),
		// IRMeta.InfixRightExpression + node.getOperator().toString());
		Map<IJavaElement, List<NodeConnectionDetailPair>> merge = node_to_merge.get(node);
		Set<IJavaElement> mkeys = merge.keySet();
		Iterator<IJavaElement> mitr = mkeys.iterator();
		List<IRForOneInstruction> new_creation = new LinkedList<IRForOneInstruction>();
		while (mitr.hasNext()) {
			IJavaElement ije = mitr.next();
			List<NodeConnectionDetailPair> list = merge.get(ije);
			// this is the only place which generates meaningful codes but not
			// through IRGeneratorHelper.
			IRForOneInstruction irfop = (IRForOneInstruction) IRGeneratorHelper.CreateIRInstruction(this,
					IRForOneOperation.class,
					new Object[] { irc, ije, node.getOperator().toString(), DefaultINodeTask.class });
			// handle group.
			if (node.equals(most_parent_infix)) {
				Set<IRForOneInstruction> instrs = instrs_under_most_parent_infix.get(ije);
				Iterator<IRForOneInstruction> initr = instrs.iterator();
				while (initr.hasNext()) {
					IRForOneInstruction iroi = initr.next();
					iroi.SetGroup(irfop);
				}
			}
			// handle connection.
			IRGeneratorHelper.HandleSourceMethodAndBranchDependency(irc, ije, irfop,
					branch_var_instr_order.empty() ? null : branch_var_instr_order.peek(),
					source_invocation_barrier.peek(), element_has_set_branch, element_has_set_source_method_barrier);
			new_creation.add(irfop);
			MergeListParallelToOne(list, ije, irfop);
		}
		IRGeneratorHelper.HandleEachElementInSameOperationDependency(new_creation);
		// clear.
		node_to_merge.get(node).clear();
		node_to_merge.remove(node);
		if (node.equals(most_parent_infix)) {
			most_parent_infix = null;
			instrs_under_most_parent_infix.clear();
		}
		super.endVisit(node);
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		super.Validation(node);
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		super.Validation(node);
		// do not need handle it.
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		super.Validation(node);
		// HandleBinding(node.resolveVariable(), node);
		return super.visit(node);
	}
	
	@Override
	public void endVisit(EnumConstantDeclaration node) {
		super.endVisit(node);
	}

	@Override
	public boolean visit(CatchClause node) {
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.Put(node.getException(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getException(), IRMeta.CatchClause);
			}
		});
		return super.visit(node);
	}

	@Override
	public boolean visit(CastExpression node) {
		IRGeneratorForOneLogicBlock this_ref = this;
		final Type ntype = node.getType();
		post_visit_task.Put(ntype, new Runnable() {
			@Override
			public void run() {
				HandleType(ntype.resolveBinding(), ntype.toString(), ntype);
				IRGeneratorHelper.GenerateGeneralIR(this_ref, ntype, IRMeta.CastType);
			}
		});
		return super.visit(node);
	}

	@Override
	public void endVisit(CastExpression node) {
		IRGeneratorForOneLogicBlock this_ref = this;
		IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getExpression(), IRMeta.CastExpression);
	}

	@Override
	public boolean visit(ArrayCreation node) {
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.Put(node.getType(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getType(), IRMeta.ArrayCreation);
			}
		});
		@SuppressWarnings("unchecked")
		List<Expression> nlist = node.dimensions();
		Iterator<Expression> itr = nlist.iterator();
		while (itr.hasNext()) {
			Expression expr = itr.next();
			post_visit_task.Put(expr, new Runnable() {
				@Override
				public void run() {
					IRGeneratorHelper.GenerateGeneralIR(this_ref, expr, IRMeta.ArrayCreationIndex);
				}
			});
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		IRGeneratorHelper.GenerateGeneralIR(this, node, IRMeta.CastExpression);
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayAccess node) {
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.Put(node.getArray(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getArray(), IRMeta.Array);
			}
		});
		return super.visit(node);
	}

	@Override
	public void endVisit(ArrayAccess node) {
		IRGeneratorHelper.GenerateGeneralIR(this, node, IRMeta.ArrayIndex);
		super.endVisit(node);
	}

	@Override
	public boolean visit(ThisExpression node) {
		// do not need to handle.
		return super.visit(node);
	}

	private IMethod WhetherGoIntoMethodReference(IMethodBinding imb) {
		if (imb != null) {
			IJavaElement jele = imb.getJavaElement();
			if (jele != null && jele instanceof IMethod) {
				IMethod im = (IMethod) jele;
				if (!im.getDeclaringType().isBinary()) {
					return im;
				}
			}
		}
		return null;
	}

	private boolean HandleMethodReferenceStart(IMethodBinding imb, ASTNode happen) {
		IMethod im = WhetherGoIntoMethodReference(imb);
		if (im != null) {
			HandleIJavaElement(im, happen);
			return false;
		}
		return true;
	}

	private void HandleMethodReferenceEnd(IMethodBinding imb, MethodReference mr, String code) {
		IMethod im = WhetherGoIntoMethodReference(imb);
		if (im == null) {
			IRGeneratorHelper.GenerateGeneralIR(this, mr, IRMeta.MethodReference + code);
		}
	}

	@Override
	public boolean visit(ExpressionMethodReference node) {
		return HandleMethodReferenceStart(node.resolveMethodBinding(), node);
	}

	@Override
	public void endVisit(ExpressionMethodReference node) {
		HandleMethodReferenceEnd(node.resolveMethodBinding(), node, node.toString());
		super.endVisit(node);
	}

	@Override
	public boolean visit(CreationReference node) {
		return HandleMethodReferenceStart(node.resolveMethodBinding(), node);
	}

	@Override
	public void endVisit(CreationReference node) {
		HandleMethodReferenceEnd(node.resolveMethodBinding(), node, node.toString());
		super.endVisit(node);
	}

	@Override
	public boolean visit(TypeMethodReference node) {
		return HandleMethodReferenceStart(node.resolveMethodBinding(), node);
	}

	@Override
	public void endVisit(TypeMethodReference node) {
		HandleMethodReferenceEnd(node.resolveMethodBinding(), node, node.toString());
		super.endVisit(node);
	}

	@Override
	public boolean visit(SuperMethodReference node) {
		boolean continue_visit = HandleMethodReferenceStart(node.resolveMethodBinding(), node);
		return continue_visit;
	}

	@Override
	public void endVisit(SuperMethodReference node) {
		TreatSuperClassElement(node);
		HandleMethodReferenceEnd(node.resolveMethodBinding(), node, node.getName().toString());
		super.endVisit(node);
	}
	
//	private String GetSuperClass(ASTNode node) {
//		ASTNode temp_node = ASTSearch.FindMostCloseAbstractTypeDeclaration(node);
//		if (temp_node instanceof TypeDeclaration) {
//			TypeDeclaration td = (TypeDeclaration) temp_node;
//			Type tp = td.getSuperclassType();
//			return tp.toString();
//		}
//		return "UnKnown#";
//	}
	
	private boolean TreatSuperClassElement(ASTNode node) {
		ASTNode temp_node = ASTSearch.FindMostCloseAbstractTypeDeclaration(node);
		if (temp_node instanceof TypeDeclaration) {
			boolean source_kind = false;
			TypeDeclaration td = (TypeDeclaration) temp_node;
			Type tp = td.getSuperclassType();
			ITypeBinding itb = tp.resolveBinding();
			IType it = null;
			if (itb != null) {
				IJavaElement ijele = itb.getJavaElement();
				if (ijele != null && ijele instanceof IType) {
					it = (IType) ijele;
					if (!it.isBinary()) {
						source_kind = true;
					}
				}
			}
			if (!source_kind) {
				HandleIJavaElement(
						IRGeneratorForOneProject.GetInstance().FetchUnresolvedTypeElement(td.getName().toString()),
						node);
				return true;
			} else {
				HandleIJavaElement(it, node);
			}
		}
		return false;
	}

	// handle type declarations.

	private void HandleTypeDeclaration(List<BodyDeclaration> bodys) {
		Iterator<BodyDeclaration> bitr = bodys.iterator();
		while (bitr.hasNext()) {
			BodyDeclaration bd = bitr.next();
			pre_visit_task.Put(bd, new Runnable() {
				@Override
				public void run() {
					StatementOverHandle();
				}
			});
		}
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		super.Validation(node);
		@SuppressWarnings("unchecked")
		List<BodyDeclaration> bodys = node.bodyDeclarations();
		HandleTypeDeclaration(bodys);
		return super.visit(node);
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		@SuppressWarnings("unchecked")
		List<BodyDeclaration> bodys = node.bodyDeclarations();
		HandleTypeDeclaration(bodys);
		return super.visit(node);
	}

	// do nothing.

	@Override
	public boolean visit(Dimension node) {
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeParameter node) {
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(Modifier node) {
		super.Validation(node);
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclarationStatement node) {
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		// do not need to handle. All will be handled in StatementExpression.
		return super.visit(node);
	}

	@Override
	public boolean visit(AssertStatement node) {
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(BlockComment node) {
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(Javadoc node) {
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodRefParameter node) {
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodRef node) {
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(LineComment node) {
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(CompilationUnit node) {
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(TagElement node) {
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(TextElement node) {
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		super.Validation(node);
		// will do in the future.
		return super.visit(node);
	}

	@Override
	public boolean visit(MemberValuePair node) {
		// will do in the future.
		return super.visit(node);
	}

	@Override
	public boolean visit(MemberRef node) {
		// will do in the future.
		return super.visit(node);
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		super.Validation(node);
		// will do in the future.
		return super.visit(node);
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		super.Validation(node);
		// will do in the future.
		return super.visit(node);
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		super.Validation(node);
		// will do in the future.
		return super.visit(node);
	}

	@Override
	public boolean visit(ThrowStatement node) {
		// will do in the future.
		return super.visit(node);
	}

	// Solved. switch such branch, how to model? dependencies on branches have
	// been considered.

	// Solved. re-check all codes, be sure the scope to search the bind.

	// Solved. remember to handle unresolved type or method invocation to its
	// raw
	// name.

	// Solved. remember to check whether temporarily appeared variable bindings
	// such as field_access/super_field_access are properly handled.

	// Solved. remember to handle null resolved binding.

	// Solved. Undone tasks are not handled. Solution: these don't need to be
	// handled
	// anymore.

	// Solved. general IR needs to handle related dependency. These situations
	// are not handled.

	// Solved. assign data_dependency is not handled. assign operation should be
	// skipped.

	// Solved. are dependencies in infix operations etc. Solved? Yes.

	public static int GetMaxLevel() {
		return max_level;
	}

	public IRCode GetGeneration() {
		return irc;
	}

}
