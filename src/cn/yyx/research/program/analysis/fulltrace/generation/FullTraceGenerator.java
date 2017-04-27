package cn.yyx.research.program.analysis.fulltrace.generation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

import cn.yyx.research.program.analysis.fulltrace.FullTrace;
import cn.yyx.research.program.ir.generation.IRGeneratorForOneProject;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneClass;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneConstructor;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneField;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneMethod;

public class FullTraceGenerator {
	
	private Set<IMethod> visited = new HashSet<IMethod>();
	
	public void GoForwardOneStep(Set<IMethod> will_visit, FullTrace ft)
	{
		Iterator<IMethod> witr = will_visit.iterator();
		while (witr.hasNext())
		{
			IMethod im = witr.next();
			if (!visited.contains(im))
			{
				visited.add(im);
				IRForOneMethod irfom = IRGeneratorForOneProject.GetInstance().GetMethodIR(im);
				if (irfom == null)
				{
					continue;
				}
				FullTrace ft_run = new FullTrace(ft);
				if (irfom instanceof IRForOneConstructor)
				{
					IRForOneConstructor irfoc = (IRForOneConstructor)irfom;
					IType it = irfoc.getWrap_class();
					IRForOneClass irfot = IRGeneratorForOneProject.GetInstance().GetClassIR(it);
					if (irfot != null)
					{
						IRForOneField field_level = irfot.GetFieldLevel();
						if (field_level != null)
						{
							// TODO execute field_level.
							ft_run.ExecuteFieldCode(field_level, this);
						}
					}
				}
				if (irfom != null)
				{
					// TODO execute irfom.
					ft_run.ExecuteMethodCode(irfom, this);
				}
			}
		}
	}
	
}
