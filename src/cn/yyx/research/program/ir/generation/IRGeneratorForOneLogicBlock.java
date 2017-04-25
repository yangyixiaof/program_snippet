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

import cn.yyx.research.program.ir.IRMeta;
import cn.yyx.research.program.ir.ast.ASTSearch;
import cn.yyx.research.program.ir.bind.BindingManager;
import cn.yyx.research.program.ir.element.ConstantUniqueElement;
import cn.yyx.research.program.ir.element.UncertainReferenceElement;
import cn.yyx.research.program.ir.element.UnresolvedLambdaUniqueElement;
import cn.yyx.research.program.ir.element.UnresolvedTypeElement;
import cn.yyx.research.program.ir.storage.node.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnection;
import cn.yyx.research.program.ir.storage.node.execution.DefaultINodeTask;
import cn.yyx.research.program.ir.storage.node.execution.RequireHandleTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneMethod;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneMethodInvocation;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneOperation;
import cn.yyx.research.program.ir.task.IRASTNodeTask;

public class IRGeneratorForOneLogicBlock extends ASTVisitor {

	public static int max_level = Integer.MAX_VALUE; // Integer.MAX_VALUE partly
														// means infinite.
	public static final int un_exist = -100;
	// Solved. return and assign right should add special task.
	// Solved. variable declarations should be removed, only assignment in it should be retained.
	// TODO how to recognize the global relationship, eclipse jdt offers?
	// Solved. Important!!!!!!!, dependencies between differernt variables seem not handled, some are not needed but some are needed.
	
	// Solved. switch case mechanism is not as tree or graph and is sequential which is not right.
	
	// for return statements, all nodes related to return should be recorded.
	
	// name must be resolved and ensure it is a variable, a global variable or a type.
	// for method invocation's parameters.
	// Solved. this element is not assigned. should be assigned in HandleIJavaElement.
	// above used for method invocation only.
	// Solved. this element is not assigned. should be assigned in HandleIJavaElement.
	
	protected HashMap<ASTNode, Set<IJavaElement>> temp_statement_expression_element_memory = new HashMap<ASTNode, Set<IJavaElement>>();
	protected HashSet<IJavaElement> temp_statement_expression_environment_set = new HashSet<IJavaElement>();
	protected HashSet<IJavaElement> temp_statement_environment_set = new HashSet<IJavaElement>();
	protected HashMap<IJavaElement, Integer> all_count = new HashMap<IJavaElement, Integer>();
	protected HashMap<IJavaElement, ASTNode> all_happen = new HashMap<IJavaElement, ASTNode>();
	
	// check if all_happen is all right assigned. yes.
	
	// these two variables are all be handled when encountering source method invocation.
	// this variable is initialized in Construction method. so this is already be initialized.
	protected IJavaElement source_method_receiver_element = null; // already assigned.
	// this should be handled. this is no need anymore.
	// protected HashMap<ASTNode, IJavaElement> source_method_return_element = new HashMap<ASTNode, IJavaElement>();
	
	private Set<IJavaElement> SearchAndRememberAllElementsInASTNodeInJustEnvironment(Expression expr) {
		HashSet<IJavaElement> result = new HashSet<IJavaElement>();
		result.addAll(temp_statement_expression_environment_set);
		Set<ASTNode> tkeys = temp_statement_expression_element_memory.keySet();
		Iterator<ASTNode> titr = tkeys.iterator();
		while (titr.hasNext())
		{
			ASTNode astnode = titr.next();
			if (ASTSearch.ASTNodeContainsAnASTNode(expr, astnode))
			{
				Set<IJavaElement> set = temp_statement_expression_element_memory.remove(astnode);
				result.addAll(set);
			}
		}
		temp_statement_expression_element_memory.put(expr, result);
		return result;
	}
	
	protected void TempExpressionOverHandle(ASTNode node, boolean remember) {
		if (remember)
		{
			temp_statement_expression_element_memory.put(node, new HashSet<IJavaElement>(temp_statement_expression_environment_set));
		}
		temp_statement_expression_environment_set.clear();
	}
	
	protected void StatementOverHandle() {
		// no need to do that anymore.
		// temp_statement_instr_order.clear();
		temp_statement_expression_environment_set.clear();
		temp_statement_environment_set.clear();
	}

	protected Stack<HashMap<IJavaElement, IRForOneInstruction>> branchs_var_instr_order = new Stack<HashMap<IJavaElement, IRForOneInstruction>>();

	protected void PushBranchInstructionOrder() {
		HashMap<IJavaElement, IRForOneInstruction> t_hash = new HashMap<IJavaElement, IRForOneInstruction>();
		Iterator<IJavaElement> titr = temp_statement_environment_set.iterator();
		while (titr.hasNext()) {
			IJavaElement ije = titr.next();
			if (irc.HasElement(ije)) {
				t_hash.put(ije, irc.GetLastIRTreeNode(ije));
			}
		}
		branchs_var_instr_order.push(t_hash);
	}

	protected void PopBranchInstructionOrder() {
		branchs_var_instr_order.pop();
	}

	protected IRCode irc = null;

	// protected Queue<IRTask> undone_tasks = new LinkedList<IRTask>();

	protected IRASTNodeTask post_visit_task = new IRASTNodeTask();
	protected IRASTNodeTask pre_visit_task = new IRASTNodeTask();

	protected Map<ASTNode, HashSet<IJavaElement>> ast_block_bind = new HashMap<ASTNode, HashSet<IJavaElement>>();

	// protected Stack<HashSet<IBinding>> switch_case_bind = new
	// Stack<HashSet<IBinding>>();
	// protected Stack<LinkedList<ASTNode>> switch_case = new
	// Stack<LinkedList<ASTNode>>();

