package cn.yyx.research.program.ir;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;

import cn.yyx.research.program.eclipse.searchutil.JavaSearch;
import cn.yyx.research.program.ir.ast.ASTSearch;
import cn.yyx.research.program.ir.method.IRForOneExtension;
import cn.yyx.research.program.ir.method.IRForOneMethod;
import cn.yyx.research.program.ir.method.IRForOneOperation;
import cn.yyx.research.program.ir.method.IRInstrKind;
import cn.yyx.research.program.ir.search.IRSearchRequestor;

public class IRGeneratorHelper {
	
	public static void GenerateGeneralIR(ASTNode node, ASTNode exact_node, IRForOneMethod irfom, Map<IBinding, Integer> temp_statement_set, String code)
	{
		Set<IBinding> temp_bindings = temp_statement_set.keySet();
		Iterator<IBinding> titr = temp_bindings.iterator();
		while (titr.hasNext())
		{
			IBinding ib = titr.next();
			if (ASTSearch.ASTNodeContainsABinding(node, ib))
			{
				Integer count = temp_statement_set.get(ib);
				if (count != null && count >= 0)
				{
					count++;
					if (count > IRGeneratorForOneMethod.GetMaxLevel()) {
						count = -1;
					} else {
						int start = exact_node.getStartPosition();
						int end = start + exact_node.getLength() - 1;
						IRInstrKind ir_kind = IRInstrKind.ComputeKind(count);
						irfom.AddOneIRUnit(ib, new IRForOneOperation(irfom.getIm(), start, end, code, ir_kind));
					}
					temp_statement_set.put(ib, count);
				}
			}
		}
		
	}
	
	public static void GenerateSourceMethodInvocationIR(IBinding ib, IMethodBinding imb, ASTNode node, ASTNode exact_node, IRForOneMethod irfom, Map<IBinding, Integer> temp_statement_set, String code)
	{
		int start = exact_node.getStartPosition();
		int end = start + exact_node.getLength() - 1;
		Integer count = temp_statement_set.get(ib);
		count++;
		IRInstrKind ir_kind = IRInstrKind.ComputeKind(count);
		temp_statement_set.put(ib, count);
		IJavaElement jele = imb.getJavaElement();
		Collection<IRForOneMethod> methods = null;
		if (jele != null && jele instanceof IMethod)
		{
			IMethod imethod = (IMethod)jele;
			try {
				IRSearchRequestor sr = new IRSearchRequestor();
				JavaSearch.SearchForWhereTheMethodIsInvoked(imethod, true, sr);
				methods = sr.GetMethods();
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		IRForOneExtension irfoe = new IRForOneExtension(irfom.getIm(), start, end, methods, ir_kind);
		irfom.AddOneIRUnit(ib, irfoe);
	}
	
}
