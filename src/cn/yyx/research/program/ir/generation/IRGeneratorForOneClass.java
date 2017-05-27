package cn.yyx.research.program.ir.generation;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneField;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneMethod;

public class IRGeneratorForOneClass extends IRGeneratorForOneLogicBlock {
	
	private Initializer initial_node = null;
	private IType it = null;
	
	public IRGeneratorForOneClass(IType it) {
		super(null, new IRForOneField(it));
		this.it = it;
	}
		
	@Override
	public boolean visit(Initializer node) {
		this.initial_node = node;
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
			IRGeneratorForOneProject.GetInstance().AddCalleeCaller(im, null);
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
			IType resolved_type = NodeBinding(node);
			boolean type_equals = resolved_type.equals(it);
			boolean has_element = irc.IsHasElement();
			if (type_equals && has_element) {
				if (this.initial_node != null) {
					this.initial_node.accept(this);
				}
				IRGeneratorForOneProject.GetInstance().FetchITypeIR((it)).SetFieldLevel((IRForOneField)irc);
			}
		}
		super.postVisit(node);
	}
	
	private IType NodeBinding(ASTNode node) {
		if (node instanceof AbstractTypeDeclaration) {
			AbstractTypeDeclaration atd = (AbstractTypeDeclaration)node;
			ITypeBinding tb = atd.resolveBinding();
			if (tb != null) {
				IJavaElement tele = tb.getJavaElement();
				if (tele != null && tele instanceof IType) {
					return (IType)tele;
				}
			}
		}
		if (node instanceof AnonymousClassDeclaration) {
			AnonymousClassDeclaration atd = (AnonymousClassDeclaration)node;
			ITypeBinding tb = atd.resolveBinding();
			if (tb != null) {
				IJavaElement tele = tb.getJavaElement();
				if (tele != null && tele instanceof IType) {
					return (IType)tele;
				}
			}
		}
		return null;
	}

}
