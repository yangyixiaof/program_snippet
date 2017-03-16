package cn.yyx.research.program.ir;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.WildcardType;

public class IRGenerator extends ASTVisitor {
	
	public IRGenerator() {
	}
	
	@Override
	public boolean visit(AssertStatement node) {
		// TODO Auto-generated method stub
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
	public boolean visit(AnnotationTypeDeclaration node) {
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
	
}
