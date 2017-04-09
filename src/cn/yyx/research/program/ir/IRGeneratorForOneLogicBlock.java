package cn.yyx.research.program.ir;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.*;

import cn.yyx.research.program.ir.ast.ASTSearch;
import cn.yyx.research.program.ir.bind.BindingManager;
import cn.yyx.research.program.ir.bind.YConstantBinding;
import cn.yyx.research.program.ir.storage.highlevel.IRCode;
import cn.yyx.research.program.ir.storage.highlevel.IRForOneMethod;
import cn.yyx.research.program.ir.storage.lowlevel.IRForOneJavaInstruction;
import cn.yyx.research.program.ir.task.IRTask;

public class IRGeneratorForOneLogicBlock extends ASTVisitor {

	public static int max_level = Integer.MAX_VALUE; // Integer.MAX_VALUE partly means infinite.

	// name must be resolved and ensure it is a variable, a global variable or a
	// type.
	protected HashMap<IMember, HashMap<ASTNode, Integer>> temp_statement_instr_order_set = new HashMap<IMember, HashMap<ASTNode, Integer>>();
	protected HashMap<IMember, Integer> temp_statement_environment_set = new HashMap<IMember, Integer>();
	
	protected void StatementOverHandle() {
		// no need to do that anymore.
		temp_statement_instr_order_set.clear();
		temp_statement_environment_set.clear();
	}

	protected Stack<HashMap<IMember, Integer>> branchs_var_instr_order = new Stack<HashMap<IMember, Integer>>();
	
