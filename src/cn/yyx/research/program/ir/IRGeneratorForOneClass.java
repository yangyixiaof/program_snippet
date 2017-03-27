package cn.yyx.research.program.ir;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;

public class IRGeneratorForOneClass extends IRGeneratorForOneCloseBlockUnit {
	
	public IRGeneratorForOneClass(IMember im) {
		super(im);
	}

	@Override
	public boolean visit(Initializer node) {
		// not handled in basic generator. This should be handled in special
		// generator invoking basic generator.
		
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		// not handled in basic generator. This should be handled in special
		// generator invoking basic generator
		
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldAccess node) {
		// not handled in basic generator. This should be handled in special
		// generator invoking basic generator
		
		return super.visit(node);
	}

}
