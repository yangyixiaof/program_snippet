package cn.yyx.research.program.ir.visual.dot.generation;

import java.util.Iterator;
import java.util.List;

import cn.yyx.research.program.analysis.fulltrace.storage.FullTrace;
import cn.yyx.research.program.ir.visual.DotView;
import cn.yyx.research.program.ir.visual.dot.DotGenerator;

public class GenerateDotForEachFullTrace implements DotGenerator {
	
	String dots_dir = null;
	String pics_dir = null;
	List<FullTrace> full_traces = null;
	
	public GenerateDotForEachFullTrace(String dots_dir, String pics_dir, List<FullTrace> ft_traces) {
		this.dots_dir = dots_dir;
		this.pics_dir = pics_dir;
		this.full_traces = ft_traces;
	}

	@Override
	public void GenerateDots() {
		Iterator<FullTrace> fitr = full_traces.iterator();
		int idx = 0;
		while (fitr.hasNext()) {
			idx++;
			FullTrace ft = fitr.next();
			CommonDotGenerator cdg = new CommonDotGenerator(ft.GetRootsForVisual(), ft, dots_dir + "/" + "FullTrace" + idx + ".dot");
			cdg.GenerateDot();
		}
		DotView dv = new DotView();
		dv.HandleAllDotsInDirectory(dots_dir, pics_dir);
	}
	
}