	protected void PushBranchInstructionOrder() {
		HashMap<IMember, Integer> t_hash = new HashMap<IMember, Integer>();
		Set<IMember> tkeys = temp_statement_environment_set.keySet();
		Iterator<IMember> titr = tkeys.iterator();
		while (titr.hasNext())
		{
			IMember im = titr.next();
			List<IRForOneJavaInstruction> ls = irc.GetOneAllIRUnits(im);
			if (ls != null && ls.size() > 0)
			{
				int order = ls.size()-1;
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

//	protected Map<ASTNode, HashSet<IMember>> ast_block_bind = new HashMap<ASTNode, HashSet<IMember>>();

	// protected Stack<HashSet<IBinding>> switch_case_bind = new Stack<HashSet<IBinding>>();
	// protected Stack<LinkedList<ASTNode>> switch_case = new Stack<LinkedList<ASTNode>>();

	public IRGeneratorForOneLogicBlock(IRCode irc) {
		this.irc = irc;
	}
	
//	public Queue<IRTask> GetUndoneTasks() {
//		return undone_tasks;
//	}

	@Override
	public void preVisit(ASTNode node) {
		if (pre_visit_task.containsKey(node)) {
			Runnable run = pre_visit_task.get(node);
			run.run();
		}
		super.preVisit(node);
	}

	// post handling statements.
	@Override
	public void postVisit(ASTNode node) {
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
				if (ije instanceof IMember)
				{
					irc.AddParameter((IMember)ije);
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
		IRGeneratorHelper.HandleMethodInvocation(irc, nlist, node.resolveMethodBinding(), node.getExpression(), node.getName().toString(),
				node, temp_statement_instr_order_set, temp_statement_environment_set);
	}
	
	@Override
	public void endVisit(SuperMethodInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> nlist = (List<Expression>) node.arguments();
		IRGeneratorHelper.HandleMethodInvocation(irc, nlist, node.resolveMethodBinding(), null, node.getName().toString(), node, temp_statement_instr_order_set, temp_statement_environment_set);
	}

	@Override
	public void endVisit(SuperConstructorInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> nlist = (List<Expression>) node.arguments();
		IRGeneratorHelper.HandleMethodInvocation(irc, nlist, node.resolveConstructorBinding(), null, "super", node, temp_statement_instr_order_set, temp_statement_environment_set);
	}

	@Override
	public void endVisit(ConstructorInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> nlist = (List<Expression>) node.arguments();
		IRGeneratorHelper.HandleMethodInvocation(irc, nlist, node.resolveConstructorBinding(), null, "this", node, temp_statement_instr_order_set, temp_statement_environment_set);
	}
	
	@Override
	public boolean visit(ClassInstanceCreation node) {
		if (node.getAnonymousClassDeclaration() != null)
		{
			pre_visit_task.put(node, new Runnable() {
				@Override
				public void run() {
					@SuppressWarnings("unchecked")
					List<Expression> nlist = (List<Expression>) node.arguments();
					IRGeneratorHelper.HandleMethodInvocation(irc, nlist, node.resolveConstructorBinding(), null, "new#"+node.getType(), node, temp_statement_instr_order_set, temp_statement_environment_set);
				}
			});	
		}
		return super.visit(node);
	}
	
	@Override
	public void endVisit(ClassInstanceCreation node) {
		if (node.getAnonymousClassDeclaration() == null)
		{
			@SuppressWarnings("unchecked")
			List<Expression> nlist = (List<Expression>) node.arguments();
			IRGeneratorHelper.HandleMethodInvocation(irc, nlist, node.resolveConstructorBinding(), null, "new#"+node.getType(), node, temp_statement_instr_order_set, temp_statement_environment_set);
		}
	}
	
	// handling statements.

	@Override
	public boolean visit(IfStatement node) {
		post_visit_task.put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(node.getExpression(), node.getExpression(), irc, temp_statement_environment_set, IRMeta.If);
				PushBranchInstructionOrder();
				StatementOverHandle();
			}
		});
		
//		Statement thenstat = node.getThenStatement();
//		if (thenstat != null)
//		{
//			ast_block_bind.put(thenstat, new HashSet<IBinding>());
//			post_visit_task.put(thenstat, new Runnable() {
//				@Override
//				public void run() {
//					IRGeneratorHelper.GenerateNoVariableBindingIR(thenstat, thenstat, irc, ast_block_bind.get(thenstat), IRMeta.IfThen);
//					ast_block_bind.remove(thenstat);
//				}
//			});
//		}
//		
//		Statement elsestat = node.getElseStatement();
//		if (elsestat != null)
//		{
//			ast_block_bind.put(elsestat, new HashSet<IBinding>());
//			post_visit_task.put(elsestat, new Runnable() {
//				@Override
//				public void run() {
//					IRGeneratorHelper.GenerateNoVariableBindingIR(elsestat, elsestat, irc, ast_block_bind.get(elsestat), IRMeta.IfElse);
//					ast_block_bind.remove(elsestat);
//				}
//			});
//		}
		
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
				IRGeneratorHelper.GenerateGeneralIR(node.getExpression(), node.getExpression(), irc, temp_statement_environment_set, IRMeta.If);
				PushBranchInstructionOrder();
			}
		});
		