	public IRGeneratorForOneLogicBlock(IRCode irc) {
		this.irc = irc;
		this.source_method_receiver_element = new UncertainReferenceElement(irc.GetScopeIElement().getElementName());
	}

	// public Queue<IRTask> GetUndoneTasks() {
	// return undone_tasks;
	// }

	@Override
	public void preVisit(ASTNode node) {
		if (node instanceof Block) {
			ast_block_bind.put(node, new HashSet<IJavaElement>());
		}
		pre_visit_task.ProcessAndRemoveTask(node);
		super.preVisit(node);
	}

	// post handling statements.
	@Override
	public void postVisit(ASTNode node) {
		if (node instanceof Block) {
			ast_block_bind.remove(node);
		}
		if (node instanceof Statement) {
			StatementOverHandle();
		}
		post_visit_task.ProcessAndRemoveTask(node);
		super.postVisit(node);
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		@SuppressWarnings("unchecked")
		List<SingleVariableDeclaration> svds = node.parameters();
		Iterator<SingleVariableDeclaration> itr = svds.iterator();
		while (itr.hasNext()) {
			SingleVariableDeclaration svd = itr.next();
			SimpleName sn = svd.getName();
			IBinding ib = sn.resolveBinding();
			if ((ib != null) && (ib instanceof IVariableBinding)) {
				IVariableBinding ivb = (IVariableBinding) ib;
				IJavaElement ije = ivb.getJavaElement();
				if (ije instanceof IJavaElement) {
					irc.AddParameter((IJavaElement) ije);
				}
			}
		}
		return super.visit(node);
	}

	// method invocation.
	
	private IJavaElement WholeExpressionIsAnElement(ASTNode expr)
	{
		Iterator<IJavaElement> titr = temp_statement_expression_environment_set.iterator();
		while (titr.hasNext())
		{
			IJavaElement ije = titr.next();
			ASTNode happen = all_happen.get(ije);
			if (happen == expr) {
				return ije;
			}
		}
		return null;
	}
	
	protected HashMap<ASTNode, Map<IJavaElement, IRForOneInstruction>> method_parameter_element_instr_order = new HashMap<ASTNode, Map<IJavaElement, IRForOneInstruction>>();
	protected HashMap<ASTNode, Map<IJavaElement, Boolean>> method_parameter_element_instr_is_self = new HashMap<ASTNode, Map<IJavaElement, Boolean>>();
	
//	private void RecordASTNodePreEnvironment(ASTNode node)
//	{
//		Map<IJavaElement, Integer> env = irc.CopyEnvironment();
//		method_parameter_element_instr_order.put(node, env);
//	}
//	
//	private boolean CompareASTNodePreEnvironmentToJudgeIfDirectTransfer(ASTNode node)
//	{
//		if (temp_statement_expression_environment_set.size() == 1)
//		{
//			Map<IJavaElement, Integer> origin_env = method_parameter_element_instr_order.get(node);
//			Iterator<IJavaElement> titr = temp_statement_expression_environment_set.iterator();
//			IJavaElement ije = titr.next();
//			List<IRForOneInstruction> list = irc.GetOneAllIRUnits(ije);
//			if (list != null && list.size() > 0)
//			{
//				int idx = list.size() - 1;
//				Integer ori_idx = origin_env.get(ije);
//				if (ori_idx == idx)
//				{
//					return true;
//				}
//			}
//		}
//		return false;
//	}

	private void PreMethodInvocation(List<Expression> exprs)
	{
		// temp_statement_instr_order
		Iterator<Expression> eitr = exprs.iterator();
		while (eitr.hasNext())
		{
			
			Expression expr = eitr.next();
//			pre_visit_task.put(expr, new Runnable() {
//				@Override
//				public void run() {
//					RecordASTNodePreEnvironment(expr);
//				}
//			});
			post_visit_task.Put(expr, new Runnable() {
				@Override
				public void run() {
					IJavaElement w_ije = WholeExpressionIsAnElement(expr);
					Map<IJavaElement, Boolean> new_is_self_env = new HashMap<IJavaElement, Boolean>();
					method_parameter_element_instr_is_self.put(expr, new_is_self_env);
					Map<IJavaElement, IRForOneInstruction> new_env = new HashMap<IJavaElement, IRForOneInstruction>();
					Iterator<IJavaElement> titr = temp_statement_expression_environment_set.iterator();
					while (titr.hasNext())
					{
						IJavaElement ije = titr.next();
						IRForOneInstruction last_instr = irc.GetLastIRTreeNode(ije);
						if (last_instr != null)
						{
							new_env.put(ije, last_instr);
							boolean direct_transfer = false;
							if (ije == w_ije)
							{
								direct_transfer = true;
							}
							new_is_self_env.put(ije, direct_transfer);
						}
					}
					method_parameter_element_instr_order.put(expr, new_env);
					TempExpressionOverHandle(null, false);
				}
			});
		}
	}
	
	private void PostMethodInvocation(IMethodBinding imb, List<Expression> nlist, Expression expr, String identifier, ASTNode node)
	{
		if (imb != null && imb.getDeclaringClass() != null && imb.getDeclaringClass().isFromSource()) {
			// source method invocation.
			ITypeBinding itb = imb.getReturnType();
			if (itb.isPrimitive() && !itb.getQualifiedName().equals("void"))
			{
				IJavaElement jele = imb.getJavaElement();
				if (jele != null && jele instanceof IMethod) {
					IMethod im = (IMethod)jele;
					IRGeneratorHelper.GenerateMethodInvocationIR(this, nlist, im, expr, identifier, node);
					IRForOneMethodInvocation irfomi = (IRForOneMethodInvocation)irc.GetLastIRTreeNode(source_method_receiver_element);
					UncertainReferenceElement ure = new UncertainReferenceElement(node.toString());
					HandleIJavaElement(ure, node);
					IRGeneratorHelper.AddMethodReturnVirtualReceiveDependency(irc, ure, irfomi);
				}
			}
		} else {
			IRGeneratorHelper.GenerateGeneralIR(this, node, IRMeta.MethodInvocation + identifier);
		}
		method_parameter_element_instr_order.clear();
		method_parameter_element_instr_is_self.clear();
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> exprs = (List<Expression>) node.arguments();
		PreMethodInvocation(exprs);
		return super.visit(node);
	}

