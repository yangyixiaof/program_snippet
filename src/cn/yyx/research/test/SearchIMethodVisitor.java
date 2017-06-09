package cn.yyx.research.test;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeMethodReference;

import cn.yyx.research.program.eclipse.searchutil.EclipseSearchForIMember;
import cn.yyx.research.program.ir.search.IRSearchMethodRequestor;

public class SearchIMethodVisitor extends ASTVisitor {

	IJavaProject java_project = null;

	public SearchIMethodVisitor(IJavaProject java_project) {
		this.java_project = java_project;
	}

	IMethodBinding im = null;

	@Override
	public void preVisit(ASTNode node) {
		if (node.toString().equals("super")) {
			System.out.println("@super:" + node.getClass());
		}
		super.preVisit(node);
	}

	@Override
	public boolean visit(BreakStatement node) {
		IBinding ib = node.getLabel().resolveBinding();
		if (ib != null) {
			System.out.println("BreakStatement binding:" + ib + ";class:" + ib.getClass());
		} else {
			System.out.println("BreakStatement binding:" + ib);
		}
		return super.visit(node);
	}
	
	@Override
	public boolean visit(TypeDeclaration node) {
		ITypeBinding it = node.resolveBinding();
		IJavaElement ije = it.getJavaElement();
		System.out.println("@!TypeDeclaration:" + node + ";ije:" + ije + ";ije class:" + ije.getClass());
		return super.visit(node);
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		ITypeBinding it = node.resolveBinding();
		IJavaElement ije = it.getJavaElement();
		System.out.println("@!AnonymousClassDeclaration:" + node + ";ije:" + ije + ";ije class:" + ije.getClass());
		return super.visit(node);
	}

