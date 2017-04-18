package cn.yyx.research.program.ir.generator;

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
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneMethod;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class IRGeneratorForOneLogicBlock extends ASTVisitor {

	public static int max_level = Integer.MAX_VALUE; // Integer.MAX_VALUE partly
														// means infinite.
	
	// TODO variable declarations should be removed, only assignment in it should be retained.
	
	// TODO for return statements, all node related to return should be recorded.
	
	// name must be resolved and ensure it is a variable, a global variable or a
	// type.
	// for method invocation's parameters.
	protected HashMap<IJavaElement, HashMap<ASTNode, Integer>> temp_statement_instr_order = new HashMap<IJavaElement, HashMap<ASTNode, Integer>>();
	protected HashSet<IJavaElement> temp_statement_environment_set = new HashSet<IJavaElement>();
	protected HashMap<IJavaElement, Integer> all_count = new HashMap<IJavaElement, Integer>();
	
	// this variable is initialized in Construction method.
	protected IJavaElement source_method_receiver_element = null;
	
	protected void StatementOverHandle() {
		// no need to do that anymore.
		temp_statement_instr_order.clear();
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

	@Override
	public void endVisit(MethodInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> nlist = (List<Expression>) node.arguments();
		IRGeneratorHelper.GenerateMethodInvocationIR(irc, nlist, node.resolveMethodBinding(), node.getExpression(),
				node.getName().toString(), node, temp_statement_instr_order, temp_statement_environment_set, all_count,
				branchs_var_instr_order.peek());
	}

	@Override
	public void endVisit(SuperMethodInvocation node) {
		TreatSuperClassElement(node);
		@SuppressWarnings("unchecked")
		List<Expression> nlist = (List<Expression>) node.arguments();
		IRGeneratorHelper.GenerateMethodInvocationIR(irc, nlist, node.resolveMethodBinding(), null,
				node.getName().toString(), node, temp_statement_instr_order, temp_statement_environment_set, all_count,
				branchs_var_instr_order.peek());
	}

	@Override
	public void endVisit(SuperConstructorInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> nlist = (List<Expression>) node.arguments();
		IRGeneratorHelper.GenerateMethodInvocationIR(irc, nlist, node.resolveConstructorBinding(), null, "super", node,
				temp_statement_instr_order, temp_statement_environment_set, all_count, branchs_var_instr_order.peek());
	}

	@Override
	public void endVisit(ConstructorInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> nlist = (List<Expression>) node.arguments();
		IRGeneratorHelper.GenerateMethodInvocationIR(irc, nlist, node.resolveConstructorBinding(), null, "this", node,
				temp_statement_instr_order, temp_statement_environment_set, all_count, branchs_var_instr_order.peek());
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		if (node.getAnonymousClassDeclaration() != null) {
			pre_visit_task.put(node, new Runnable() {
				@Override
				public void run() {
					@SuppressWarnings("unchecked")
					List<Expression> nlist = (List<Expression>) node.arguments();
					IRGeneratorHelper.GenerateMethodInvocationIR(irc, nlist, node.resolveConstructorBinding(), null,
							"new#" + node.getType(), node, temp_statement_instr_order, temp_statement_environment_set,
							all_count, branchs_var_instr_order.peek());
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
			IRGeneratorHelper.GenerateMethodInvocationIR(irc, nlist, node.resolveConstructorBinding(), null,
					"new#" + node.getType(), node, temp_statement_instr_order, temp_statement_environment_set,
					all_count, branchs_var_instr_order.peek());
		}
	}

	// handling statements.

	@Override
	public boolean visit(IfStatement node) {
		post_visit_task.put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(irc, node.getExpression(), temp_statement_environment_set,
						all_count, IRMeta.If, branchs_var_instr_order.peek());
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
		post_visit_task.put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(irc, node.getExpression(), temp_statement_environment_set,
						all_count, IRMeta.If, branchs_var_instr_order.peek());
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
		post_visit_task.put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(irc, node.getExpression(), temp_statement_environment_set,
						all_count, IRMeta.While, branchs_var_instr_order.peek());
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
		post_visit_task.put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(irc, node.getExpression(), temp_statement_environment_set,
						all_count, IRMeta.DoWhile, branchs_var_instr_order.peek());
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
						IRGeneratorHelper.GenerateGeneralIR(irc, exp, temp_statement_environment_set, all_count,
								IRMeta.For_Initial, branchs_var_instr_order.peek());
						StatementOverHandle();
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
						IRGeneratorHelper.GenerateGeneralIR(irc, exp, temp_statement_environment_set, all_count,
								IRMeta.For_Judge, branchs_var_instr_order.peek());
						PushBranchInstructionOrder();
						StatementOverHandle();
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
						IRGeneratorHelper.GenerateGeneralIR(irc, exp, temp_statement_environment_set, all_count,
								IRMeta.For_Update, branchs_var_instr_order.peek());
						StatementOverHandle();
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
		super.endVisit(node);
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		post_visit_task.put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(irc, node, temp_statement_environment_set, all_count,
						IRMeta.EnhancedFor, branchs_var_instr_order.peek());
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
			IRGeneratorHelper.GenerateNoVariableBindingIR(node, node.getLabel(), irc, ast_block_bind.get(n),
					IRMeta.Break, branchs_var_instr_order.peek());
		}
	}

	@Override
	public void endVisit(ContinueStatement node) {
		ASTNode n = ASTSearch.FindMostCloseLoopNode(node);
		if (n != null && ast_block_bind.containsKey(n)) {
			IRGeneratorHelper.GenerateNoVariableBindingIR(node, node.getLabel(), irc, ast_block_bind.get(n),
					IRMeta.Continue, branchs_var_instr_order.peek());
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
		post_visit_task.put(node.getName(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(irc, node, temp_statement_environment_set, all_count,
						IRMeta.VariabledDeclare, branchs_var_instr_order.peek());
				StatementOverHandle();
			}
		});
		return super.visit(node);
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		post_visit_task.put(node.getName(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(irc, node, temp_statement_environment_set, all_count,
						IRMeta.VariabledDeclare, branchs_var_instr_order.peek());
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
	public void endVisit(ReturnStatement node) {
		IRGeneratorHelper.GenerateGeneralIR(irc, node, temp_statement_environment_set, all_count, IRMeta.Return,
				branchs_var_instr_order.peek());
	}

	// need to handle data_dependency.
	@Override
	public boolean visit(Assignment node) {
		node.getRightHandSide().accept(this);
		HashSet<IJavaElement> depd = new HashSet<IJavaElement>(temp_statement_environment_set);
		StatementOverHandle();
		node.getLeftHandSide().accept(this);
		IRGeneratorHelper.GenerateGeneralIR(irc, node, temp_statement_environment_set, all_count, IRMeta.LeftHandAssign,
				branchs_var_instr_order.peek());
		// add assign dependency.
		Iterator<IJavaElement> titr = temp_statement_environment_set.iterator();
		while (titr.hasNext()) {
			IJavaElement ijele = titr.next();
			irc.AddAssignDependency(ijele, new HashSet<IJavaElement>(depd));
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
		post_visit_task.put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(irc, node, temp_statement_environment_set, all_count,
						IRMeta.Synchronized, branchs_var_instr_order.peek());
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
		post_visit_task.put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				switch_judge_members.push(new HashSet<IJavaElement>(temp_statement_environment_set));
				IRGeneratorHelper.GenerateGeneralIR(irc, node, temp_statement_environment_set, all_count, IRMeta.Switch,
						branchs_var_instr_order.peek());
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

		PopSwitchBranch((SwitchStatement) node.getParent());

		Expression expr = node.getExpression();
		if (expr != null) {
			post_visit_task.put(expr, new Runnable() {
				@Override
				public void run() {
					IRGeneratorHelper.GenerateGeneralIR(irc, node, temp_statement_environment_set, all_count,
							IRMeta.Switch_Case_Cause, branchs_var_instr_order.peek());
					PushBranchInstructionOrder();
					StatementOverHandle();
				}
			});
		} else {
			Set<IJavaElement> members = switch_judge_members.peek();
			IRGeneratorHelper.GenerateNoVariableBindingIR(node, node, irc, members, IRMeta.Switch_Case_Default,
					branchs_var_instr_order.peek());
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

	private boolean HandleBinding(IBinding ib) {
		if (!BindingManager.QualifiedBinding(ib)) {
			return false;
		}
		IJavaElement jele = ib.getJavaElement();
		return HandleIJavaElement(jele);
	}

	private boolean HandleIJavaElement(IJavaElement jele) {
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
			if (!(jele instanceof ILocalVariable) && !(jele instanceof UnresolvedTypeElement)
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
		return true;
	}

	@Override
	public boolean visit(SimpleName node) {
		IBinding ib = node.resolveBinding();
		HandleBinding(ib);

		return super.visit(node);
	}

	@Override
	public void endVisit(FieldAccess node) {
		IVariableBinding ib = node.resolveFieldBinding();
		if (ib == null || ib.getJavaElement() == null) {
			IRGeneratorHelper.GenerateGeneralIR(irc, node, temp_statement_environment_set, all_count,
					IRMeta.FieldAccess + node.getName().toString(), branchs_var_instr_order.peek());
		}
	}

	@Override
	public void endVisit(SuperFieldAccess node) {
		IVariableBinding ib = node.resolveFieldBinding();
		if (ib == null || ib.getJavaElement() == null) {
			TreatSuperClassElement(node);
			IRGeneratorHelper.GenerateGeneralIR(irc, node, temp_statement_environment_set, all_count,
					IRMeta.FieldAccess + node.getName().toString(), branchs_var_instr_order.peek());
		}
	}

	@Override
	public boolean visit(StringLiteral node) {
		HandleIJavaElement(ConstantUniqueElement.FetchConstantElement(node.toString()));
		return super.visit(node);
	}

	@Override
	public boolean visit(NumberLiteral node) {
		HandleIJavaElement(ConstantUniqueElement.FetchConstantElement(node.toString()));
		return super.visit(node);
	}

	@Override
	public boolean visit(NullLiteral node) {
		HandleIJavaElement(ConstantUniqueElement.FetchConstantElement(node.toString()));
		return super.visit(node);
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		HandleIJavaElement(ConstantUniqueElement.FetchConstantElement(node.toString()));
		return super.visit(node);
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		HandleIJavaElement(ConstantUniqueElement.FetchConstantElement(node.toString()));
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeLiteral node) {
		HandleIJavaElement(ConstantUniqueElement.FetchConstantElement(node.toString()));
		return super.visit(node);
	}

	private void HandleType(IBinding ib, String represent) {
		boolean source_resolved = HandleBinding(ib);
		if (!source_resolved) {
			UnresolvedTypeElement ele = UnresolvedTypeElement.FetchConstantElement(represent);
			HandleIJavaElement(ele);
		}
	}

	@Override
	public boolean visit(ArrayType node) {
		HandleType(node.resolveBinding(), node.toString());
		return false;
	}

	@Override
	public boolean visit(SimpleType node) {
		HandleType(node.resolveBinding(), node.toString());
		return false;
	}

	@Override
	public boolean visit(PrimitiveType node) {
		HandleType(node.resolveBinding(), node.toString());
		return false;
	}

	@Override
	public boolean visit(QualifiedType node) {
		HandleType(node.resolveBinding(), node.toString());
		return false;
	}

	@Override
	public boolean visit(NameQualifiedType node) {
		HandleType(node.resolveBinding(), node.toString());
		return false;
	}

	@Override
	public boolean visit(QualifiedName node) {
		HandleType(node.resolveBinding(), node.toString());
		return false;
	}

	@Override
	public void endVisit(PrefixExpression node) {
		IRGeneratorHelper.GenerateGeneralIR(irc, node, temp_statement_environment_set, all_count,
				IRMeta.Prefix + node.getOperator().toString(), branchs_var_instr_order.peek());
	}

	@Override
	public boolean visit(PostfixExpression node) {
		IRGeneratorHelper.GenerateGeneralIR(irc, node, temp_statement_environment_set, all_count,
				IRMeta.Postfix + node.getOperator().toString(), branchs_var_instr_order.peek());
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
		post_visit_task.put(node.getLeftOperand(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(irc, node.getLeftOperand(), temp_statement_environment_set,
						all_count, IRMeta.InstanceOfExpression, branchs_var_instr_order.peek());
			}
		});
		return super.visit(node);
	}

	@Override
	public void endVisit(InstanceofExpression node) {
		IRGeneratorHelper.GenerateGeneralIR(irc, node.getRightOperand(), temp_statement_environment_set, all_count,
				IRMeta.InstanceOfType, branchs_var_instr_order.peek());
		super.endVisit(node);
	}

	@Override
	public boolean visit(InfixExpression node) {
		post_visit_task.put(node.getLeftOperand(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(irc, node.getLeftOperand(), temp_statement_environment_set,
						all_count, IRMeta.InfixLeftExpression + node.getOperator().toString(),
						branchs_var_instr_order.peek());
			}
		});
		return super.visit(node);
	}

	@Override
	public void endVisit(InfixExpression node) {
		IRGeneratorHelper.GenerateGeneralIR(irc, node.getRightOperand(), temp_statement_environment_set, all_count,
				IRMeta.InfixRightExpression + node.getOperator().toString(), branchs_var_instr_order.peek());
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
		HandleBinding(node.resolveVariable());
		return super.visit(node);
	}

	@Override
	public boolean visit(CatchClause node) {
		post_visit_task.put(node.getException(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(irc, node.getException(), temp_statement_environment_set, all_count,
						IRMeta.CatchClause, branchs_var_instr_order.peek());
			}
		});
		return super.visit(node);
	}

	@Override
	public boolean visit(CastExpression node) {
		post_visit_task.put(node.getType(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(irc, node.getType(), temp_statement_environment_set, all_count,
						IRMeta.CastType, branchs_var_instr_order.peek());
			}
		});
		return super.visit(node);
	}

	@Override
	public void endVisit(CastExpression node) {
		IRGeneratorHelper.GenerateGeneralIR(irc, node.getExpression(), temp_statement_environment_set, all_count,
				IRMeta.CastExpression, branchs_var_instr_order.peek());
	}

	@Override
	public boolean visit(ArrayCreation node) {
		post_visit_task.put(node.getType(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(irc, node.getType(), temp_statement_environment_set, all_count,
						IRMeta.ArrayCreation, branchs_var_instr_order.peek());
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
					IRGeneratorHelper.GenerateGeneralIR(irc, expr, temp_statement_environment_set, all_count,
							IRMeta.ArrayCreationIndex, branchs_var_instr_order.peek());
				}
			});
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		IRGeneratorHelper.GenerateGeneralIR(irc, node, temp_statement_environment_set, all_count, IRMeta.CastExpression,
				branchs_var_instr_order.peek());
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayAccess node) {
		post_visit_task.put(node.getArray(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(irc, node.getArray(), temp_statement_environment_set, all_count,
						IRMeta.Array, branchs_var_instr_order.peek());
			}
		});
		return super.visit(node);
	}

	@Override
	public void endVisit(ArrayAccess node) {
		IRGeneratorHelper.GenerateGeneralIR(irc, node, temp_statement_environment_set, all_count, IRMeta.ArrayIndex,
				branchs_var_instr_order.peek());
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
				HandleIJavaElement(im);
				handled = true;

				IRForOneMethod irfom = IRGeneratorForOneProject.FetchIMethodIR(im);
				IRGeneratorForOneLogicBlock irgfocb = new IRGeneratorForOneLogicBlock(irfom);
				node.accept(irgfocb);
			}
		}
		if (!handled) {
			HandleIJavaElement(UnresolvedLambdaUniqueElement.FetchConstantElement(node.toString(), (IMember)irc.GetScopeIElement(), irc.CopyEnvironment()));
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

	private boolean HandleMethodReferenceStart(IMethodBinding imb) {
		IMethod im = WhetherGoIntoMethodReference(imb);
		if (im != null) {
			HandleIJavaElement(im);
			return false;
		}
		return true;
	}

	private void HandleMethodReferenceEnd(IMethodBinding imb, MethodReference mr, String code) {
		IMethod im = WhetherGoIntoMethodReference(imb);
		if (im == null) {
			IRGeneratorHelper.GenerateGeneralIR(irc, mr, temp_statement_environment_set, all_count,
					IRMeta.MethodReference + code, branchs_var_instr_order.peek());
		}
	}

	@Override
	public boolean visit(ExpressionMethodReference node) {
		return HandleMethodReferenceStart(node.resolveMethodBinding());
	}

	@Override
	public void endVisit(ExpressionMethodReference node) {
		HandleMethodReferenceEnd(node.resolveMethodBinding(), node, node.toString());
		super.endVisit(node);
	}

	@Override
	public boolean visit(CreationReference node) {
		return HandleMethodReferenceStart(node.resolveMethodBinding());
	}

	@Override
	public void endVisit(CreationReference node) {
		HandleMethodReferenceEnd(node.resolveMethodBinding(), node, node.toString());
		super.endVisit(node);
	}

	@Override
	public boolean visit(TypeMethodReference node) {
		return HandleMethodReferenceStart(node.resolveMethodBinding());
	}

	@Override
	public void endVisit(TypeMethodReference node) {
		HandleMethodReferenceEnd(node.resolveMethodBinding(), node, node.toString());
		super.endVisit(node);
	}

	@Override
	public boolean visit(SuperMethodReference node) {
		boolean continue_visit = HandleMethodReferenceStart(node.resolveMethodBinding());
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
				HandleIJavaElement(UnresolvedTypeElement.FetchConstantElement(td.getName().toString()));
				return true;
			} else {
				HandleIJavaElement(it);
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
