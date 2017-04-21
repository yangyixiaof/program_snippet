package cn.yyx.research.program.ir.generation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

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
import cn.yyx.research.program.ir.storage.node.execution.RequireHandleTask;
import cn.yyx.research.program.ir.storage.node.execution.SkipSelfTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneMethod;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneMethodInvocation;

public class IRGeneratorForOneLogicBlock extends ASTVisitor {

	public static int max_level = Integer.MAX_VALUE; // Integer.MAX_VALUE partly
														// means infinite.
	public static final int un_exist = -100;
	// TODO return and assign right should add special task.
	// TODO variable declarations should be removed, only assignment in it should be retained.
	// TODO how to recognize the global relationship, eclipse jdt offers?
	
	// for return statements, all nodes related to return should be recorded.
	
	// name must be resolved and ensure it is a variable, a global variable or a type.
	// for method invocation's parameters.
	// Solved. this element is not assigned. should be assigned in HandleIJavaElement.
	protected HashSet<IJavaElement> temp_statement_expression_environment_set = new HashSet<IJavaElement>();
	protected HashMap<ASTNode, Map<IJavaElement, Integer>> temp_statement_instr_order = new HashMap<ASTNode, Map<IJavaElement, Integer>>();
	protected HashMap<ASTNode, Map<IJavaElement, Boolean>> temp_statement_instr_is_self = new HashMap<ASTNode, Map<IJavaElement, Boolean>>();
	// above used for method invocation only.
	// Solved. this element is not assigned. should be assigned in HandleIJavaElement.
	protected HashSet<IJavaElement> temp_statement_environment_set = new HashSet<IJavaElement>();
	protected HashMap<IJavaElement, Integer> all_count = new HashMap<IJavaElement, Integer>();
	protected HashMap<IJavaElement, ASTNode> all_happen = new HashMap<IJavaElement, ASTNode>();
	
	// check if all_happen is all right assigned. yes.
	
	// these two variables are all be handled when encountering source method invocation.
	// this variable is initialized in Construction method. so this is already be initialized.
	protected IJavaElement source_method_receiver_element = null; // already assigned.
	// this should be handled. this is no need anymore.
	// protected HashMap<ASTNode, IJavaElement> source_method_return_element = new HashMap<ASTNode, IJavaElement>();
	
	protected void TempExpressionOverHandle() {
		temp_statement_expression_environment_set.clear();
	}
	
	protected void StatementOverHandle() {
		// no need to do that anymore.
		// temp_statement_instr_order.clear();
		temp_statement_expression_environment_set.clear();
		temp_statement_environment_set.clear();
	}

	protected Stack<HashMap<IJavaElement, Integer>> branchs_var_instr_order = new Stack<HashMap<IJavaElement, Integer>>();

	protected void PushBranchInstructionOrder() {
		HashMap<IJavaElement, Integer> t_hash = new HashMap<IJavaElement, Integer>();
		Iterator<IJavaElement> titr = temp_statement_environment_set.iterator();
		while (titr.hasNext()) {
			IJavaElement im = titr.next();
			List<IRForOneInstruction> ls = irc.GetOneAllIRUnits(im);
			if (ls != null && ls.size() > 0) {
				int order = ls.size() - 1;
				t_hash.put(im, order);
			}
		}
		branchs_var_instr_order.push(t_hash);
	}

	protected void PopBranchInstructionOrder() {
		branchs_var_instr_order.pop();
	}

	protected IRCode irc = null;

	// protected Queue<IRTask> undone_tasks = new LinkedList<IRTask>();

	protected Map<ASTNode, Runnable> post_visit_task = new HashMap<ASTNode, Runnable>();
	protected Map<ASTNode, Runnable> pre_visit_task = new HashMap<ASTNode, Runnable>();

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
		if (pre_visit_task.containsKey(node)) {
			Runnable run = pre_visit_task.get(node);
			run.run();
		}
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
		if (post_visit_task.containsKey(node)) {
			Runnable run = post_visit_task.get(node);
			run.run();
		}
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
	
	private void RecordASTNodePreEnvironment(ASTNode node)
	{
		Map<IJavaElement, Integer> env = irc.CopyEnvironment();
		temp_statement_instr_order.put(node, env);
	}
	
	private boolean CompareASTNodePreEnvironmentToJudgeIfDirectTransfer(ASTNode node)
	{
		if (temp_statement_expression_environment_set.size() == 1)
		{
			Map<IJavaElement, Integer> origin_env = temp_statement_instr_order.get(node);
			Iterator<IJavaElement> titr = temp_statement_expression_environment_set.iterator();
			IJavaElement ije = titr.next();
			List<IRForOneInstruction> list = irc.GetOneAllIRUnits(ije);
			if (list != null && list.size() > 0)
			{
				int idx = list.size() - 1;
				Integer ori_idx = origin_env.get(ije);
				if (ori_idx == idx)
				{
					return true;
				}
			}
		}
		return false;
	}

