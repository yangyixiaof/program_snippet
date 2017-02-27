package cn.yyx.research.program.snippet.extract;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import cn.yyx.research.logger.DebugLogger;
import cn.yyx.research.program.analysis.exception.WrongArgumentException;
import cn.yyx.research.program.analysis.jdtutil.JDTParser;
import cn.yyx.research.program.analysis.loadproject.AnalysisEnvironment;
import cn.yyx.research.program.analysis.loadproject.ProjectInfo;
import cn.yyx.research.program.systemutil.SystemUtil;

public class SnippetExtractor implements IApplication {
	
	public static JDTParser LoadProjectAccordingToArgs(String[] args) throws Exception 
	{
		if (args.length != 2)
		{
			throw new WrongArgumentException();
		}
		
		DebugLogger.Log("Just for test, this is the args:", args);
		
		// load projects
		ProjectInfo epi = new ProjectInfo(args[0], args[1]);//args[0]:no_use args[1]:D:/eclipse-workspace-pool/eclipse-rcp-neon-codecompletion/cn.yyx.research.program.snippet.extractor
		JDTParser jdtparser = AnalysisEnvironment.CreateAnalysisEnvironment(epi);
		
		return jdtparser;
	}
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		SystemUtil.Delay(1000);
		
		JDTParser jdtparser = LoadProjectAccordingToArgs((String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS));
		// testing.
		System.out.println(jdtparser);
		
		
		// 我想选一门机器翻译和自然语言处理的课，但是好多都不能选，只给本科生开。
		// 你知不知道，那种，语言的语义，到另一种语义的对应？那种课
		// 我想做自然语言->程序语言
		// Read the first line of file named 'sdds.txt'
		// 我自动帮它写
		// 我想做的这个有点像机器翻译
		// 你看，代码里面不是有很多字符串码？这些自然语言可以用一种。
		
		SystemUtil.Delay(1000);
		return null;
	}
	
	@Override
	public void stop() {
		try {
			AnalysisEnvironment.DeleteAllAnalysisEnvironment();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
}
