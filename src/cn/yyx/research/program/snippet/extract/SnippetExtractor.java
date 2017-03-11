package cn.yyx.research.program.snippet.extract;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.IJavaProject;

import cn.yyx.research.logger.DebugLogger;
import cn.yyx.research.program.eclipse.exception.WrongArgumentException;
import cn.yyx.research.program.eclipse.jdtutil.JDTParser;
import cn.yyx.research.program.eclipse.project.AnalysisEnvironment;
import cn.yyx.research.program.eclipse.project.ProjectInfo;
import cn.yyx.research.program.systemutil.SystemUtil;
import cn.yyx.research.test.TestJavaSearch;

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
		DebugLogger.Log("Start is invoked!");
		SystemUtil.Delay(1000);
		
		JDTParser jdtparser = LoadProjectAccordingToArgs((String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS));
		
		IJavaProject java_project = jdtparser.GetJavaProject();
		
		// testing.
		TestJavaSearch tjs = new TestJavaSearch();
		tjs.TestJavaSearchMehodInvocation(jdtparser);
		
		// ����ѡһ�Ż����������Ȼ���Դ���ĿΣ����Ǻö඼����ѡ��ֻ������������
		// ��֪��֪�������֣����Ե����壬����һ������Ķ�Ӧ�����ֿ�
		// ��������Ȼ����->��������
		// Read the first line of file named 'sdds.txt'
		// ���Զ�����д
		// ������������е����������
		// �㿴���������治���кܶ��ַ����룿��Щ��Ȼ���Կ�����һ�֡�
		
//		DebugLogger.Log("Normal Stop is invoked!");
//		try {
//			AnalysisEnvironment.DeleteAllAnalysisEnvironment();
//		} catch (CoreException e) {
//			e.printStackTrace();
//		}
		
		SystemUtil.Delay(1000);
		return IApplication.EXIT_OK;
	}
	
	@Override
	public void stop() {
		DebugLogger.Log("Force Stop is invoked!");
		try {
			AnalysisEnvironment.DeleteAllAnalysisEnvironment();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
}