	@Override
	public boolean visit(SimpleName node) {
		IBinding ib = node.resolveBinding();
		if (ib instanceof IVariableBinding) {
			IVariableBinding ivb = (IVariableBinding) ib;
			IJavaElement ij = ivb.getJavaElement();
			System.out.println("#SimpleName:" + node + ";IJavaElement:" + ij + ";IJavaElementClass:" + ij.getClass());
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(LambdaExpression node) {
		System.out.println("==================== start ====================");
		System.out.println("node:" + node);
		IMethodBinding im = node.resolveMethodBinding();
		if (this.im == null) {
			this.im = im;
		} else {
			System.out.println("if equals:" + this.im.equals(im));
		}
		ITypeBinding it = node.resolveTypeBinding();
		System.out.println("MethodBinding:" + im);
		System.out.println("MethodJavaElement:" + im.getJavaElement());
		System.out.println("TypeBinding:" + it);
		System.out.println("==================== end ====================");
		return super.visit(node);
	}

	@Override
	public boolean visit(ExpressionMethodReference node) {
		System.out.println("==================== start ====================");
		System.out.println("node:" + node);
		IMethodBinding im = node.resolveMethodBinding();
		ITypeBinding it = node.resolveTypeBinding();
		System.out.println("MethodBinding:" + im);
		System.out.println("TypeBinding:" + it);
		System.out.println("==================== end ====================");
		return super.visit(node);
	}

	@Override
	public boolean visit(CreationReference node) {
		System.out.println("==================== start ====================");
		System.out.println("node:" + node);
		IMethodBinding im = node.resolveMethodBinding();
		ITypeBinding it = node.resolveTypeBinding();
		System.out.println("MethodBinding:" + im);
		System.out.println("TypeBinding:" + it);
		System.out.println("==================== end ====================");
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeMethodReference node) {
		System.out.println("==================== start ====================");
		System.out.println("node:" + node);
		IMethodBinding im = node.resolveMethodBinding();
		ITypeBinding it = node.resolveTypeBinding();
		System.out.println("MethodBinding:" + im);
		System.out.println("TypeBinding:" + it);
		System.out.println("==================== end ====================");
		return super.visit(node);
	}

	@Override
	public boolean visit(StringLiteral node) {
		System.out.println("node:" + node);
		System.out.println("Constant Value:" + node.resolveConstantExpressionValue());
		System.out.println("TypeBinding:" + node.resolveTypeBinding());
		System.out.println("TypeBinding not null:" + node.resolveTypeBinding() != null);
		System.out.println("Constant Value not null:" + node.resolveTypeBinding() != null);
		return super.visit(node);
	}

	@Override
	public boolean visit(NumberLiteral node) {
		System.out.println("node:" + node);
		System.out.println("Constant Value:" + node.resolveConstantExpressionValue());
		System.out.println("TypeBinding:" + node.resolveTypeBinding());
		System.out.println("TypeBinding not null:" + node.resolveTypeBinding() != null);
		System.out.println("Constant Value not null:" + node.resolveTypeBinding() != null);
		return super.visit(node);
	}

	@Override
	public boolean visit(NullLiteral node) {
		System.out.println("node:" + node);
		System.out.println("Constant Value:" + node.resolveConstantExpressionValue());
		System.out.println("TypeBinding:" + node.resolveTypeBinding());
		System.out.println("TypeBinding not null:" + node.resolveTypeBinding() != null);
		System.out.println("Constant Value not null:" + node.resolveTypeBinding() != null);
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		IMethodBinding imb = node.resolveMethodBinding();
		System.out.println("SuperMethodInvocation Binding:" + imb + ";JavaElement:" + imb.getJavaElement());
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		IVariableBinding ivb = node.resolveFieldBinding();
		System.out.println("SuperFieldAccess Binding:" + ivb + ";JavaElement:" + ivb.getJavaElement());
		System.out.println("==================== its name ====================");
		IBinding nivb = node.getName().resolveBinding();
		System.out.println("SuperFieldAccess Binding:" + nivb + ";JavaElement:" + nivb.getJavaElement());
		return super.visit(node);
	}

	// @Override
	// public boolean visit(MethodDeclaration node) {
	// System.out.println(node.getName());
	// IMethodBinding ibinding = node.resolveBinding();
	// if (ibinding != null)
	// {
	// IMethod imethod = (IMethod)ibinding.getJavaElement();
	// try {
	// // testing.
	// System.out.println("MethodInvocation:" + node.getName() + " Search for
	// references.");
	// JavaSearch.SearchForWhereTheMethodIsConcreteImplementated(imethod, false,
	// new SearchResultRequestorForTest(java_project));
	// } catch (CoreException e) {
	// e.printStackTrace();
	// }
	// }
	// return super.visit(node);
	// }
	private void PrintInformation(IMethod search, Set<IMethod> methods) {
		// codes below are just used for debugging.
		System.out.println("================== search method integrate start ==================");
		System.out.println("search content is:" + search);
		Iterator<IMethod> mitr = methods.iterator();
		while (mitr.hasNext()) {
			IMethod method = (IMethod) mitr.next();
			IType type = method.getDeclaringType();
			System.out.println("IType:" + type);
			System.out.println("IMethod:" + method);
		}
		System.out.println("================== search method integrate end ==================");
	}

	@Override
	public boolean visit(MethodInvocation node) {
		System.out.println(node.getName());
		// if (!node.getName().toString().startsWith("haha"))
		// {
		// return false;
		// }
		IMethodBinding ibinding = node.resolveMethodBinding();
		if (ibinding != null) {
			IMethod imethod = (IMethod) ibinding.getJavaElement();
//			Set<IMethod> root_callers = null;
//			// EclipseSearchForICallGraph.GetRootCallers(new IMember[]{imethod});
//			Iterator<IMethod> ritr = root_callers.iterator();
//			System.out.println("================ root calls start ================");
//			System.out.println("callee:" + node);
//			while (ritr.hasNext())
//			{
//				IMethod imd = ritr.next();
//				System.out.println("root_call_method:" + imd);
//			}
//			System.out.println("================ root calls end ================");
			try {
				System.out.println("========MethodInvocation Return========:" + ibinding.getReturnType().toString()
						+ ";" + (ibinding.getReturnType().getQualifiedName().equals("void")));
				System.out.println("MethodInvocation:" + node.getName() + " Search for declarations.");
				IRSearchMethodRequestor sr = new IRSearchMethodRequestor(java_project, imethod);
				EclipseSearchForIMember search = new EclipseSearchForIMember();
				search.SearchForWhereTheMethodIsConcreteImplementated(imethod, sr);
				PrintInformation(imethod, sr.GetMethods());
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		System.out.println("ConstructorInvocation:" + node);
		IMethodBinding ibinding = node.resolveConstructorBinding();
		if (ibinding != null) {
			IMethod imethod = (IMethod) ibinding.getJavaElement();
			try {
				System.out.println("========Construction Return========:" + ibinding.getReturnType().toString() + ";"
						+ (ibinding.getReturnType().getQualifiedName().equals("void")));
				System.out.println("MethodInvocation:" + node + " Search for declarations.");
				IRSearchMethodRequestor sr = new IRSearchMethodRequestor(java_project, imethod);
				EclipseSearchForIMember search = new EclipseSearchForIMember();
				search.SearchForWhereTheMethodIsConcreteImplementated(imethod, sr);
				PrintInformation(imethod, sr.GetMethods());
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		System.out.println("ClassInstanceCreation:" + node);
		IMethodBinding ibinding = node.resolveConstructorBinding();
		if (ibinding != null) {
			IMethod imethod = (IMethod) ibinding.getJavaElement();
			try {
				System.out.println("========ClassInstanceCreation Return========:" + ibinding.getReturnType().toString()
						+ ";" + (ibinding.getReturnType().getQualifiedName().equals("void")));
				System.out.println("MethodInvocation:" + node + " Search for declarations.");
				if (imethod == null) {
					System.out.println("Method Element is null:" + node + ";ibinding declaraing class:" + ibinding.getDeclaringClass());
				}
				IRSearchMethodRequestor sr = new IRSearchMethodRequestor(java_project, imethod);
				EclipseSearchForIMember search = new EclipseSearchForIMember();
				search.SearchForWhereTheMethodIsConcreteImplementated(imethod, sr);
				PrintInformation(imethod, sr.GetMethods());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return super.visit(node);
	}

}
