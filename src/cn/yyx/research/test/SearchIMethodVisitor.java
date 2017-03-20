package cn.yyx.research.test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import cn.yyx.research.program.eclipse.searchutil.JavaSearch;

public class SearchIMethodVisitor extends ASTVisitor {
	
	CompilationUnit cu = null;
	
	public SearchIMethodVisitor(CompilationUnit cu) {
		this.cu = cu;
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
				JavaSearch.SearchForWhereTheMethodIsInvoked(imethod, false, new SearchResultRequestorForTest(cu));
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
				JavaSearch.SearchForWhereTheMethodIsInvoked(imethod, true, new SearchResultRequestorForTest(cu));
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return super.visit(node);
	}
	
}