//		post_visit_task.put(node.getThenExpression(), new Runnable() {
//			@Override
//			public void run() {
//				IRGeneratorHelper.GenerateGeneralIR(node.getThenExpression(), node.getThenExpression(), irc, temp_statement_environment_set, IRMeta.IfThen);
//			}
//		});
//		
//		post_visit_task.put(node.getElseExpression(), new Runnable() {
//			@Override
//			public void run() {
//				IRGeneratorHelper.GenerateGeneralIR(node.getElseExpression(), node.getElseExpression(), irc, temp_statement_environment_set, IRMeta.IfElse);
//			}
//		});
		
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
				IRGeneratorHelper.GenerateGeneralIR(node, node.getExpression(), irc, temp_statement_environment_set,
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
		post_visit_task.put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(node, node.getExpression(), irc, temp_statement_environment_set,
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
		ast_block_bind.put(node, new HashSet<IBinding>());

		Expression last_expr = null;
		@SuppressWarnings("unchecked")
		List<Expression> ini_list = node.initializers();
		if (ini_list != null) {
			last_expr = ini_list.get(ini_list.size() - 1);
		}
		Expression expr = node.getExpression();
		if (expr != null) {
			last_expr = expr;
		}
		@SuppressWarnings("unchecked")
		List<Expression> upd_list = node.updaters();
		if (upd_list != null) {
			last_expr = upd_list.get(upd_list.size() - 1);
		}
		final ASTNode exp = last_expr;
		if (last_expr != null) {
			post_visit_task.put(last_expr, new Runnable() {
				@Override
				public void run() {
					IRGeneratorHelper.GenerateGeneralIR(node, exp, irc, temp_statement_environment_set, IRMeta.For);
					StatementOverHandle();
				}
			});
		}

		return super.visit(node);
	}

	@Override
	public void endVisit(ForStatement node) {
		ast_block_bind.remove(node);
		super.endVisit(node);
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		ast_block_bind.put(node, new HashSet<IBinding>());

		post_visit_task.put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(node, node.getExpression(), irc, temp_statement_environment_set,
						IRMeta.EnhancedFor);
				StatementOverHandle();
			}
		});
		return super.visit(node);
	}

	@Override
	public void endVisit(EnhancedForStatement node) {
		ast_block_bind.remove(node);
		super.endVisit(node);
	}

	// loop statements end.

	// missed to consider the label and will be considered in the future.
	@Override
	public void endVisit(BreakStatement node) {
		ASTNode n = ASTSearch.FindMostCloseLoopNode(node);
		if (n != null && ast_block_bind.containsKey(n)) {
			IRGeneratorHelper.GenerateNoVariableBindingIR(node, node.getLabel(), irc, ast_block_bind.get(n), IRMeta.Break);
		}
	}

	@Override
	public void endVisit(ContinueStatement node) {
		ASTNode n = ASTSearch.FindMostCloseLoopNode(node);
		if (n != null && ast_block_bind.containsKey(n)) {
			IRGeneratorHelper.GenerateNoVariableBindingIR(node, node.getLabel(), irc, ast_block_bind.get(n),
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
		post_visit_task.put(node.getName(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(node, node.getName(), irc, temp_statement_environment_set,
						IRMeta.VariabledDeclare);
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
				IRGeneratorHelper.GenerateGeneralIR(node, node.getName(), irc, temp_statement_environment_set,
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
	public void endVisit(ReturnStatement node) {
		IRGeneratorHelper.GenerateGeneralIR(node, node, irc, temp_statement_environment_set, IRMeta.Return);
	}

	// need to handle data_dependency.
	@Override
	public boolean visit(Assignment node) {
		post_visit_task.put(node.getLeftHandSide(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(node, node.getLeftHandSide(), irc, temp_statement_environment_set,
						IRMeta.LeftHandAssign);
			}
		});
		post_visit_task.put(node.getRightHandSide(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(node, node.getRightHandSide(), irc, temp_statement_environment_set,
						IRMeta.RightHandAssign);
			}
		});
		return super.visit(node);
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		post_visit_task.put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(node, node.getExpression(), irc, temp_statement_environment_set,
						IRMeta.Synchronized);
				StatementOverHandle();
			}
		});
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchStatement node) {
		switch_case_bind.push(new HashSet<IBinding>());
		switch_case.push(new LinkedList<ASTNode>());

		post_visit_task.put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(node, node.getExpression(), irc, temp_statement_environment_set,
						IRMeta.Switch);
			}
		});
		return super.visit(node);
	}

	// closely related expressions.
	@Override
	public boolean visit(SwitchCase node) {
		HashSet<IBinding> binds = switch_case_bind.peek();
		LinkedList<ASTNode> slist = switch_case.peek();
		if (slist.size() > 0) {
			ASTNode sc = slist.get(0);
			IRGeneratorHelper.GenerateSwitchCaseIR(node, sc, irc, binds);

			slist.removeFirst();
			binds.clear();
		}
		slist.add(node);
		return super.visit(node);
	}

	@Override
	public void endVisit(SwitchCase node) {
		IRGeneratorHelper.GenerateGeneralIR(node, node.getExpression(), irc, temp_statement_environment_set,
				IRMeta.Switch_Case_Cause);
		super.endVisit(node);
	}

	@Override
	public void endVisit(SwitchStatement node) {
		HashSet<IBinding> binds = switch_case_bind.pop();
		LinkedList<ASTNode> slist = switch_case.pop();
		if (slist.size() > 0) {
			ASTNode sc = switch_case.peek().get(0);
			IRGeneratorHelper.GenerateSwitchCaseIR(node, sc, irc, binds);
		}

		binds.clear();
		slist.clear();
	}

	// handling expressions.

	private void HandleBinding(IBinding ib) {
		// handle loop_bind, just for no variable bind statements such as
		// break and continue.
		if (!BindingManager.QualifiedBinding(ib)) {
			return;
		}
		Set<ASTNode> ks = ast_block_bind.keySet();
		Iterator<ASTNode> kitr = ks.iterator();
		while (kitr.hasNext()) {
			ASTNode an = kitr.next();
			HashSet<IBinding> set = ast_block_bind.get(an);
			set.add(ib);
		}

		// handle switch_case_bind
		if (!switch_case_bind.isEmpty()) {
			switch_case_bind.peek().add(ib);
		}

		// next isolated tasks.
		temp_statement_environment_set.put(ib, -1);
	}

	@Override
	public boolean visit(SimpleName node) {
		IBinding ib = node.resolveBinding();
		HandleBinding(ib);

		return super.visit(node);
	}
	
	@Override
	public boolean visit(FieldAccess node) {
		// no need to do anything.
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		// no need to do anything.
		return super.visit(node);
	}

	@Override
	public boolean visit(StringLiteral node) {
		HandleBinding(new YConstantBinding(node.toString(), node.resolveTypeBinding(),
				node.resolveConstantExpressionValue()));
		return super.visit(node);
	}

	@Override
	public boolean visit(NumberLiteral node) {
		HandleBinding(new YConstantBinding(node.toString(), node.resolveTypeBinding(),
				node.resolveConstantExpressionValue()));
		return super.visit(node);
	}

	@Override
	public boolean visit(NullLiteral node) {
		HandleBinding(new YConstantBinding(node.toString(), node.resolveTypeBinding(),
				node.resolveConstantExpressionValue()));
		return super.visit(node);
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		HandleBinding(new YConstantBinding(node.toString(), node.resolveTypeBinding(),
				node.resolveConstantExpressionValue()));
		return super.visit(node);
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		HandleBinding(new YConstantBinding(node.toString(), node.resolveTypeBinding(),
				node.resolveConstantExpressionValue()));
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeLiteral node) {
		HandleBinding(new YConstantBinding(node.toString(), node.resolveTypeBinding(),
				node.resolveConstantExpressionValue()));
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayType node) {
		HandleBinding(node.resolveBinding());
		return super.visit(node);
	}

	@Override
	public boolean visit(SimpleType node) {
		HandleBinding(node.resolveBinding());
		return super.visit(node);
	}

	@Override
	public boolean visit(PrimitiveType node) {
		HandleBinding(node.resolveBinding());
		return super.visit(node);
	}

	@Override
	public boolean visit(QualifiedType node) {
		HandleBinding(node.resolveBinding());
		return super.visit(node);
	}
	
	@Override
	public boolean visit(NameQualifiedType node) {
		HandleBinding(node.resolveBinding());
		return super.visit(node);
	}

	@Override
	public boolean visit(QualifiedName node) {
		HandleBinding(node.resolveBinding());
		return super.visit(node);
	}

	@Override
	public void endVisit(PrefixExpression node) {
		IRGeneratorHelper.GenerateGeneralIR(node, node.getOperand(), irc, temp_statement_environment_set,
				IRMeta.Prefix + node.getOperator().toString());
	}

	@Override
	public boolean visit(PostfixExpression node) {
		IRGeneratorHelper.GenerateGeneralIR(node, node.getOperand(), irc, temp_statement_environment_set,
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
		post_visit_task.put(node.getLeftOperand(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(node.getLeftOperand(), node.getLeftOperand(), irc,
						temp_statement_environment_set, IRMeta.InstanceOfExpression);
			}
		});
		return super.visit(node);
	}

	@Override
	public void endVisit(InstanceofExpression node) {
		IRGeneratorHelper.GenerateGeneralIR(node.getRightOperand(), node.getRightOperand(), irc, temp_statement_environment_set,
				IRMeta.InstanceOfType);
		super.endVisit(node);
	}

	@Override
	public boolean visit(InfixExpression node) {
		post_visit_task.put(node.getLeftOperand(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(node.getLeftOperand(), node.getLeftOperand(), irc,
						temp_statement_environment_set, IRMeta.InfixLeftExpression + node.getOperator().toString());
			}
		});
		return super.visit(node);
	}
	
	@Override
	public void endVisit(InfixExpression node) {
		IRGeneratorHelper.GenerateGeneralIR(node.getRightOperand(), node.getRightOperand(), irc, temp_statement_environment_set,
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
		HandleBinding(node.resolveVariable());
		return super.visit(node);
	}

	@Override
	public boolean visit(CatchClause node) {
		post_visit_task.put(node.getException(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(node.getException(), node.getException(), irc, temp_statement_environment_set,
						IRMeta.CatchClause);
			}
		});
		return super.visit(node);
	}
	
	@Override
	public boolean visit(CastExpression node) {
		post_visit_task.put(node.getType(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(node.getType(), node.getType(), irc, temp_statement_environment_set,
						IRMeta.CastType);
			}
		});
		return super.visit(node);
	}

	@Override
	public void endVisit(CastExpression node) {
		IRGeneratorHelper.GenerateGeneralIR(node.getExpression(), node.getExpression(), irc, temp_statement_environment_set,
				IRMeta.CastExpression);
	}
	
	@Override
	public boolean visit(ArrayCreation node) {
		post_visit_task.put(node.getType(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(node.getType(), node.getType(), irc, temp_statement_environment_set,
						IRMeta.ArrayCreation);
			}
		});
		@SuppressWarnings("unchecked")
		List<Expression> nlist = node.dimensions();
		Iterator<Expression> itr = nlist.iterator();
		while (itr.hasNext())
		{
			Expression expr = itr.next();
			post_visit_task.put(expr, new Runnable() {
				@Override
				public void run() {
					IRGeneratorHelper.GenerateGeneralIR(expr, expr, irc, temp_statement_environment_set,
							IRMeta.ArrayCreationIndex);
				}
			});
		}
		return super.visit(node);
	}
	
	@Override
	public boolean visit(ArrayInitializer node) {
		IRGeneratorHelper.GenerateGeneralIR(node, node, irc, temp_statement_environment_set,
				IRMeta.CastExpression);
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayAccess node) {
		post_visit_task.put(node.getArray(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(node.getArray(), node.getArray(), irc, temp_statement_environment_set,
						IRMeta.Array);
			}
		});
		return super.visit(node);
	}
	
	@Override
	public void endVisit(ArrayAccess node) {
		IRGeneratorHelper.GenerateGeneralIR(node, node, irc, temp_statement_environment_set,
				IRMeta.ArrayIndex);
		super.endVisit(node);
	}

	@Override
	public boolean visit(ThisExpression node) {
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(LambdaExpression node) {
		// TODO Auto-generated method stub
		IMethodBinding imb = node.resolveMethodBinding();
		if (imb != null) {
			IJavaElement jele = imb.getJavaElement();
			if (jele != null && jele instanceof IMethod) {
				IMethod im = (IMethod)jele;
				
			} else {
				
			}
		} else {
			
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(ExpressionMethodReference node) {
		// TODO Auto-generated method stub
		
		return super.visit(node);
	}

	@Override
	public boolean visit(CreationReference node) {
		// TODO Auto-generated method stub
		
		return super.visit(node);
	}
	
	@Override
	public boolean visit(TypeMethodReference node) {
		// TODO Auto-generated method stub
		
		return super.visit(node);
	}
	
	@Override
	public boolean visit(SuperMethodReference node) {
		// TODO Auto-generated method stub
		
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
	
	// switch such branch, how to model?

	// TODO re-check all codes, be sure the scope to search the bind.
	
	// TODO remember to handle unresolved type or method invocation to its raw name.
	
	// TODO remember to check whether temporarily appeared variable bindings such as field_access/super_field_access are properly handled.
	
	// TODO remember to handle null resolved binding.
	
	// TODO undone tasks are not handled.
	
	public static int GetMaxLevel() {
		return max_level;
	}
	
	public IRCode GetGeneration() {
		return irc;
	}

}
