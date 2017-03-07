package cn.yyx.research.program.analysis.prepare;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;

public class ParameterizedTypeEliminator extends ASTVisitor {
	
	@Override
	public boolean visit(ParameterizedType node) {
		@SuppressWarnings("unchecked")
		List<Type> tas = node.typeArguments();
		Iterator<Type> titr = tas.iterator();
		while (titr.hasNext())
		{
			Type t = titr.next();
			t.delete();
		}
		
		return super.visit(node);
	}
	
}