	private void PreMethodInvocation(List<Expression> exprs)
	{
		// temp_statement_instr_order
		Iterator<Expression> eitr = exprs.iterator();
		while (eitr.hasNext())
		{
			
			Expression expr = eitr.next();
			pre_visit_task.put(expr, new Runnable() {
				@Override
				public void run() {
					RecordASTNodePreEnvironment(expr);
				}
			});
			post_visit_task.put(expr, new Runnable() {
				@Override
				public void run() {
					boolean direct_transfer = CompareASTNodePreEnvironmentToJudgeIfDirectTransfer(expr);
					Map<IJavaElement, Boolean> new_is_self_env = new HashMap<IJavaElement, Boolean>();
					temp_statement_instr_is_self.put(expr, new_is_self_env);
					Map<IJavaElement, Integer> new_env = new HashMap<IJavaElement, Integer>();
					Iterator<IJavaElement> titr = temp_statement_expression_environment_set.iterator();
					while (titr.hasNext())
					{
						IJavaElement ije = titr.next();
						List<IRForOneInstruction> list = irc.GetOneAllIRUnits(ije);
						if (list != null && list.size() > 0)
						{
							int idx = list.size() - 1;
							new_env.put(ije, idx);
							new_is_self_env.put(ije, direct_transfer);
						}
					}
					temp_statement_instr_order.put(expr, new_env);
					TempExpressionOverHandle();
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
					IRForOneMethodInvocation irfomi = (IRForOneMethodInvocation)irc.GetLastIRUnit(source_method_receiver_element);
					UncertainReferenceElement ure = new UncertainReferenceElement(node.toString());
					HandleIJavaElement(ure, node);
					IRGeneratorHelper.AddMethodReturnVirtualReceiveNodeAndSelfDependency(irc, ure, irfomi);
				}
			}
		} else {
			IRGeneratorHelper.GenerateGeneralIR(this, node, IRMeta.MethodInvocation + identifier);
		}
		temp_statement_instr_order.clear();
		temp_statement_instr_is_self.clear();
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
			pre_visit_task.put(node, new Runnable() {
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

	@Override
	public boolean visit(IfStatement node) {
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getExpression(),
						IRMeta.If);
				PushBranchInstructionOrder();
				StatementOverHandle();
			}
		});

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
		PopBranchInstructionOrder();
		super.endVisit(node);
	}

