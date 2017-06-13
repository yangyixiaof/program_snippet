package cn.yyx.research.program.snippet.bootstrap;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;

import cn.yyx.research.logger.DebugLogger;
import cn.yyx.research.program.analysis.fulltrace.generation.CodeOnOneTraceGenerator;
import cn.yyx.research.program.analysis.fulltrace.generation.InvokeMethodSelector;
import cn.yyx.research.program.analysis.fulltrace.generation.MethodSelection;
import cn.yyx.research.program.analysis.fulltrace.storage.FullTrace;
import cn.yyx.research.program.eclipse.exception.WrongArgumentException;
import cn.yyx.research.program.eclipse.project.AnalysisEnvironment;
import cn.yyx.research.program.eclipse.project.ProjectInfo;
import cn.yyx.research.program.eclipse.searchutil.EclipseSearchForICallGraph;
import cn.yyx.research.program.ir.IRControlMeta;
import cn.yyx.research.program.ir.generation.IRGeneratorForOneProject;
import cn.yyx.research.program.ir.visual.dot.generation.GenerateDotForEachFullTrace;
import cn.yyx.research.program.ir.visual.dot.generation.GenerateDotForEachIRCodeInIRProject;
import cn.yyx.research.program.ir.visual.meta.DotMeta;
import cn.yyx.research.program.systemutil.EnvironmentUtil;
import cn.yyx.research.program.systemutil.SystemUtil;
import cn.yyx.research.test.TestJavaSearch;

public class SnippetExtractor implements IApplication {

	public static IJavaProject LoadProjectAccordingToArgs(String[] args) throws Exception {
		if (args.length != 2) {
			throw new WrongArgumentException();
		}
		DebugLogger.Log("Just for test, this is the args:", args);

		// load projects
		ProjectInfo epi = new ProjectInfo(args[0], args[1]);// args[0]:no_use
															// args[1]:D:/eclipse-workspace-pool/eclipse-rcp-neon-codecompletion/cn.yyx.research.program.snippet.extractor
		IJavaProject jproj = AnalysisEnvironment.CreateAnalysisEnvironment(epi);

		return jproj;
	}

	@Override
	public Object start(IApplicationContext context) throws Exception {
		EnvironmentUtil.Clear();
		IJavaProject java_project = LoadProjectAccordingToArgs(
				(String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS));
		try {
			// DebugLogger.Log("Start is invoked!");
			// SystemUtil.Delay(1000);
			// testing.
			if (IRControlMeta.test) {
				TestJavaSearch.TestInAll(java_project);
			} else {
				IRGeneratorForOneProject.GenerateForAllICompilationUnits(java_project);
				// generate each local method.
				GenerateDotForEachIRCodeInIRProject irproj_local_generation = new GenerateDotForEachIRCodeInIRProject(
						DotMeta.ProjectEachMethodDotDir, DotMeta.ProjectEachMethodPicDir);
				irproj_local_generation.GenerateDots();

				if (true) {
					// generate for each full trace.
					IRGeneratorForOneProject irinstance = IRGeneratorForOneProject.GetInstance();
					Set<IMethod> roots = EclipseSearchForICallGraph
							.GetRootCallEntries(irinstance.GetInverseCallGraph());

					// debugging.
					DebugLogger.Log("root size:" + roots.size());
					DebugLogger.Log("root imethods:" + roots);

					InvokeMethodSelector ims = new InvokeMethodSelector(roots);
					ims.StartSelectMethodsProcess();
					List<MethodSelection> method_selects = ims.GetMethodSelections();
					List<FullTrace> ft_traces = new LinkedList<FullTrace>();
					Iterator<MethodSelection> mitr = method_selects.iterator();
					while (mitr.hasNext()) {
						MethodSelection ms = mitr.next();
						CodeOnOneTraceGenerator cootg = new CodeOnOneTraceGenerator(ms);
						FullTrace ft = cootg.GetFullTrace();
						ft_traces.add(ft);
					}
					GenerateDotForEachFullTrace full_trace_generator = new GenerateDotForEachFullTrace(
							DotMeta.FullTraceDotDir, DotMeta.FullTracePicDir, ft_traces);
					full_trace_generator.GenerateDots();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// AnalysisEnvironment.DeleteAllAnalysisEnvironment();
		SystemUtil.Flush();
		SystemUtil.Delay(1000);
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		// DebugLogger.Log("Force Stop is invoked!");
		// try {
		// AnalysisEnvironment.DeleteAllAnalysisEnvironment();
		// } catch (CoreException e) {
		// e.printStackTrace();
		// }
	}

}
