package cn.yyx.research.program.ir;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.SwitchCase;

import cn.yyx.research.program.eclipse.searchutil.JavaSearch;
import cn.yyx.research.program.ir.ast.ASTSearch;
import cn.yyx.research.program.ir.search.IRSearchRequestor;
import cn.yyx.research.program.ir.storage.highlevel.IRForOneCloseBlockUnit;
import cn.yyx.research.program.ir.storage.lowlevel.IRForOneExtension;
import cn.yyx.research.program.ir.storage.lowlevel.IRForOneOperation;
import cn.yyx.research.program.ir.storage.lowlevel.IRInstrKind;

public class IRGeneratorHelper {
	
	public static void GenerateSwitchCaseIR(ASTNode node, ASTNode sc, IRForOneCloseBlockUnit irfom, HashSet<IBinding> binds)
	{
		String code = IRMeta.Switch_Case_Default;
		if (!sc.toString().startsWith("default")) {
			SwitchCase r_sc = (SwitchCase)sc;
			code = IRMeta.Switch_Case_Relation + r_sc.getExpression().toString();
		}
		IRGeneratorHelper.GenerateNoVariableBindingIR(node.getParent(), node, irfom, binds, code);
	}
	
	public static void GenerateNoVariableBindingIR(ASTNode node, ASTNode exact_node, IRForOneCloseBlockUnit irfom,
			HashSet<IBinding> bind_set, String code) {
		Set<IBinding> temp_bindings = bind_set;
		Iterator<IBinding> titr = temp_bindings.iterator();
		while (titr.hasNext()) {
			IBinding ib = titr.next();
			if (ASTSearch.ASTNodeContainsABinding(node, ib)) {
				int start = exact_node.getStartPosition();
				int end = start + exact_node.getLength() - 1;
				IRInstrKind ir_kind = IRInstrKind.ComputeKind(1);
				irfom.AddOneIRUnit(ib, new IRForOneOperation(irfom.getIm(), start, end, code, ir_kind));
			}
		}

	}

	public static void GenerateGeneralIR(ASTNode node, ASTNode exact_node, IRForOneCloseBlockUnit irfom,
			Map<IBinding, Integer> temp_statement_set, String code) {
		Set<IBinding> temp_bindings = temp_statement_set.keySet();
		Iterator<IBinding> titr = temp_bindings.iterator();
		while (titr.hasNext()) {
			IBinding ib = titr.next();
			if (ASTSearch.ASTNodeContainsABinding(node, ib)) {
				Integer count = temp_statement_set.get(ib);
				if (count != null && count >= 0) {
					count++;
					if (count > IRGeneratorForOneCloseBlockUnit.GetMaxLevel()) {
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

	public static void GenerateSourceMethodInvocationIR(IBinding ib, IMethodBinding imb, ASTNode node,
			ASTNode exact_node, IRForOneCloseBlockUnit irfom, Map<IBinding, Integer> temp_statement_set, String code) {
		int start = exact_node.getStartPosition();
		int end = start + exact_node.getLength() - 1;
		Integer count = temp_statement_set.get(ib);
		count++;
		IRInstrKind ir_kind = IRInstrKind.ComputeKind(count);
		temp_statement_set.put(ib, count);
		IJavaElement jele = imb.getJavaElement();
		Collection<IRForOneCloseBlockUnit> methods = null;
		if (jele != null && jele instanceof IMethod) {
			IMethod imethod = (IMethod) jele;
			try {
				IRSearchRequestor sr = new IRSearchRequestor();
				JavaSearch.SearchForWhereTheMethodIsInvoked(imethod, true, sr);
				methods = sr.GetMethods();
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		if (methods != null && methods.size() > 0) {
			IRForOneExtension irfoe = new IRForOneExtension(irfom.getIm(), start, end, methods, ir_kind);
			irfom.AddOneIRUnit(ib, irfoe);
		}
	}

}
