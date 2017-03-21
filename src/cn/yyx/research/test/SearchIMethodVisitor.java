package cn.yyx.research.test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import cn.yyx.research.program.eclipse.jdtutil.JDTParser;
import cn.yyx.research.program.eclipse.searchutil.JavaSearch;

public class SearchIMethodVisitor extends ASTVisitor {
	
	JDTParser jdtparser = null;
	
	public SearchIMethodVisitor(JDTParser jdtparser) {
		this.jdtparser = jdtparser;
	}
	
	@Override
	public boolean visit(SuperMethodInvocation node) {
		IMethodBinding imb = node.resolveMethodBinding();
		System.out.println("SuperMethodInvocation Binding:" + imb);
		return super.visit(node);
	}
	
	@Override
	public boolean visit(SuperFieldAccess node) {
		IVariableBinding ivb = node.resolveFieldBinding();
		System.out.println("SuperFieldAccess Binding:" + ivb);
		return super.visit(node);
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		IMethodBinding ibinding = node.resolveBinding();
		if (ibinding != null)
		{
			IMethod imethod = (IMethod)ibinding.getJavaElement();
			try {
				// testing.
				System.out.println("MethodInvocation:" + node.getName() + " Search for references.");
				JavaSearch.SearchForWhereTheMethodIsInvoked(imethod, false, new SearchResultRequestorForTest(jdtparser));
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
		return super.visit(node);
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
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
				JavaSearch.SearchForWhereTheMethodIsInvoked(imethod, true, new SearchResultRequestorForTest(jdtparser));
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return super.visit(node);
	}
	
}
