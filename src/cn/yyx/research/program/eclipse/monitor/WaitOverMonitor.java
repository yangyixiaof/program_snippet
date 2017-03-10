package cn.yyx.research.program.eclipse.monitor;

import org.eclipse.core.runtime.NullProgressMonitor;

import cn.yyx.research.program.systemutil.SystemUtil;

public class WaitOverMonitor extends NullProgressMonitor {
	
	boolean exit = false;
	
	public WaitOverMonitor() {
	}
	
	@Override
	public void done() {
		super.done();
		exit = true;
	}
	
	public void WaitToStop()
	{
		while (!exit)
		{
			SystemUtil.Delay(1000);
		}
		SystemUtil.Delay(1000);
	}
	
}
