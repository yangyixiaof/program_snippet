package cn.yyx.research.program.systemutil;

import java.io.File;

import cn.yyx.research.program.fileutil.FileUtil;
import cn.yyx.research.program.ir.visual.meta.DotMeta;

public class EnvironmentUtil {
	
	public EnvironmentUtil() {
	}

	public static void Clear() {
		File f = null;
		f = new File(DotMeta.FullTraceDotDir);
		if (f.exists()) {
			FileUtil.DeleteFile(f);
		}
		f = new File(DotMeta.FullTracePicDir);
		if (f.exists()) {
			FileUtil.DeleteFile(f);
		}
		f = new File(DotMeta.ProjectEachMethodDotDir);
		if (f.exists()) {
			FileUtil.DeleteFile(f);
		}
		f = new File(DotMeta.ProjectEachMethodPicDir);
		if (f.exists()) {
			FileUtil.DeleteFile(f);
		}
		f = new File(DotMeta.DebugDotDir);
		if (f.exists()) {
			FileUtil.DeleteFile(f);
		}
		f = new File(DotMeta.DebugPicDir);
		if (f.exists()) {
			FileUtil.DeleteFile(f);
		}
	}
	
}
