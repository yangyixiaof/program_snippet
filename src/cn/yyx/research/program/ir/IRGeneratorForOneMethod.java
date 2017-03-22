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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.*;

import cn.yyx.research.program.ir.ast.ASTSearch;
import cn.yyx.research.program.ir.bind.YConstantBinding;
import cn.yyx.research.program.ir.method.IRForOneMethod;

public class IRGeneratorForOneMethod extends ASTVisitor {

	private static int max_level = -1; // -1 means infinite.

	// name must be resolved and ensure it is a variable, a global variable or a
	// type.
	private Map<IBinding, Integer> temp_statement_set = new HashMap<IBinding, Integer>();

	private void StatementOverHandle() {
		// no need to do that anymore.
		temp_statement_set.clear();
	}

	private IRForOneMethod irfom = null;

	private Queue<IRTask> undone_tasks = new LinkedList<IRTask>();

	private Map<ASTNode, Runnable> post_visit_task = new HashMap<ASTNode, Runnable>();

	private Map<ASTNode, Runnable> pre_visit_task = new HashMap<ASTNode, Runnable>();

	private Map<ASTNode, HashSet<IBinding>> loop_bind = new HashMap<ASTNode, HashSet<IBinding>>();

	private Stack<HashSet<IBinding>> switch_case_bind = new Stack<HashSet<IBinding>>();
	private Stack<LinkedList<ASTNode>> switch_case = new Stack<LinkedList<ASTNode>>();

	public IRGeneratorForOneMethod(IMethod im) {
		this.irfom = new IRForOneMethod(im);
	}

	public IRGeneratorForOneMethod(int max_level, IMethod im) {
		IRGeneratorForOneMethod.max_level = max_level;
		this.irfom = new IRForOneMethod(im);
	}