	@Override
	public void endVisit(MethodInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> nlist = (List<Expression>) node.arguments();
		PostMethodInvocation(node.resolveMethodBinding(), nlist, node.getExpression(), node.getName().toString(), node);
	}
	
	@Override
	public boolean visit(SuperMethodInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> exprs = (List<Expression>) node.arguments();
		PreMethodInvocation(exprs);
		return super.visit(node);
	}

	@Override
	public void endVisit(SuperMethodInvocation node) {
		TreatSuperClassElement(node);
		@SuppressWarnings("unchecked")
		List<Expression> nlist = (List<Expression>) node.arguments();
		PostMethodInvocation(node.resolveMethodBinding(), nlist, null,
				node.getName().toString(), node);
	}
	
	@Override
	public boolean visit(SuperConstructorInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> exprs = (List<Expression>) node.arguments();
		PreMethodInvocation(exprs);
		return super.visit(node);
	}

	@Override
	public void endVisit(SuperConstructorInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> nlist = (List<Expression>) node.arguments();
		PostMethodInvocation(node.resolveConstructorBinding(), nlist, null, "super", node);
	}
	
	@Override
	public boolean visit(ConstructorInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> exprs = (List<Expression>) node.arguments();
		PreMethodInvocation(exprs);
		return super.visit(node);
	}

