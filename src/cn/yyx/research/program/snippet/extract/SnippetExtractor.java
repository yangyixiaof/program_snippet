package cn.yyx.research.program.snippet.extract;

import java.util.List;
import java.util.Set;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;

import cn.yyx.research.logger.DebugLogger;
import cn.yyx.research.program.analysis.fulltrace.generation.InvokeMethodSelector;
import cn.yyx.research.program.analysis.fulltrace.generation.MethodSelection;
import cn.yyx.research.program.eclipse.exception.WrongArgumentException;
import cn.yyx.research.program.eclipse.project.AnalysisEnvironment;
import cn.yyx.research.program.eclipse.project.ProjectInfo;
import cn.yyx.research.program.eclipse.searchutil.EclipseSearchForICallGraph;
import cn.yyx.research.program.ir.IRControl;
import cn.yyx.research.program.ir.generation.IRGeneratorForOneProject;
import cn.yyx.research.program.systemutil.SystemUtil;
import cn.yyx.research.test.TestJavaSearch;

public class SnippetExtractor implements IApplication {
	
	public static IJavaProject LoadProjectAccordingToArgs(String[] args) throws Exception 
	{
		if (args.length != 2)
		{
			throw new WrongArgumentException();
		}
		
		DebugLogger.Log("Just for test, this is the args:", args);
		
		// load projects
		ProjectInfo epi = new ProjectInfo(args[0], args[1]);//args[0]:no_use args[1]:D:/eclipse-workspace-pool/eclipse-rcp-neon-codecompletion/cn.yyx.research.program.snippet.extractor
		IJavaProject jproj = AnalysisEnvironment.CreateAnalysisEnvironment(epi);
		
		return jproj;
	}
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		DebugLogger.Log("Start is invoked!");
		SystemUtil.Delay(1000);
		
		IJavaProject java_project = LoadProjectAccordingToArgs((String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS));
				
		// testing.
		if (IRControl.test) {
			TestJavaSearch.TestInAll(java_project);
		} else {
			IRGeneratorForOneProject.GenerateForAllICompilationUnits(java_project);
			IRGeneratorForOneProject irinstance = IRGeneratorForOneProject.GetInstance();
			Set<IMethod> roots = EclipseSearchForICallGraph.GetRootCallEntries(irinstance.GetInverseCallGraph());
			InvokeMethodSelector ims = new InvokeMethodSelector();
			ims.SelectOneMethod(null, roots, true);
			List<MethodSelection> method_selects = ims.GetMethodSelections();
			
		}
		
		SystemUtil.Delay(1000);
		return IApplication.EXIT_OK;
	}
	
	@Override
	public void stop() {
//		DebugLogger.Log("Force Stop is invoked!");
//		try {
//			AnalysisEnvironment.DeleteAllAnalysisEnvironment();
//		} catch (CoreException e) {
//			e.printStackTrace();
//		}
	}
	
}
