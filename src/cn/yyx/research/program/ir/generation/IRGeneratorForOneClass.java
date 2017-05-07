package cn.yyx.research.program.ir.generation;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import cn.yyx.research.program.ir.storage.node.zstatic.highlevel.IRForOneField;
import cn.yyx.research.program.ir.storage.node.zstatic.highlevel.IRForOneMethod;

public class IRGeneratorForOneClass extends IRGeneratorForOneLogicBlock {
	
	private Initializer node = null;
	private IType it = null;
	
	public IRGeneratorForOneClass(IType it) {
		super(null, new IRForOneField(it));
	}
		
	@Override
	public boolean visit(Initializer node) {
		this.node = node;
		return false;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		// no need to do anything.
		return super.visit(node);
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		IJavaElement ije = node.resolveBinding().getJavaElement();
		if (ije instanceof IMethod)
		{
			IMethod im = (IMethod)ije;
			IRForOneMethod imb = null;
			if (node.isConstructor()) {
				imb = IRGeneratorForOneProject.GetInstance().FetchIConstructorIR(im, it);
			} else {
				imb = IRGeneratorForOneProject.GetInstance().FetchIMethodIR(im);
			}
			IRGeneratorForOneLogicBlock irgfocb = new IRGeneratorForOneLogicBlock(im, imb);
			node.accept(irgfocb);
			IRGeneratorForOneProject.GetInstance().FetchITypeIR((it)).AddMethodLevel((IRForOneMethod)irgfocb.GetGeneration());
		}
		return false;
	}
	
	@Override
	public void postVisit(ASTNode node) {
		if (node instanceof AbstractTypeDeclaration || node instanceof AnonymousClassDeclaration)
		{
			this.node.accept(this);
			IRGeneratorForOneProject.GetInstance().FetchITypeIR((it)).SetFieldLevel((IRForOneField)irc);
		}
		super.postVisit(node);
	}

}
