package cn.yyx.research.program.ir;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class IRGeneratorForClassesInICompilationUnit extends ASTVisitor {
	
	// private List<IRForOneClass> classes = new LinkedList<IRForOneClass>();
	
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		IMember im = ResolveAbstractType(node.resolveBinding());
		if (im != null)
		{
			IRGeneratorForOneClass irfoc = new IRGeneratorForOneClass(im);
			node.accept(irfoc);
			// getClasses().add(irfoc.GetClassLevelGeneration());
		}
		return false;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		IMember im = ResolveAbstractType(node.resolveBinding());
		if (im != null)
		{
			IRGeneratorForOneClass irfoc = new IRGeneratorForOneClass(im);
			node.accept(irfoc);
			// getClasses().add(irfoc.GetClassLevelGeneration());
		}
		return false;
	}
	
	private IMember ResolveAbstractType(ITypeBinding type_bind)
	{
		IJavaElement j_ele = null;
		if (type_bind != null)
		{
			j_ele = type_bind.getJavaElement();
		}
		if (j_ele instanceof IMember)
		{
			IMember im = (IMember)j_ele;
			return im;
		}
		return null;
	}

//	public List<IRForOneClass> getClasses() {
//		return classes;
//	}
//
//	private void setClasses(List<IRForOneClass> classes) {
//		this.classes = classes;
//	}
	
}
