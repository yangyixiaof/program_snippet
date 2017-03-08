package cn.yyx.research.program.analysis.prepare;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import cn.yyx.research.program.eclipse.jdtutil.JDTParser;

public class PreProcessCompilationUnit {
	
	public CompilationUnit EntirePreProcessCompilationUnit(CompilationUnit cu, JDTParser parser)
	{
		IDocument doc = new Document(cu.toString());
		cu.recordModifications();
		final ASTRewrite rewrite = ASTRewrite.create(cu.getAST());
		cu.accept(new ParameterizedTypeEliminator(rewrite));
		TextEdit edits = rewrite.rewriteAST(doc, null);
		try {
			edits.apply(doc);
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		CompilationUnit modified_cu = parser.ParseJavaFile(doc);
		// System.out.println("CompilationUnit:" + modified_cu);
		return modified_cu;
	}

}
