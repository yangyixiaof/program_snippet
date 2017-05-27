package cn.yyx.research.program.ir.generation;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class IRGeneratorForClassesInICompilationUnit extends ASTVisitor {
	
	// private List<IRForOneClass> classes = new LinkedList<IRForOneClass>();
	
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		IType it = ResolveAbstractType(node.resolveBinding());
		if (it != null)
		{
			IRGeneratorForOneClass irfoc = new IRGeneratorForOneClass(it);
			node.accept(irfoc);
			// getClasses().add(irfoc.GetClassLevelGeneration());
		}
		return false;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		IType it = ResolveAbstractType(node.resolveBinding());
		if (it != null)
		{
			IRGeneratorForOneClass irfoc = new IRGeneratorForOneClass(it);
			node.accept(irfoc);
			// getClasses().add(irfoc.GetClassLevelGeneration());
		}
		return false;
	}
	
	private IType ResolveAbstractType(ITypeBinding type_bind)
	{
		IJavaElement j_ele = null;
		if (type_bind != null)
		{
			j_ele = type_bind.getJavaElement();
		}
		if (j_ele instanceof IType)
		{
			IType it = (IType)j_ele;
			return it;
		}
		return null;
	}
	
}
