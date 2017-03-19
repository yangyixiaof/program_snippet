package cn.yyx.research.program.ir;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;

import cn.yyx.research.program.ir.ast.ASTSearch;
import cn.yyx.research.program.ir.method.IRForOneMethod;
import cn.yyx.research.program.ir.method.IRForOneOperation;
import cn.yyx.research.program.ir.method.IRInstrKind;

public class IRGeneratorHelper {
	
	public static void GeneralGenerateIR(ASTNode node, ASTNode exact_node, IRForOneMethod irfom, Map<IBinding, Integer> temp_statement_set, String code)
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
	
}
