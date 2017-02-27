package cn.yyx.research.program.analysis.jdtutil;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import cn.yyx.research.program.fileutil.FileUtil;
import cn.yyx.research.program.systemutil.SystemUtil;

public class JDTParser {
	
	private static JDTParser Unique_Empty_Parser = new JDTParser(null, null);
	
	private ASTParser parser = null;
	
	private IJavaProject javaProject = null;
	private Set<String> source_classes = new HashSet<String>();
	
	public JDTParser(IJavaProject javaProject, Set<String> source_classes) {
		this.javaProject = javaProject;
		if (source_classes != null)
		{
			this.source_classes.addAll(source_classes);
		}
		parser = ASTParser.newParser(AST.JLS8);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		parser.setCompilerOptions(options);
		parser.setProject(javaProject);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
	}
	
	public CompilationUnit ParseJavaFile(File f)
	{
		parser.setUnitName(f.getName());
		parser.setSource(FileUtil.ReadFromFile(f).toCharArray());
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
	
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer("javaProject:" + javaProject);
		result.append(SystemUtil.LINE_SEPARATOR);
		result.append("source_classes:" + source_classes);
		return result.toString();
	}
		
}
