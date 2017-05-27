package cn.yyx.research.program.ir.visual.dot.debug;

import cn.yyx.research.program.analysis.fulltrace.storage.FullTrace;
import cn.yyx.research.program.ir.IRControlMeta;
import cn.yyx.research.program.ir.visual.DotView;
import cn.yyx.research.program.ir.visual.dot.generation.CommonDotGenerator;

public class DebugShowDotPic {
	
	public static void ShowPicForTrace(FullTrace ft) {
		if (IRControlMeta.debug) {
			CommonDotGenerator cdg = new CommonDotGenerator(ft.GetRootsForVisual(), ft, DotDebugMeta.DebugDotDir + "/" + "Debug" + ".dot", ft.GetDescription());
			cdg.GenerateDot();
			DotView.HandleAllDotsInDirectory(DotDebugMeta.DebugDotDir, DotDebugMeta.DebugPicDir);
		}
	}
	
}