	@Override
	public void endVisit(ConstructorInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> nlist = (List<Expression>) node.arguments();
		PostMethodInvocation(node.resolveConstructorBinding(), nlist, null, "this", node);
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		@SuppressWarnings("unchecked")
		List<Expression> nlist = (List<Expression>) node.arguments();
		PreMethodInvocation(nlist);
		if (node.getAnonymousClassDeclaration() != null) {
			pre_visit_task.Put(node, new Runnable() {
				@Override
				public void run() {
					PostMethodInvocation(node.resolveConstructorBinding(), nlist, null,
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
			PostMethodInvocation(node.resolveConstructorBinding(), nlist, null,
					"new#" + node.getType(), node);
		}
	}

	// handling statements.
	
	// Solved. all branches and loops should handle parallel connections.
	protected HashMap<ASTNode, Map<IJavaElement, IRForOneInstruction>> switch_record = new HashMap<ASTNode, Map<IJavaElement, IRForOneInstruction>>();
	
	private void HandleOneBranch(ASTNode all_in_control, ASTNode then_stat, boolean clear)
	{
		if (then_stat != null)
		{
			pre_visit_task.Put(then_stat, new Runnable() {
				@Override
				public void run() {
					ast_block_bind.put(then_stat, new HashSet<IJavaElement>());
				}
			});
			post_visit_task.Put(then_stat, new Runnable() {
				@Override
				public void run() {
					HashSet<IJavaElement> eles = ast_block_bind.get(then_stat);
					Iterator<IJavaElement> eitr = eles.iterator();
					while (eitr.hasNext())
					{
						IJavaElement ije = eitr.next();
						irc.SwitchDirection(ije, switch_record.get(all_in_control).get(ije));
						Map<IJavaElement, List<IRForOneInstruction>> merge = node_to_merge.get(all_in_control);
						if (merge == null)
						{
							merge = new HashMap<IJavaElement, List<IRForOneInstruction>>();
							node_to_merge.put(all_in_control, merge);
						}
						List<IRForOneInstruction> merge_list = merge.get(ije);
						if (merge_list == null)
						{
							merge_list = new LinkedList<IRForOneInstruction>();
							merge.put(ije, merge_list);
						}
						merge_list.add(irc.GetLastIRTreeNode(ije));
					}
					ast_block_bind.remove(then_stat);
					if (clear) {
						StatementOverHandle();
					}
				}
			});
		}
	}
	
	private void PreVisitBranch(ASTNode all_in_control, Expression judge, ASTNode then_stat, ASTNode else_stat, boolean clear)
	{
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.Put(judge, new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(this_ref, judge,
						IRMeta.If);
				PushBranchInstructionOrder();
				switch_record.put(all_in_control, irc.CopyEnvironment());
				if (clear) {
					StatementOverHandle();
				}
			}
		});
		HandleOneBranch(all_in_control, then_stat, clear);
		
		HandleOneBranch(all_in_control, else_stat, clear);
	}
	
	private void HandleMerge(ASTNode all_in_control)
	{
		List<IRForOneOperation> ops = new LinkedList<IRForOneOperation>();
		Map<IJavaElement, List<IRForOneInstruction>> merge = node_to_merge.remove(all_in_control);
		Iterator<IJavaElement> itr = merge.keySet().iterator();
		while (itr.hasNext())
		{
			IJavaElement ije = itr.next();
			IRForOneOperation irfop = new IRForOneOperation(irc, ije, IRMeta.BranchOver, DefaultINodeTask.class);
			ops.add(irfop);
			List<IRForOneInstruction> merge_list = merge.get(ije);
			MergeListParallelToOne(merge_list, ije, irfop);
		}
		IRGeneratorHelper.HandleEachElementInSameOperationDependency(ops);
		merge.clear();
	}
	
	private void PostVisitBranch(ASTNode all_in_control, boolean clear)
	{
		PopBranchInstructionOrder();
		switch_record.remove(all_in_control);
		HandleMerge(all_in_control);
		if (clear)
		{
			StatementOverHandle();
		}
	}
	
	@Override
	public boolean visit(IfStatement node) {
		PreVisitBranch(node, node.getExpression(), node.getThenStatement(), node.getElseStatement(), true);
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
		PreVisitBranch(node, node.getExpression(), node.getThenExpression(), node.getElseExpression(), false);
//		IRGeneratorForOneLogicBlock this_ref = this;
//		post_visit_task.Put(node.getExpression(), new Runnable() {
//			@Override
//			public void run() {
//				IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getExpression(),
//						IRMeta.If);
//				PushBranchInstructionOrder();
//			}
//		});

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

	// loop statements begin.
	@Override
	public boolean visit(WhileStatement node) {
		// ast_block_bind.put(node, new HashSet<IBinding>());
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.Put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getExpression(),
						IRMeta.While);
				PushBranchInstructionOrder();
				StatementOverHandle();
			}
		});
		return super.visit(node);
	}

	@Override
	public void endVisit(WhileStatement node) {
		PopBranchInstructionOrder();
		super.endVisit(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		// ast_block_bind.put(node, new HashSet<IBinding>());
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.Put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getExpression(),
						IRMeta.DoWhile);
				PushBranchInstructionOrder();
				StatementOverHandle();
			}
		});
		return super.visit(node);
	}

	@Override
	public void endVisit(DoStatement node) {
		PopBranchInstructionOrder();
		super.endVisit(node);
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
			final ASTNode exp = last_expr;
			if (last_expr != null) {
				post_visit_task.Put(last_expr, new Runnable() {
					@Override
					public void run() {
						HashSet<IJavaElement> temp = this_ref.temp_statement_environment_set;
						this_ref.temp_statement_environment_set = this_ref.temp_statement_expression_environment_set;
						
						IRGeneratorHelper.GenerateGeneralIR(this_ref, exp,
								IRMeta.For_Initial);
						
						TempExpressionOverHandle(null, false);
						this_ref.temp_statement_environment_set = temp;
					}
				});
			}
		}
		Expression expr = node.getExpression();
		if (expr != null) {
			last_expr = expr;
			final ASTNode exp = last_expr;
			if (last_expr != null) {
				post_visit_task.Put(last_expr, new Runnable() {
					@Override
					public void run() {
						HashSet<IJavaElement> temp = this_ref.temp_statement_environment_set;
						this_ref.temp_statement_environment_set = this_ref.temp_statement_expression_environment_set;
						
						IRGeneratorHelper.GenerateGeneralIR(this_ref, exp,
								IRMeta.For_Judge);
						PushBranchInstructionOrder();
						
						TempExpressionOverHandle(null, false);
						this_ref.temp_statement_environment_set = temp;
					}
				});
			}
		}
		@SuppressWarnings("unchecked")
		List<Expression> upd_list = node.updaters();
		if (upd_list != null) {
			last_expr = upd_list.get(upd_list.size() - 1);
			final ASTNode exp = last_expr;
			if (last_expr != null) {
				post_visit_task.Put(last_expr, new Runnable() {
					@Override
					public void run() {
						HashSet<IJavaElement> temp = this_ref.temp_statement_environment_set;
						this_ref.temp_statement_environment_set = this_ref.temp_statement_expression_environment_set;
						
						IRGeneratorHelper.GenerateGeneralIR(this_ref, exp,
								IRMeta.For_Update);
						
						TempExpressionOverHandle(null, false);
						this_ref.temp_statement_environment_set = temp;
					}
				});
			}
		}

		return super.visit(node);
	}

	@Override
	public void endVisit(ForStatement node) {
		Expression expr = node.getExpression();
		if (expr != null) {
			PopBranchInstructionOrder();
		}
		StatementOverHandle();
		super.endVisit(node);
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.Put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node,
						IRMeta.EnhancedFor);
				StatementOverHandle();
			}
		});
		return super.visit(node);
	}

	@Override
	public void endVisit(EnhancedForStatement node) {
		super.endVisit(node);
	}

	// loop statements end.

	// Solved. missed to consider the label and will be considered in the future.
	
	private void HandleBreakContinueStatement(ASTNode node, SimpleName label, String code) {
		ASTNode n = ASTSearch.FindMostCloseLoopNode(node);
		if (n != null && ast_block_bind.containsKey(n)) {
			HashSet<IJavaElement> elements = ast_block_bind.get(n);
			Map<IJavaElement, IRForOneInstruction> eles = irc.CopyEnvironment(elements);
			
			ASTNode break_scope = SearchForLiveScopeOfBreakContinue(node, label);
			
			IRGeneratorForOneLogicBlock this_ref = this;
			post_visit_task.Put(break_scope, new Runnable() {
				@Override
				public void run() {
					IRGeneratorHelper.GenerateNoVariableBindingIR(this_ref, node, elements,
							code);
					Set<IJavaElement> keys = eles.keySet();
					Iterator<IJavaElement> kitr = keys.iterator();
					while (kitr.hasNext())
					{
						IJavaElement ije = kitr.next();
						IRForOneInstruction ir_instr = eles.get(ije);
						IRGeneratorForOneProject.GetInstance().RegistConnection(new StaticConnection(ir_instr, irc.GetLastIRTreeNode(ije), EdgeBaseType.Self.getType()));
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
		if (label != null)
		{
			label_scope.put(label.toString(), node.getBody());
		}
		return super.visit(node);
	}
	
	@Override
	public void endVisit(LabeledStatement node) {
		SimpleName label = node.getLabel();
		if (label != null)
		{
			label_scope.remove(label.toString());
		}
		super.endVisit(node);
	}
	
	private ASTNode SearchForLiveScopeOfBreakContinue(ASTNode node, SimpleName label)
	{
		if (label != null) {
			return label_scope.get(label.toString());
		} else {
			return ASTSearch.FindMostCloseLoopNode(node);
		}
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		// no need to do anything, all things are in
		// VariableDeclarationFragment.
		return super.visit(node);
	}

	// closely related expressions stumbled into.
	@Override
	public boolean visit(VariableDeclarationExpression node) {
		// no need to do anything, all things are in
		// VariableDeclarationFragment.
		return super.visit(node);
	}
	
	private void HandleAssign(Expression left, Expression right)
	{
		if (right != null)
		{
			right.accept(this);
		}
		Map<IJavaElement, IRForOneInstruction> env = irc.CopyEnvironment(temp_statement_environment_set);
		StatementOverHandle();
		
		right.accept(this);
		
		Iterator<IJavaElement> itr = temp_statement_environment_set.iterator();
		while (itr.hasNext())
		{
			IJavaElement t_ije = itr.next();
			IRForOneInstruction last_node = irc.GetLastIRTreeNode(t_ije);
			last_node.SetRequireType(EdgeBaseType.Self.getType());
			
			Set<IJavaElement> ekeys = env.keySet();
			Iterator<IJavaElement> eitr = ekeys.iterator();
			while (eitr.hasNext())
			{
				IJavaElement e_ije = eitr.next();
				if (e_ije != t_ije)
				{
					IRForOneInstruction ir_instr = env.get(e_ije);
					IRGeneratorForOneProject.GetInstance().RegistConnection(new StaticConnection(ir_instr, last_node, EdgeBaseType.Sequential.getType()));
				}
			}
		}
		
		IJavaElement ije = WholeExpressionIsAnElement(right);
		if (ije != null)
		{
			IRForOneInstruction last = irc.GetLastIRTreeNode(ije);
			last.SetRequireType(EdgeBaseType.Self.getType());
			irc.AddAssignDependency(ije, new HashSet<IJavaElement>(env.keySet()));
		}
		
		StatementOverHandle();
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		// Solved. how to redirect? same as assignment.
		HandleAssign(node.getName(), node.getInitializer());
//		IRGeneratorForOneLogicBlock this_ref = this;
//		post_visit_task.put(node.getName(), new Runnable() {
//			@Override
//			public void run() {
//				IRGeneratorHelper.GenerateGeneralIR(this_ref, node,
//						IRMeta.VariabledDeclare);
//				StatementOverHandle();
//			}
//		});
		return false;
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		// Solved. how to redirect? same as assignment.
		HandleAssign(node.getName(), node.getInitializer());
//		IRGeneratorForOneLogicBlock this_ref = this;
//		post_visit_task.put(node.getName(), new Runnable() {
//			@Override
//			public void run() {
//				IRGeneratorHelper.GenerateGeneralIR(this_ref, node,
//						IRMeta.VariabledDeclare);
//				StatementOverHandle();
//			}
//		});
		return false;
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
		if (expr != null)
		{
//			pre_visit_task.put(expr, new Runnable() {
//				@Override
//				public void run() {
//					RecordASTNodePreEnvironment(expr);
//				}
//			});
			post_visit_task.Put(expr, new Runnable() {
				@Override
				public void run() {
					IJavaElement ije = WholeExpressionIsAnElement(expr);
					if (ije != null)
					{
						Iterator<IJavaElement> titr = temp_statement_environment_set.iterator();
						while (titr.hasNext())
						{
							IJavaElement t_ije = titr.next();
							IRForOneInstruction iru = irc.GetLastIRTreeNode(t_ije);
							if (iru != null)
							{
								iru.SetAcceptType(EdgeBaseType.Self.getType());
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
		Iterator<IJavaElement> titr = temp_statement_environment_set.iterator();
		while (titr.hasNext())
		{
			IJavaElement ije = titr.next();
			IRForOneInstruction iru = irc.GetLastIRTreeNode(ije);
			if (iru != null)
			{
				irc.PutOutNodes(ije, iru);
			}
		}
	}

	// need to handle data_dependency.
	@Override
	public boolean visit(Assignment node) {
		// Solved. how to redirect? last node to skip self.
		
		// Solved. depd needs to record which iirnode left value depends. connections need to be added from left to right.
		// Solved. assign dependency should be extracted as a stand_alone function because var-declare also will also use it.
		
		HandleAssign(node.getLeftHandSide(), node.getRightHandSide());
		
//		Expression right_val = node.getRightHandSide();
//		right_val.accept(this);
//		IJavaElement right_jele = WholeExpressionIsAnElement(right_val);
//		if (right_jele != null)
//		{
//			IRForOneInstruction iru = irc.GetLastIRTreeNode(right_jele);
//			iru.SetAcceptType(EdgeBaseType.Self.getType());
//		}
//		HashSet<IJavaElement> depd = new HashSet<IJavaElement>(temp_statement_environment_set);
//		StatementOverHandle();
//		Expression left_val = node.getLeftHandSide();
//		left_val.accept(this);
//		IRGeneratorHelper.GenerateGeneralIR(this, node, IRMeta.LeftHandAssign);
//		IJavaElement left_jele = WholeExpressionIsAnElement(left_val);
//		if (left_jele != null) {
//			IRForOneInstruction iru = irc.GetLastIRTreeNode(left_jele);
//			iru.SetRequireType(EdgeBaseType.Self.getType());
//			iru.SetOutConnectionMergeTask(new SkipSelfTask(iru));
//		} else {
//			// add assign dependency.
//			Iterator<IJavaElement> titr = temp_statement_environment_set.iterator();
//			while (titr.hasNext()) {
//				IJavaElement ijele = titr.next();
//				irc.AddAssignDependency(ijele, new HashSet<IJavaElement>(depd));
//			}
//		}
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
	public boolean visit(SynchronizedStatement node) {
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.Put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node,
						IRMeta.Synchronized);
				StatementOverHandle();
			}
		});
		return super.visit(node);
	}

	protected Stack<Set<IJavaElement>> switch_judge_members = new Stack<Set<IJavaElement>>();
	protected Map<ASTNode, Integer> switch_case_flag = new HashMap<ASTNode, Integer>();
	protected Map<SwitchStatement, SwitchCase> last_switch_case = new HashMap<SwitchStatement, SwitchCase>();
	
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
		node_to_merge.put(node, new HashMap<IJavaElement, List<IRForOneInstruction>>());
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.Put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				switch_judge_members.push(new HashSet<IJavaElement>(temp_statement_environment_set));
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node, IRMeta.Switch);
				PushBranchInstructionOrder();
				switch_record.put(node, irc.CopyEnvironment());
				StatementOverHandle();
			}
		});
		return super.visit(node);
	}

	private void PopSwitchBranch(SwitchStatement node) {
		// branch flag
		Integer flag = switch_case_flag.get(node);
		if (flag == null) {
			flag = 0;
		}
		flag = (flag + 1) % 2;
		switch_case_flag.put(node, flag);
		if (flag == 0) {
			PopBranchInstructionOrder();
		}
	}

	private void ClearSwitchEnvironment(SwitchStatement node) {
		switch_case_flag.remove(node);
		switch_judge_members.pop();
		switch_record.remove(node);
		last_switch_case.remove(node);
		node_to_merge.get(node).clear();
		node_to_merge.remove(node);
	}
	
	private void HandleLastSwitchCase(SwitchStatement switch_statement, SwitchCase node)
	{
		SwitchCase sc = last_switch_case.get(switch_statement);
		if (sc == null) {
			if (node != null)
			{
				last_switch_case.put(switch_statement, node);
			}
		} else {
			HashSet<IJavaElement> bind_elements = ast_block_bind.get(sc);
			Map<IJavaElement, List<IRForOneInstruction>> merge = node_to_merge.get(switch_statement);
			PrepareCurrentEnvironmentToMerge(bind_elements, merge);
			
			ast_block_bind.remove(sc);
			if (node != null)
			{
				last_switch_case.put(switch_statement, node);
				ast_block_bind.put(node, new HashSet<IJavaElement>());
			}
		}
	}

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
		SwitchStatement switch_statement = (SwitchStatement) node.getParent();
		
		// handle previous switch_case.
		HandleLastSwitchCase(switch_statement, node);
		
		// switch to direction.
		Map<IJavaElement, IRForOneInstruction> env = switch_record.get(switch_statement);
		Set<IJavaElement> ekys = env.keySet();
		Iterator<IJavaElement> eitr = ekys.iterator();
		while (eitr.hasNext())
		{
			IJavaElement ije = eitr.next();
			irc.SwitchDirection(ije, env.get(ije));
		}
		
		PopSwitchBranch(switch_statement);
		Expression expr = node.getExpression();
		if (expr != null) {
			post_visit_task.Put(expr, new Runnable() {
				@Override
				public void run() {
					IRGeneratorHelper.GenerateGeneralIR(this_ref, node,
							IRMeta.Switch_Case_Cause);
					PushBranchInstructionOrder();
					StatementOverHandle();
				}
			});
		} else {
			Set<IJavaElement> members = switch_judge_members.peek();
			IRGeneratorHelper.GenerateNoVariableBindingIR(this_ref, node, members, IRMeta.Switch_Case_Default);
			PushBranchInstructionOrder();
			StatementOverHandle();
		}
		return super.visit(node);
	}

	@Override
	public void endVisit(SwitchCase node) {
		// IRGeneratorHelper.GenerateGeneralIR(node, node.getExpression(), irc,
		// temp_statement_environment_set,
		// IRMeta.Switch_Case_Cause);
		// super.endVisit(node);
	}

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
		HandleLastSwitchCase(node, null);
		
		HandleMerge(node);
		
		PopSwitchBranch(node);
		ClearSwitchEnvironment(node);
		StatementOverHandle();
	}

	// handling expressions.

	private boolean HandleBinding(IBinding ib, ASTNode happen) {
		if (!BindingManager.QualifiedBinding(ib)) {
			return false;
		}
		IJavaElement jele = ib.getJavaElement();
		return HandleIJavaElement(jele, happen);
	}

	private boolean HandleIJavaElement(IJavaElement jele, ASTNode happen) {
		// handle loop_bind, just for no variable bind statements such as
		// break and continue.
		// if (!BindingManager.QualifiedBinding(ib)) {
		// return;
		// }
		// IJavaElement jele = ib.getJavaElement();
		if (jele == null) {
			return false;
		}
		if (jele instanceof IMember) {
			IMember im = (IMember) jele;
			if (im.getDeclaringType().isBinary()) {
				return false;
			}
		} else {
			if (!(jele instanceof ILocalVariable) && !(jele instanceof UnresolvedLambdaUniqueElement)
					&& !(jele instanceof UnresolvedTypeElement) && !(jele instanceof ConstantUniqueElement)) {
				return false;
			}
		}

		Set<ASTNode> ks = ast_block_bind.keySet();
		Iterator<ASTNode> kitr = ks.iterator();
		while (kitr.hasNext()) {
			ASTNode an = kitr.next();
			HashSet<IJavaElement> set = ast_block_bind.get(an);
			set.add(jele);
		}

		// handle switch_case_bind
		// if (!switch_case_bind.isEmpty()) {
		// switch_case_bind.peek().add(ib);
		// }

		// next isolated tasks.
		all_count.put(jele, -1);
		all_happen.put(jele, happen);
		temp_statement_environment_set.add(jele);
		temp_statement_expression_environment_set.add(jele);
		return true;
	}

	@Override
	public boolean visit(SimpleName node) {
		IBinding ib = node.resolveBinding();
		HandleBinding(ib, node);

		return super.visit(node);
	}

	@Override
	public void endVisit(FieldAccess node) {
		IVariableBinding ib = node.resolveFieldBinding();
		if (ib == null || ib.getJavaElement() == null) {
			IRGeneratorHelper.GenerateGeneralIR(this, node,
					IRMeta.FieldAccess + node.getName().toString());
		}
	}

	@Override
	public void endVisit(SuperFieldAccess node) {
		IVariableBinding ib = node.resolveFieldBinding();
		if (ib == null || ib.getJavaElement() == null) {
			TreatSuperClassElement(node);
			IRGeneratorHelper.GenerateGeneralIR(this, node,
					IRMeta.FieldAccess + node.getName().toString());
		}
	}

	@Override
	public boolean visit(StringLiteral node) {
		HandleIJavaElement(IRGeneratorForOneProject.GetInstance().FetchConstantUniqueElement(node.toString()), node);
		return super.visit(node);
	}

	@Override
	public boolean visit(NumberLiteral node) {
		HandleIJavaElement(IRGeneratorForOneProject.GetInstance().FetchConstantUniqueElement(node.toString()), node);
		return super.visit(node);
	}

	@Override
	public boolean visit(NullLiteral node) {
		HandleIJavaElement(IRGeneratorForOneProject.GetInstance().FetchConstantUniqueElement(node.toString()), node);
		return super.visit(node);
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		HandleIJavaElement(IRGeneratorForOneProject.GetInstance().FetchConstantUniqueElement(node.toString()), node);
		return super.visit(node);
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		HandleIJavaElement(IRGeneratorForOneProject.GetInstance().FetchConstantUniqueElement(node.toString()), node);
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeLiteral node) {
		HandleIJavaElement(IRGeneratorForOneProject.GetInstance().FetchConstantUniqueElement(node.toString()), node);
		return super.visit(node);
	}

	private void HandleType(IBinding ib, String represent, ASTNode happen) {
		boolean source_resolved = HandleBinding(ib, happen);
		if (!source_resolved) {
			UnresolvedTypeElement ele = IRGeneratorForOneProject.GetInstance().FetchUnresolvedTypeElement(represent);
			HandleIJavaElement(ele, happen);
		}
	}

	@Override
	public boolean visit(ArrayType node) {
		HandleType(node.resolveBinding(), node.toString(), node);
		return false;
	}

	@Override
	public boolean visit(SimpleType node) {
		HandleType(node.resolveBinding(), node.toString(), node);
		return false;
	}

	@Override
	public boolean visit(PrimitiveType node) {
		HandleType(node.resolveBinding(), node.toString(), node);
		return false;
	}

	@Override
	public boolean visit(QualifiedType node) {
		HandleType(node.resolveBinding(), node.toString(), node);
		return false;
	}

	@Override
	public boolean visit(NameQualifiedType node) {
		HandleType(node.resolveBinding(), node.toString(), node);
		return false;
	}

	@Override
	public boolean visit(QualifiedName node) {
		HandleType(node.resolveBinding(), node.toString(), node);
		return false;
	}

	@Override
	public void endVisit(PrefixExpression node) {
		IRGeneratorHelper.GenerateGeneralIR(this, node,
				IRMeta.Prefix + node.getOperator().toString());
	}

	@Override
	public boolean visit(PostfixExpression node) {
		IRGeneratorHelper.GenerateGeneralIR(this, node,
				IRMeta.Postfix + node.getOperator().toString());
		return super.visit(node);
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		// no need to do anything.
		return super.visit(node);
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.Put(node.getLeftOperand(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getLeftOperand(), 
						IRMeta.InstanceOfExpression);
			}
		});
		return super.visit(node);
	}

	@Override
	public void endVisit(InstanceofExpression node) {
		IRGeneratorHelper.GenerateGeneralIR(this, node.getRightOperand(),
				IRMeta.InstanceOfType);
		super.endVisit(node);
	}
	
	// Reminding: begin to handle infix expression.
	private Map<ASTNode, Map<IJavaElement, List<IRForOneInstruction>>> node_to_merge = new HashMap<ASTNode, Map<IJavaElement, List<IRForOneInstruction>>>();
	
	private void PrepareCurrentEnvironmentToMerge(Set<IJavaElement> bind_elements, Map<IJavaElement, List<IRForOneInstruction>> merge)
	{
		Iterator<IJavaElement> itr = bind_elements.iterator();
		while (itr.hasNext())
		{
			IJavaElement ije = itr.next();
			List<IRForOneInstruction> list = merge.get(ije);
			if (list == null)
			{
				list = new LinkedList<IRForOneInstruction>();
				merge.put(ije, list);
			}
			list.add(irc.GetLastIRTreeNode(ije));
		}
	}
	
	@Override
	public boolean visit(InfixExpression node) {
		HashMap<IJavaElement, List<IRForOneInstruction>> merge = new HashMap<IJavaElement, List<IRForOneInstruction>>();
		node_to_merge.put(node, merge);
		
		Map<IJavaElement, IRForOneInstruction> env = irc.CopyEnvironment();
		
		List<Expression> expr_list = new LinkedList<Expression>();
		expr_list.add(node.getLeftOperand());
		@SuppressWarnings("unchecked")
		List<Expression> exprs = (List<Expression>)node.extendedOperands();
		expr_list.addAll(exprs);
		
		Iterator<Expression> eitr = expr_list.iterator();
		while (eitr.hasNext())
		{
			Expression expr = eitr.next();
			pre_visit_task.Put(expr, new Runnable() {
				@Override
				public void run() {
					Set<IJavaElement> ekeys = env.keySet();
					Iterator<IJavaElement> eijeitr = ekeys.iterator();
					while (eijeitr.hasNext())
					{
						IJavaElement ije = eijeitr.next();
						IRForOneInstruction irtree_node = env.get(ije);
						irc.SwitchDirection(ije, irtree_node);
					}
				}
			});
			post_visit_task.Put(expr, new Runnable() {
				@Override
				public void run() {
					Set<IJavaElement> all_elements = SearchAndRememberAllElementsInASTNodeInJustEnvironment(expr);
					PrepareCurrentEnvironmentToMerge(all_elements, merge);
				}
			});
		}
		
//		IRGeneratorForOneLogicBlock this_ref = this;
//		post_visit_task.put(node.getLeftOperand(), new Runnable() {
//			@Override
//			public void run() {
//				IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getLeftOperand(), 
//						IRMeta.InfixLeftExpression + node.getOperator().toString());
//			}
//		});
		return false;
	}
	
	private void MergeListParallelToOne(List<IRForOneInstruction> list, IJavaElement ije, IRForOneOperation irfop)
	{
		Iterator<IRForOneInstruction> litr = list.iterator();
		while (litr.hasNext())
		{
			IRForOneInstruction tn = litr.next();
			IRGeneratorForOneProject.GetInstance().RegistConnection(new StaticConnection(tn, irfop, EdgeBaseType.Self.getType()));
		}
		irc.SwitchDirection(ije, irfop);
	}
	
	@Override
	public void endVisit(InfixExpression node) {
//		IRGeneratorHelper.GenerateGeneralIR(this, node.getRightOperand(),
//				IRMeta.InfixRightExpression + node.getOperator().toString());
		Map<IJavaElement, List<IRForOneInstruction>> merge = node_to_merge.get(node);
		Set<IJavaElement> mkeys = merge.keySet();
		Iterator<IJavaElement> mitr = mkeys.iterator();
		List<IRForOneOperation> new_creation = new LinkedList<IRForOneOperation>();
		while (mitr.hasNext())
		{
			IJavaElement ije = mitr.next();
			List<IRForOneInstruction> list = merge.get(ije);
			IRForOneOperation irfop = new IRForOneOperation(irc, ije, node.getOperator().toString(), DefaultINodeTask.class);
			new_creation.add(irfop);
			MergeListParallelToOne(list, ije, irfop);
		}
		IRGeneratorHelper.HandleEachElementInSameOperationDependency(new_creation);
		node_to_merge.get(node).clear();
		node_to_merge.remove(node);
		super.endVisit(node);
	}
	@Override
	public boolean visit(ImportDeclaration node) {
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		// do not need handle it.
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		HandleBinding(node.resolveVariable(), node);
		return super.visit(node);
	}

	@Override
	public boolean visit(CatchClause node) {
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.Put(node.getException(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getException(),
						IRMeta.CatchClause);
			}
		});
		return super.visit(node);
	}

	@Override
	public boolean visit(CastExpression node) {
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.Put(node.getType(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getType(),
						IRMeta.CastType);
			}
		});
		return super.visit(node);
	}

	@Override
	public void endVisit(CastExpression node) {
		IRGeneratorForOneLogicBlock this_ref = this;
		IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getExpression(),
				IRMeta.CastExpression);
	}

	@Override
	public boolean visit(ArrayCreation node) {
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.Put(node.getType(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getType(),
						IRMeta.ArrayCreation);
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
					IRGeneratorHelper.GenerateGeneralIR(this_ref, expr,
							IRMeta.ArrayCreationIndex);
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
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getArray(),
						IRMeta.Array);
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

				IRForOneMethod irfom = IRGeneratorForOneProject.GetInstance().FetchIMethodIR(im);
				IRGeneratorForOneLogicBlock irgfocb = new IRGeneratorForOneLogicBlock(irfom);
				node.accept(irgfocb);
			}
		}
		if (!handled) {
			HandleIJavaElement(IRGeneratorForOneProject.GetInstance().FetchUnresolvedLambdaUniqueElement(node.toString(), (IMember)irc.GetScopeIElement(), irc.CopyEnvironment()), node);
		}
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
			IRGeneratorHelper.GenerateGeneralIR(this, mr,
					IRMeta.MethodReference + code);
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
				HandleIJavaElement(IRGeneratorForOneProject.GetInstance().FetchUnresolvedTypeElement(td.getName().toString()), node);
				return true;
			} else {
				HandleIJavaElement(it, node);
			}
		}
		return false;
	}

	@Override
	public void endVisit(SuperMethodReference node) {
		TreatSuperClassElement(node);
		HandleMethodReferenceEnd(node.resolveMethodBinding(), node, node.getName().toString());
		super.endVisit(node);
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
	public boolean visit(ParameterizedType node) {
		// it won't happen.
		return super.visit(node);
	}

	@Override
	public boolean visit(BlockComment node) {
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(WildcardType node) {
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
	public boolean visit(UnionType node) {
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(IntersectionType node) {
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
		// will do in the future.
		return super.visit(node);
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		// will do in the future.
		return super.visit(node);
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		// will do in the future.
		return super.visit(node);
	}

	@Override
	public boolean visit(ThrowStatement node) {
		// will do in the future.
		return super.visit(node);
	}

	// Solved. switch such branch, how to model? dependencies on branches have been considered.

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