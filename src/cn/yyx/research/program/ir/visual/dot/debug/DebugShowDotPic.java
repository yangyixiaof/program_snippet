package cn.yyx.research.program.ir.visual.dot.debug;

import cn.yyx.research.program.analysis.fulltrace.storage.FullTrace;
import cn.yyx.research.program.fileutil.FileUtil;
import cn.yyx.research.program.ir.IRControlMeta;
import cn.yyx.research.program.ir.visual.DotView;
import cn.yyx.research.program.ir.visual.dot.generation.CommonDotGenerator;
import cn.yyx.research.program.ir.visual.meta.DotMeta;

public class DebugShowDotPic {
	
	public static void ShowPicForTrace(FullTrace ft) {
		FileUtil.EnsureDirectoryExist(DotMeta.DebugDotDir);
		FileUtil.EnsureDirectoryExist(DotMeta.DebugPicDir);
		if (IRControlMeta.deep_debug) {
			CommonDotGenerator cdg = new CommonDotGenerator(ft.GetRootsForVisual(), ft, DotMeta.DebugDotDir + "/" + "Debug" + ".dot", ft.GetDescription());
			cdg.GenerateDot();
			DotView.HandleAllDotsInDirectory(DotMeta.DebugDotDir, DotMeta.DebugPicDir);
		}
	}
	
}
