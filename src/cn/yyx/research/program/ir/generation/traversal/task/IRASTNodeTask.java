package cn.yyx.research.program.ir.generation.traversal.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;

public class IRASTNodeTask {
	
	private Map<ASTNode, List<Runnable>> run_task = new HashMap<ASTNode, List<Runnable>>();
	
	public IRASTNodeTask() {
	}
	
	public void Put(ASTNode node, Runnable run)
	{
		List<Runnable> tasks = run_task.get(node);
		if (tasks == null)
		{
			tasks = new LinkedList<Runnable>();
			run_task.put(node, tasks);
		}
		tasks.add(run);
	}
	
	public void ProcessAndRemoveTask(ASTNode node)
	{
		List<Runnable> tasks = run_task.get(node);
		if (tasks != null)
		{
			Iterator<Runnable> itr = tasks.iterator();
			while (itr.hasNext())
			{
				Runnable run = itr.next();
				run.run();
			}
		}
	}
	
}
