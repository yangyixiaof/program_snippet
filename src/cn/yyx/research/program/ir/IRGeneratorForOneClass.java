package cn.yyx.research.program.ir;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import cn.yyx.research.program.ir.storage.highlevel.IRForOneClass;
import cn.yyx.research.program.ir.storage.highlevel.IRForOneCloseBlockUnit;

public class IRGeneratorForOneClass extends IRGeneratorForOneCloseBlockUnit {
	
	List<IRForOneCloseBlockUnit> methods = new LinkedList<IRForOneCloseBlockUnit>();
	
	public IRGeneratorForOneClass(IMember im) {
		super(im);
	}
		
	@Override
	public boolean visit(Initializer node) {
		// no need to do anything.
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		// no need to do anything.
		return super.visit(node);
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		IJavaElement im = node.resolveBinding().getJavaElement();
		if (im instanceof IMember)
		{
			IRGeneratorForOneCloseBlockUnit irgfocb = new IRGeneratorForOneCloseBlockUnit((IMember)im);
			node.accept(irgfocb);
			methods.add(irgfocb.GetGeneration());
		}
		return false;
	}
	
	public IRForOneClass GetClassLevelGeneration()
	{
		return new IRForOneClass(irfom, methods);
	}

}
