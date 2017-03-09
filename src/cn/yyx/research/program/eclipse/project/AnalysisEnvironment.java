package cn.yyx.research.program.eclipse.project;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import cn.yyx.research.program.analysis.prepare.PreProcessCompilationUnitHelper;
import cn.yyx.research.program.eclipse.exception.NoAnalysisSourceException;
import cn.yyx.research.program.eclipse.exception.ProjectAlreadyExistsException;
import cn.yyx.research.program.eclipse.jdtutil.JDTParser;
import cn.yyx.research.program.fileutil.FileIterator;
import cn.yyx.research.program.fileutil.FileUtil;

public class AnalysisEnvironment {
	
	public static JDTParser CreateAnalysisEnvironment(ProjectInfo pi) throws NoAnalysisSourceException, ProjectAlreadyExistsException, CoreException
	{
		// Map<String, String> all_need_handle_files = new TreeMap<String, String>();
		// Iterate all files to fill the specific structure.
		Map<String, TreeMap<String, String>> dir_files_map = new TreeMap<String, TreeMap<String, String>>();
		File dir = new File(pi.getBasedir());
		if (!dir.exists() || !dir.isDirectory())
		{
			throw new NoAnalysisSourceException();
		}
		FileIterator fi = new FileIterator(dir.getAbsolutePath(), ".+(?<!-copy)\\.java$");
		Iterator<File> fitr = fi.EachFileIterator();
		while (fitr.hasNext())
		{
			File f = fitr.next();
			String f_norm_path = f.getAbsolutePath().trim().replace('\\', '/');
			FileUtil.CopyFile(f, new File(f_norm_path.substring(0, f_norm_path.lastIndexOf(".java")) + "-copy" + ".java"));
			JDTParser unique_parser = JDTParser.GetUniqueEmptyParser();
			CompilationUnit cu = unique_parser.ParseJavaFile(f);
			CompilationUnit modified_cu = PreProcessCompilationUnitHelper.EntirePreProcessCompilationUnit(cu, unique_parser);
			FileUtil.WriteToFile(f, modified_cu.toString());
			PackageDeclaration pack = cu.getPackage();
			if (pack != null)
			{
				String fname = f.getName();
				String packagename = pack.getName().toString();
				String packagepath = packagename.replace('.', '/');
				String packagepath_with_classfile = packagepath + "/" + fname;
				String class_full_qualified_name = packagename + "." + fname.substring(0, fname.lastIndexOf(".java"));
				if (f_norm_path.endsWith(packagepath_with_classfile))
				{
					String f_dir = f_norm_path.substring(0, f_norm_path.lastIndexOf(packagepath_with_classfile)).replace('\\', '/');
					while (f_dir.endsWith("/"))
					{
						f_dir = f_dir.substring(0, f_dir.length()-1);
					}
					TreeMap<String, String> files_in_dir = dir_files_map.get(f_dir);
					if (files_in_dir == null)
					{
						files_in_dir = new TreeMap<String, String>();
						dir_files_map.put(f_dir, files_in_dir);
					}
					// How to judge which java file is more complete? Currently, just judge the last update time of a file.
					if (files_in_dir.containsKey(class_full_qualified_name)) {
						String full_name = files_in_dir.get(class_full_qualified_name);
						File full_f = new File(full_name);
						if (f.lastModified() > full_f.lastModified()) {
							files_in_dir.put(class_full_qualified_name, f_norm_path);
						}
					} else {
						files_in_dir.put(class_full_qualified_name, f_norm_path);
					}
				}
			}
		}
		// all_need_handle_files.put(f_norm_path, f_dir);
		
		// Create and fill the source folder of the project.
		JavaProjectManager manager = JavaProjectManager.UniqueManager();
		IJavaProject javaProject = manager.CreateJavaProject(pi.getName());
		Set<String> analysis_classes = JavaImportOperation.ImportFileSystem(javaProject, dir_files_map);
		
		JDTParser jdtparser = new JDTParser(javaProject, analysis_classes);
		return jdtparser;
	}
	
	public static void DeleteAnalysisEnvironment(ProjectInfo pi) throws CoreException
	{
		JavaProjectManager.UniqueManager().DeleteJavaProject(pi.getName());
	}
	
	public static void DeleteAllAnalysisEnvironment() throws CoreException
	{
		JavaProjectManager.UniqueManager().DeleteAllJavaProject();
	}
	
}
