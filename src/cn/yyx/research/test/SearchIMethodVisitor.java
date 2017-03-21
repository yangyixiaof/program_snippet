package cn.yyx.research.test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import cn.yyx.research.program.eclipse.searchutil.JavaSearch;

public class SearchIMethodVisitor extends ASTVisitor {
	
	IJavaProject java_project = null;
	
	public SearchIMethodVisitor(IJavaProject java_project) {
		this.java_project = java_project;
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
	
	@Override
	public boolean visit(MethodDeclaration node) {
		System.out.println(node.getName());
		IMethodBinding ibinding = node.resolveBinding();
		if (ibinding != null)
		{
			IMethod imethod = (IMethod)ibinding.getJavaElement();
			try {
				// testing.
				System.out.println("MethodInvocation:" + node.getName() + " Search for references.");
				JavaSearch.SearchForWhereTheMethodIsInvoked(imethod, false, new SearchResultRequestorForTest(java_project));
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
		return super.visit(node);
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		System.out.println(node.getName());
//		if (!node.getName().toString().startsWith("haha"))
//		{
//			return false;
//		}
		IMethodBinding ibinding = node.resolveMethodBinding();
		if (ibinding != null)
		{
			IMethod imethod = (IMethod)ibinding.getJavaElement();
			try {
				System.out.println("MethodInvocation:" + node.getName() + " Search for declarations.");
				JavaSearch.SearchForWhereTheMethodIsInvoked(imethod, true, new SearchResultRequestorForTest(java_project));
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return super.visit(node);
	}
	
}
