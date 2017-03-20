package cn.yyx.research.program.ir;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.*;

import cn.yyx.research.program.ir.method.IRForOneMethod;

public class IRGeneratorForOneMethod extends ASTVisitor {
	
	private static int max_level = -1; // -1 means infinite.
	
	// name must be resolved and ensure it is a variable, a global variable or a type.
	private Map<IBinding, Integer> temp_statement_set = new HashMap<IBinding, Integer>();
	
	private IRForOneMethod irfom = null;
	
	private Queue<IRTask> undone_tasks = new LinkedList<IRTask>();
	
	public IRGeneratorForOneMethod(IMethod im) {
		this.irfom = new IRForOneMethod(im);
	}
	
	public IRGeneratorForOneMethod(int max_level, IMethod im) {
		IRGeneratorForOneMethod.max_level = max_level;
		this.irfom = new IRForOneMethod(im);
	}
	
	public Queue<IRTask> GetUndoneTasks()
	{
		return undone_tasks;
	}
	
	// post handling statements.
	@Override
	public void postVisit(ASTNode node) {
		if (node instanceof Statement)
		{
			temp_statement_set.clear();
		}
		super.postVisit(node);
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		@SuppressWarnings("unchecked")
		List<SingleVariableDeclaration> svds = node.parameters();
		Iterator<SingleVariableDeclaration> itr = svds.iterator();
		int idx = 0;
		while (itr.hasNext())
		{
			idx++;
			SingleVariableDeclaration svd = itr.next();
			SimpleName sn = svd.getName();
			IBinding ib = sn.resolveBinding();
			if ((ib != null) && (ib instanceof IVariableBinding))
			{
				IVariableBinding ivb = (IVariableBinding)ib;
				irfom.PutParameterPrder(ivb, idx);
			}
		}
		return super.visit(node);
	}

	@Override
	public void endVisit(MethodInvocation node) {
		
		// initialize parameters of the node.
		Map<IBinding, Integer> invoke_parameter_order = new HashMap<IBinding, Integer>();
		@SuppressWarnings("unchecked")
		List<Expression> nlist = node.arguments();
		Iterator<Expression> itr = nlist.iterator();
		int idx = 0;
		while (itr.hasNext())
		{
			idx++;
			Expression e = itr.next();
			if (e instanceof Name)
			{
				Name n = (Name)e;
				IBinding nbind = n.resolveBinding();
				if (nbind != null && nbind instanceof ITypeBinding && nbind instanceof IVariableBinding)
				{
					invoke_parameter_order.put(nbind, idx);
				}
			}
		}
		
		IBinding ib = null;
		IMethodBinding imb = node.resolveMethodBinding();
		Expression expr = node.getExpression();
		String code = node.getName().toString();
		if (expr == null ) {
			// set to meta--invoke other user defined function.
			code = IRMeta.User_Defined_Function;
		} else {
			// set data_dependency in IRForOneMethod.
			if (expr instanceof Name)
			{
				Name n = (Name)expr;
				ib = n.resolveBinding();
				if (ib instanceof ITypeBinding || ib instanceof IVariableBinding)
				{
					irfom.AddDataDependency(ib, invoke_parameter_order.keySet());
				}
			}
		}
		
		if (imb == null || !imb.getDeclaringClass().isFromSource()) {
			// null or is from binary.
			IRGeneratorHelper.GenerateGeneralIR(node, node.getName(), irfom, temp_statement_set, code);
		} else {
			// is from source.
			// need to be handled specifically.
			if (ib != null)
			{
				code = IRMeta.User_Defined_Function;
				IRGeneratorHelper.GenerateSourceMethodInvocationIR(ib, imb, node, node.getName(), irfom, invoke_parameter_order, code);
			}
		}
	}
	
	// handling statements.
	@Override
	public boolean visit(AssertStatement node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(IfStatement node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(WhileStatement node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(ForStatement node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(BreakStatement node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(VariableDeclarationStatement node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(TryStatement node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(Block node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(ThrowStatement node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(ContinueStatement node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(EnhancedForStatement node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(EmptyStatement node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(LabeledStatement node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(ReturnStatement node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(Assignment node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(ExpressionStatement node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(TypeDeclarationStatement node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(SynchronizedStatement node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(SwitchStatement node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	// handling expressions.
	@Override
	public boolean visit(SimpleName node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(SwitchCase node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(SuperMethodInvocation node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(SuperFieldAccess node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(SuperConstructorInvocation node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(StringLiteral node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(SingleVariableDeclaration node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(SimpleType node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(QualifiedName node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(PrimitiveType node) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(NumberLiteral node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(NullLiteral node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
		
	@Override
	public boolean visit(InstanceofExpression node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(Initializer node) {
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
		// TODO Auto-generated method stub
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
	public boolean visit(CharacterLiteral node) {
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
	public boolean visit(BooleanLiteral node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(ArrayType node) {
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
	public boolean visit(QualifiedType node) {
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
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(TypeLiteral node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(TypeMethodReference node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(TypeParameter node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(VariableDeclarationExpression node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	// do nothing.

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

	public static int GetMaxLevel() {
		return max_level;
	}
	
}
