package cn.yyx.research.program.ir.visual;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.util.Iterator;

import cn.yyx.research.program.fileutil.FileIterator;
import cn.yyx.research.program.ir.IRVisualMeta;

public class DotView {
	
	public static void HandleAllDotsInDirectory()
	{
		HandleAllDotsInDirectory(IRVisualMeta.dot_directory, IRVisualMeta.dot_pics_directory);
	}
	
	public static void HandleAllDotsInDirectory(String dot_directory, String pic_directory)
	{
		FileIterator fi = new FileIterator(dot_directory, ".*\\.dot$");
		Iterator<File> fitr = fi.EachFileIterator();
		while (fitr.hasNext())
		{
			File f = fitr.next();
			HandleOneDot(f, pic_directory);
		}
	}
	
	private static void HandleOneDot(File f, String pic_directory) {
		String fname = f.getName();
		String dotname = fname.substring(0, fname.lastIndexOf(".dot"));
		String cmd = IRVisualMeta.DOT_EXE + " -Tjpg " + f.getAbsolutePath() + " -o " + pic_directory + "/" + dotname + ".jpg";
		try {
			ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
			pb.redirectError(Redirect.INHERIT);
			pb.redirectOutput(Redirect.INHERIT);
			Process process = pb.start();
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("One file: " + f.getAbsolutePath() + " has been hanlded over.");
	}
	
	public static void main(String[] args) {
		DotView.HandleAllDotsInDirectory();
	}

	public static void HandleLatestDotInDirectory(String debugdotdir, String debugpicdir) {
		File latest = null;
		FileIterator fi = new FileIterator(debugdotdir, ".*\\.dot$");
		Iterator<File> fitr = fi.EachFileIterator();
		int max = -1;
		while (fitr.hasNext())
		{
			File f = fitr.next();
			String fname = f.getName();
			fname = fname.substring(0, fname.lastIndexOf(".dot"));
			int flen = fname.length()-1;
			String num = "";
			while (flen >= 0) {
				char c = fname.charAt(flen);
				if (!Character.isDigit(c)) {
					break;
				}
				num = c + num;
				flen--;
			}
			if (num.equals("")) {
				num = "0";
			}
			int n = Integer.parseInt(num);
			if (max < n) {
				max = n;
				latest = f;
			}
		}
		if (latest != null) {
			HandleOneDot(latest, debugpicdir);
		}
	}
	
}