	// highly related to IfStatement.
	@Override
	public boolean visit(ConditionalExpression node) {
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getExpression(),
						IRMeta.If);
				PushBranchInstructionOrder();
			}
		});

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
		PopBranchInstructionOrder();
		super.endVisit(node);
	}

	// loop statements begin.
	@Override
	public boolean visit(WhileStatement node) {
		// ast_block_bind.put(node, new HashSet<IBinding>());
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.put(node.getExpression(), new Runnable() {
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
		post_visit_task.put(node.getExpression(), new Runnable() {
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
				post_visit_task.put(last_expr, new Runnable() {
					@Override
					public void run() {
						HashSet<IJavaElement> temp = this_ref.temp_statement_environment_set;
						this_ref.temp_statement_environment_set = this_ref.temp_statement_expression_environment_set;
						
						IRGeneratorHelper.GenerateGeneralIR(this_ref, exp,
								IRMeta.For_Initial);
						
						TempExpressionOverHandle();
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
				post_visit_task.put(last_expr, new Runnable() {
					@Override
					public void run() {
						HashSet<IJavaElement> temp = this_ref.temp_statement_environment_set;
						this_ref.temp_statement_environment_set = this_ref.temp_statement_expression_environment_set;
						
						IRGeneratorHelper.GenerateGeneralIR(this_ref, exp,
								IRMeta.For_Judge);
						PushBranchInstructionOrder();
						
						TempExpressionOverHandle();
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
				post_visit_task.put(last_expr, new Runnable() {
					@Override
					public void run() {
						HashSet<IJavaElement> temp = this_ref.temp_statement_environment_set;
						this_ref.temp_statement_environment_set = this_ref.temp_statement_expression_environment_set;
						
						IRGeneratorHelper.GenerateGeneralIR(this_ref, exp,
								IRMeta.For_Update);
						
						TempExpressionOverHandle();
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
		post_visit_task.put(node.getExpression(), new Runnable() {
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

	// missed to consider the label and will be considered in the future.
	@Override
	public void endVisit(BreakStatement node) {
		ASTNode n = ASTSearch.FindMostCloseLoopNode(node);
		if (n != null && ast_block_bind.containsKey(n)) {
			IRGeneratorHelper.GenerateNoVariableBindingIR(this, node, ast_block_bind.get(n),
					IRMeta.Break);
		}
	}

	@Override
	public void endVisit(ContinueStatement node) {
		ASTNode n = ASTSearch.FindMostCloseLoopNode(node);
		if (n != null && ast_block_bind.containsKey(n)) {
			IRGeneratorHelper.GenerateNoVariableBindingIR(this, node, ast_block_bind.get(n),
					IRMeta.Continue);
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

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		// Solved. how to redirect? same as assignment.
		Expression ini = node.getInitializer();
		if (ini != null)
		{
			// TODO
		}
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.put(node.getName(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node,
						IRMeta.VariabledDeclare);
				StatementOverHandle();
			}
		});
		return false;
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		// Solved. how to redirect? same as assignment.
		// TODO
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.put(node.getName(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node,
						IRMeta.VariabledDeclare);
				StatementOverHandle();
			}
		});
		return super.visit(node);
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
			pre_visit_task.put(expr, new Runnable() {
				@Override
				public void run() {
					RecordASTNodePreEnvironment(expr);
				}
			});
			post_visit_task.put(expr, new Runnable() {
				@Override
				public void run() {
					boolean direct = CompareASTNodePreEnvironmentToJudgeIfDirectTransfer(expr);
					if (direct)
					{
						Iterator<IJavaElement> titr = temp_statement_environment_set.iterator();
						while (titr.hasNext())
						{
							IJavaElement ije = titr.next();
							IRForOneInstruction iru = irc.GetLastIRUnit(ije);
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
			IRForOneInstruction iru = irc.GetLastIRUnit(ije);
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
		// TODO depd needs to record which iirnode left value depends.
		// TODO assign dependency should be extracted as a stand_alone function because var-declare also will also use it.
		Expression right_val = node.getRightHandSide();
		right_val.accept(this);
		IJavaElement right_jele = WholeExpressionIsAnElement(right_val);
		if (right_jele != null)
		{
			IRForOneInstruction iru = irc.GetLastIRUnit(right_jele);
			iru.SetAcceptType(EdgeBaseType.Self.getType());
		}
		HashSet<IJavaElement> depd = new HashSet<IJavaElement>(temp_statement_environment_set);
		StatementOverHandle();
		Expression left_val = node.getLeftHandSide();
		left_val.accept(this);
		IRGeneratorHelper.GenerateGeneralIR(this, node, IRMeta.LeftHandAssign);
		IJavaElement left_jele = WholeExpressionIsAnElement(left_val);
		if (left_jele != null) {
			IRForOneInstruction iru = irc.GetLastIRUnit(left_jele);
			iru.SetRequireType(EdgeBaseType.Self.getType());
			iru.SetOutConnectionMergeTask(new SkipSelfTask(iru));
		} else {
			// add assign dependency.
			Iterator<IJavaElement> titr = temp_statement_environment_set.iterator();
			while (titr.hasNext()) {
				IJavaElement ijele = titr.next();
				irc.AddAssignDependency(ijele, new HashSet<IJavaElement>(depd));
			}
		}
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
		post_visit_task.put(node.getExpression(), new Runnable() {
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
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				switch_judge_members.push(new HashSet<IJavaElement>(temp_statement_environment_set));
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node, IRMeta.Switch);
				PushBranchInstructionOrder();
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

	private void RemoveSwitchBranch(SwitchStatement node) {
		switch_case_flag.remove(node);
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

		PopSwitchBranch((SwitchStatement) node.getParent());
		Expression expr = node.getExpression();
		if (expr != null) {
			post_visit_task.put(expr, new Runnable() {
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
		switch_judge_members.pop();
		PopSwitchBranch(node);
		RemoveSwitchBranch(node);
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
		post_visit_task.put(node.getLeftOperand(), new Runnable() {
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

	@Override
	public boolean visit(InfixExpression node) {
		IRGeneratorForOneLogicBlock this_ref = this;
		post_visit_task.put(node.getLeftOperand(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(this_ref, node.getLeftOperand(), 
						IRMeta.InfixLeftExpression + node.getOperator().toString());
			}
		});
		return super.visit(node);
	}

	@Override
	public void endVisit(InfixExpression node) {
		IRGeneratorHelper.GenerateGeneralIR(this, node.getRightOperand(),
				IRMeta.InfixRightExpression + node.getOperator().toString());
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
		post_visit_task.put(node.getException(), new Runnable() {
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
		post_visit_task.put(node.getType(), new Runnable() {
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
		post_visit_task.put(node.getType(), new Runnable() {
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
			post_visit_task.put(expr, new Runnable() {
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
		post_visit_task.put(node.getArray(), new Runnable() {
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

	@Override
	public boolean visit(LabeledStatement node) {
		// will do in the future. The current structure does not recognize such
		// minor but close relation.
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
