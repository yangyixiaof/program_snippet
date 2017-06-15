package cn.yyx.research.program.ir.generation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class IRGeneratorForValidation extends ASTVisitor {
	
	private Set<Object> forbid = new HashSet<Object>();
	
	private void AddForbid(Object obj) {
		if (obj != null) {
			forbid.add(obj);
		}
	}
	
	private void AddAllForbid(Collection<Object> objs) {
		if (objs != null && objs.size() > 0) {
			forbid.addAll(objs);
		}
	}
	
	@Override
	public boolean preVisit2(ASTNode node) {
		boolean contigo = super.preVisit2(node);
		if (forbid.remove(node)) {
			contigo = contigo && false;
		}
		return contigo;
	}
	
	@SuppressWarnings("unchecked")
	public void Validation(TypeDeclaration node) {
		AddForbid(node.getName());
		AddAllForbid(node.superInterfaceTypes());
		AddAllForbid(node.modifiers());
	}
	
	@SuppressWarnings("unchecked")
	public void Validation(AnnotationTypeDeclaration node) {
		AddForbid(node.getName());
		AddAllForbid(node.modifiers());
	}
	
	public void Validation(ClassInstanceCreation node) {
		AddForbid(node.getType());
	}
	
	@SuppressWarnings("unchecked")
	public void Validation(EnumDeclaration node) {
		AddForbid(node.getName());
		AddAllForbid(node.superInterfaceTypes());
		AddAllForbid(node.modifiers());
	}
	
	@SuppressWarnings("unchecked")
	public void Validation(FieldDeclaration node) {
		AddForbid(node.getType());
		AddAllForbid(node.modifiers());
	}
	
	@SuppressWarnings("unchecked")
	public void Validation(EnumConstantDeclaration node) {
		AddAllForbid(node.modifiers());
	}
	
	@SuppressWarnings("unchecked")
	public void Validation(MethodDeclaration node) {
		AddForbid(node.getName());
		AddForbid(node.getReturnType2());
		AddAllForbid(node.modifiers());
	}
	
	@SuppressWarnings("unchecked")
	public void Validation(MethodInvocation node) {
		AddForbid(node.getName());
		AddAllForbid(node.typeArguments());
	}
	
	@SuppressWarnings("unchecked")
	public void Validation(SuperMethodInvocation node) {
		AddForbid(node.getQualifier());
		AddAllForbid(node.typeArguments());
	}
	
	public void Validation(VariableDeclarationStatement node) {
		AddForbid(node.getType());
	}
	
	@SuppressWarnings("unchecked")
	public void Validation(PackageDeclaration node) {
		AddForbid(node.getName());
		AddAllForbid(node.annotations());
	}
	
	@SuppressWarnings("unchecked")
	public void Validation(AnnotationTypeMemberDeclaration node) {
		AddForbid(node.getType());
		AddAllForbid(node.modifiers());
		AddForbid(node.getName());
	}
	
	public void Validation(ImportDeclaration node) {
		AddForbid(node.getName());
	}
	
	public void Validation(MarkerAnnotation node) {
		AddForbid(node.getTypeName());
	}
	
	public void Validation(SingleMemberAnnotation node) {
		AddForbid(node.getTypeName());
	}
	
	public void Validation(VariableDeclarationExpression node) {
		AddForbid(node.getType());
	}
	
	public void Validation(Modifier node) {
		AddForbid(node.getKeyword());
	}
	
	public void Validation(SingleVariableDeclaration node) {
		AddForbid(node.getType());
	}
	
}
