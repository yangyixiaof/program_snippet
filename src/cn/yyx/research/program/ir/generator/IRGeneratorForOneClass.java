package cn.yyx.research.program.ir.generator;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import cn.yyx.research.program.ir.storage.highlevel.IRForOneField;
import cn.yyx.research.program.ir.storage.highlevel.IRForOneMethod;

public class IRGeneratorForOneClass extends IRGeneratorForOneLogicBlock {
	
	private Initializer node = null;
	private IType it = null;
	
	public IRGeneratorForOneClass(IType it) {
		super(new IRForOneField(it));
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
			IRForOneMethod imb = IRGeneratorForOneProject.FetchIMethodIR(im);
			IRGeneratorForOneLogicBlock irgfocb = new IRGeneratorForOneLogicBlock(imb);
			node.accept(irgfocb);
			IRGeneratorForOneProject.FetchITypeIR((it)).AddMethodLevel((IRForOneMethod)irgfocb.GetGeneration());
		}
		return false;
	}
	
	@Override
	public void postVisit(ASTNode node) {
		if (node instanceof AbstractTypeDeclaration || node instanceof AnonymousClassDeclaration)
		{
			this.node.accept(this);
			IRGeneratorForOneProject.FetchITypeIR((it)).SetFieldLevel((IRForOneField)irc);
		}
		super.postVisit(node);
	}

}