package cn.yyx.research.program.eclipse.jdtutil;

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.IDocument;

import cn.yyx.research.program.fileutil.FileUtil;

public class JDTParser {
	
	private static JDTParser Unique_Empty_Parser = new JDTParser(null);// , null
	
	private ASTParser parser = null;
	
	private IJavaProject javaProject = null;
	// private Set<String> source_classes = new HashSet<String>();
	
	public static JDTParser CreateJDTParser(IJavaProject java_project)
	{
		return new JDTParser(java_project);
	}
	
	private JDTParser(IJavaProject javaProject) {// , Set<String> source_classes
		this.javaProject = javaProject;
//		if (source_classes != null)
//		{
//			this.source_classes.addAll(source_classes);
//		}
		parser = ASTParser.newParser(AST.JLS8);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		parser.setCompilerOptions(options);
		parser.setProject(javaProject);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
	}
	
	public CompilationUnit ParseICompilationUnit(ICompilationUnit icu)
	{
		parser.setSource(icu);
		CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
		return compilationUnit;
	}
	
	public CompilationUnit ParseJavaFile(File f)
	{
		// parser.setUnitName(f.getName());
		parser.setSource(FileUtil.ReadFromFile(f).toCharArray());
		CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
		return compilationUnit;
	}
	
	public CompilationUnit ParseJavaFile(IDocument doc)
	{
		parser.setSource(doc.get().toCharArray());
		CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
		return compilationUnit;
	}
	
	public CompilationUnit ParseOneClass(IType f)
	{
		parser.setSource(f.getClassFile());
		CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
		return compilationUnit;
	}

	public static JDTParser GetUniqueEmptyParser() {
		return Unique_Empty_Parser;
	}

	public IJavaProject GetJavaProject() {
		return javaProject;
	}
		
}