	public Queue<IRTask> GetUndoneTasks() {
		return undone_tasks;
	}

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
		int idx = 0;
		while (itr.hasNext()) {
			idx++;
			SingleVariableDeclaration svd = itr.next();
			SimpleName sn = svd.getName();
			IBinding ib = sn.resolveBinding();
			if ((ib != null) && (ib instanceof IVariableBinding)) {
				IVariableBinding ivb = (IVariableBinding) ib;
				irfom.PutParameterPrder(ivb, idx);
			}
		}
		return super.visit(node);
	}

	private void HandleMethodInvocation(List<Expression> nlist, IMethodBinding imb, Expression expr, String identifier,
			ASTNode node) {
		// initialize parameters of the node.
		Map<IBinding, Integer> invoke_parameter_order = new HashMap<IBinding, Integer>();
		// @SuppressWarnings("unchecked")
		// List<Expression> nlist = node.arguments();
		Iterator<Expression> itr = nlist.iterator();
		int idx = 0;
		while (itr.hasNext()) {
			idx++;
			Expression e = itr.next();
			if (e instanceof Name) {
				Name n = (Name) e;
				IBinding nbind = n.resolveBinding();
				if (nbind != null && nbind instanceof ITypeBinding && nbind instanceof IVariableBinding) {
					invoke_parameter_order.put(nbind, idx);
				}
			}
		}

		IBinding ib = null;
		// IMethodBinding imb = node.resolveMethodBinding();
		// Expression expr = node.getExpression();
		String code = identifier.toString();
		if (expr == null) {
			// set to meta--invoke other user defined function.
			code = IRMeta.User_Defined_Function;
		} else {
			// set data_dependency in IRForOneMethod.
			if (expr instanceof Name) {
				Name n = (Name) expr;
				ib = n.resolveBinding();
				if (ib instanceof ITypeBinding || ib instanceof IVariableBinding) {
					irfom.AddDataDependency(ib, invoke_parameter_order.keySet());
				}
			}
		}

		if (imb == null || !imb.getDeclaringClass().isFromSource()) {
			// null or is from binary.
			IRGeneratorHelper.GenerateGeneralIR(node, node, irfom, temp_statement_set, code);
		} else {
			// is from source.
			// need to be handled specifically.
			if (ib != null) {
				code = IRMeta.User_Defined_Function;
				IRGeneratorHelper.GenerateSourceMethodInvocationIR(ib, imb, node, node, irfom, invoke_parameter_order,
						code);
			}
		}
	}

	@Override
	public void endVisit(MethodInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> nlist = (List<Expression>) node.arguments();
		HandleMethodInvocation(nlist, node.resolveMethodBinding(), node.getExpression(), node.getName().toString(),
				node);
	}

	// handling statements.

	@Override
	public boolean visit(IfStatement node) {
		post_visit_task.put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(node, node.getExpression(), irfom, temp_statement_set, IRMeta.If);
				StatementOverHandle();
			}
		});
		return super.visit(node);
	}

	// loop statements begin.
	@Override
	public boolean visit(WhileStatement node) {
		loop_bind.put(node, new HashSet<IBinding>());

		post_visit_task.put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(node, node.getExpression(), irfom, temp_statement_set,
						IRMeta.While);
				StatementOverHandle();
			}
		});
		return super.visit(node);
	}

	@Override
	public void endVisit(WhileStatement node) {
		loop_bind.remove(node);
		super.endVisit(node);
	}

	@Override
	public boolean visit(ForStatement node) {
		loop_bind.put(node, new HashSet<IBinding>());

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
					IRGeneratorHelper.GenerateGeneralIR(node, exp, irfom, temp_statement_set, IRMeta.For);
					StatementOverHandle();
				}
			});
		}

		return super.visit(node);
	}

	@Override
	public void endVisit(ForStatement node) {
		loop_bind.remove(node);
		super.endVisit(node);
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		loop_bind.put(node, new HashSet<IBinding>());

		post_visit_task.put(node.getExpression(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(node, node.getExpression(), irfom, temp_statement_set,
						IRMeta.EnhancedFor);
				StatementOverHandle();
			}
		});
		return super.visit(node);
	}

	@Override
	public void endVisit(EnhancedForStatement node) {
		loop_bind.remove(node);
		super.endVisit(node);
	}

	// loop statements end.

	// missed to consider the label and will be considered in the future.
	@Override
	public void endVisit(BreakStatement node) {
		ASTNode n = ASTSearch.FindMostCloseLoopNode(node);
		if (n != null && loop_bind.containsKey(n)) {
			IRGeneratorHelper.GenerateNoVariableBindingIR(node, node.getLabel(), irfom, loop_bind.get(n), IRMeta.Break);
		}
	}

	@Override
	public void endVisit(ContinueStatement node) {
		ASTNode n = ASTSearch.FindMostCloseLoopNode(node);
		if (n != null && loop_bind.containsKey(n)) {
			IRGeneratorHelper.GenerateNoVariableBindingIR(node, node.getLabel(), irfom, loop_bind.get(n),
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
				IRGeneratorHelper.GenerateGeneralIR(node, node.getName(), irfom, temp_statement_set,
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
				IRGeneratorHelper.GenerateGeneralIR(node, node.getName(), irfom, temp_statement_set,
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
		IRGeneratorHelper.GenerateGeneralIR(node, node, irfom, temp_statement_set, IRMeta.Return);
	}

	// need to handle data_dependency.
	@Override
	public boolean visit(Assignment node) {
		post_visit_task.put(node.getLeftHandSide(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(node, node.getLeftHandSide(), irfom, temp_statement_set,
						IRMeta.LeftHandAssign);
			}
		});
		post_visit_task.put(node.getRightHandSide(), new Runnable() {
			@Override
			public void run() {
				IRGeneratorHelper.GenerateGeneralIR(node, node.getRightHandSide(), irfom, temp_statement_set,
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
				IRGeneratorHelper.GenerateGeneralIR(node, node.getExpression(), irfom, temp_statement_set,
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
				IRGeneratorHelper.GenerateGeneralIR(node, node.getExpression(), irfom, temp_statement_set,
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
			IRGeneratorHelper.GenerateSwitchCaseIR(node, sc, irfom, binds);

			slist.removeFirst();
			binds.clear();
		}
		slist.add(node);
		return super.visit(node);
	}

	@Override
	public void endVisit(SwitchCase node) {
		IRGeneratorHelper.GenerateGeneralIR(node, node.getExpression(), irfom, temp_statement_set,
				IRMeta.Switch_Case_Cause);
		super.endVisit(node);
	}

	@Override
	public void endVisit(SwitchStatement node) {
		HashSet<IBinding> binds = switch_case_bind.pop();
		LinkedList<ASTNode> slist = switch_case.pop();
		if (slist.size() > 0) {
			ASTNode sc = switch_case.peek().get(0);
			IRGeneratorHelper.GenerateSwitchCaseIR(node, sc, irfom, binds);
		}

		binds.clear();
		slist.clear();
	}

	// handling expressions.

	private void HandleBinding(IBinding ib) {
		// handle loop_bind, just for no variable bind statements such as
		// break and continue.
		Set<ASTNode> ks = loop_bind.keySet();
		Iterator<ASTNode> kitr = ks.iterator();
		while (kitr.hasNext()) {
			ASTNode an = kitr.next();
			HashSet<IBinding> set = loop_bind.get(an);
			set.add(ib);
		}

		// handle switch_case_bind
		if (!switch_case_bind.isEmpty()) {
			switch_case_bind.peek().add(ib);
		}

		// next isolated tasks.
		temp_statement_set.put(ib, -1);
	}

	@Override
	public boolean visit(SimpleName node) {
		IBinding ib = node.resolveBinding();
		if (ib != null && (ib instanceof ITypeBinding || ib instanceof IVariableBinding)) {
			HandleBinding(ib);
		}

		return super.visit(node);
	}

	@Override
	public void endVisit(SuperMethodInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> nlist = (List<Expression>) node.arguments();
		HandleMethodInvocation(nlist, node.resolveMethodBinding(), null, node.getName().toString(), node);
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		// no need to do anything.
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		@SuppressWarnings("unchecked")
		List<Expression> nlist = (List<Expression>) node.arguments();
		HandleMethodInvocation(nlist, node.resolveConstructorBinding(), null, "super", node);
		return super.visit(node);
	}

	@Override
	public boolean visit(StringLiteral node) {
		HandleBinding(new YConstantBinding(node.toString(), node.resolveTypeBinding(), node.resolveConstantExpressionValue()));
		return super.visit(node);
	}

	@Override
	public boolean visit(NumberLiteral node) {
		HandleBinding(new YConstantBinding(node.toString(), node.resolveTypeBinding(), node.resolveConstantExpressionValue()));
		return super.visit(node);
	}

	@Override
	public boolean visit(NullLiteral node) {
		HandleBinding(new YConstantBinding(node.toString(), node.resolveTypeBinding(), node.resolveConstantExpressionValue()));
		return super.visit(node);
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		HandleBinding(new YConstantBinding(node.toString(), node.resolveTypeBinding(), node.resolveConstantExpressionValue()));
		return super.visit(node);
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		HandleBinding(new YConstantBinding(node.toString(), node.resolveTypeBinding(), node.resolveConstantExpressionValue()));
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeLiteral node) {
		HandleBinding(new YConstantBinding(node.toString(), node.resolveTypeBinding(), node.resolveConstantExpressionValue()));
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
	public boolean visit(QualifiedName node) {
		HandleBinding(node.resolveBinding());
		return super.visit(node);
	}

	@Override
	public boolean visit(PrefixExpression node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(PostfixExpression node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(InfixExpression node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		// do not need to handle.
		return super.visit(node);
	}
	
	@Override
	public boolean visit(Initializer node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(FieldDeclaration node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldAccess node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		// do not need handle it.
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(CatchClause node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(CastExpression node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayCreation node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayAccess node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(Modifier node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(ThisExpression node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperMethodReference node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(LambdaExpression node) {
		// TODO Auto-generated method stub
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
	public boolean visit(NameQualifiedType node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(Dimension node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		// do not need to handle.
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeMethodReference node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeParameter node) {
		// do not need to handle.
		return super.visit(node);
	}

	// do nothing.

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

	public static int GetMaxLevel() {
		return max_level;
	}

}
