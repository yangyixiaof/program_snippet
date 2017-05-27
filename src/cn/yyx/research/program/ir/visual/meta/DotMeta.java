package cn.yyx.research.program.ir.visual.meta;

import cn.yyx.research.program.fileutil.FileUtil;

public class DotMeta {
	
	public static final String FullTraceDotDir = "FullTraceDots";
	public static final String FullTracePicDir = "FullTracePics";
	
	public static final String ProjectEachMethodDotDir = "IRProjectLocalMethodDots";
	public static final String ProjectEachMethodPicDir = "IRProjectLocalMethodPics";
	
	public static final String DebugDotDir = "DebugDots";
	public static final String DebugPicDir = "DebugPics";
	
	static {
		FileUtil.EnsureDirectoryExist(DotMeta.FullTraceDotDir);
		FileUtil.EnsureDirectoryExist(DotMeta.FullTracePicDir);
		
		FileUtil.EnsureDirectoryExist(DotMeta.ProjectEachMethodDotDir);
		FileUtil.EnsureDirectoryExist(DotMeta.ProjectEachMethodPicDir);
		
		FileUtil.EnsureDirectoryExist(DotMeta.DebugDotDir);
		FileUtil.EnsureDirectoryExist(DotMeta.DebugPicDir);
	}
	
}
