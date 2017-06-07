package cn.yyx.research.logger;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.SearchMatch;

import cn.yyx.research.program.eclipse.jdtutil.JDTParser;
import cn.yyx.research.program.ir.IRControlMeta;

public class DebugLogger {

	public static void Log(String additioninfo, String[] infos) {
		if (IRControlMeta.debug) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < infos.length; i++) {
				sb.append("arr[" + i + "]" + ":" + infos[i] + ";");
			}
			System.out.println("Debug " + additioninfo + ":" + sb);
		}
	}
	
	public static void Error(String info) {
		if (IRControlMeta.debug) {
			System.err.println(info);
		}
	}
	
	public static void Debug(String info) {
		if (IRControlMeta.debug) {
			System.err.println(info);
		}
	}

	public static void Log(String info) {
		if (IRControlMeta.debug) {
			System.out.println(info);
		}
	}

	public static void Log(SearchMatch match, IJavaProject java_project) {
		if (IRControlMeta.debug) {
			System.out.println("================== search start ==================");
			Object ele = match.getElement();
			if (ele instanceof IMember)
			{
				IMember im = (IMember)ele;
				if (im.isBinary())
				{
					return;
				}
			}
			if (ele instanceof IMethod)
			{
				IMethod im = (IMethod)ele;
				System.out.println("method matches:" + match.toString());
				System.out.println("method match element class:" + match.getElement().getClass());
				CompilationUnit cu = JDTParser.CreateJDTParser(java_project).ParseICompilationUnit(im.getCompilationUnit());
				String searched_content = "#Unknown#";
				try {
					searched_content = cu.getTypeRoot().getBuffer().getText(match.getOffset(), match.getLength());
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("searched_content:" + searched_content);
			} else if (ele instanceof IType) {
				System.out.println("type matches:" + match.toString());
				System.out.println("type match element class:" + match.getElement().getClass());
			}
			System.out.println("================== search end ==================");
		}
	}

}
