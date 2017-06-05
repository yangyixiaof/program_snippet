package cn.yyx.research.program.fileutil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {

	public static void EnsureDirectoryExist(String dir) {
		File d = new File(dir);
		if (!d.exists()) {
			d.mkdirs();
		}
	}

	public static String ReadFromFile(File f) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			StringBuilder content = new StringBuilder();
			String tmp = null;
			while ((tmp = reader.readLine()) != null) {
				content.append(tmp);
				content.append("\n");
			}
			reader.close();
			reader = null;
			String source = content.toString();
			return source;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	public static void WriteToFile(String filename, String filecontent, String directory) {
		if (directory == null) {
			directory = "";
		}

		String filepath = directory + "/" + filename;
		if (directory.endsWith("/") || directory.endsWith("\\")) {
			filepath = directory + filename;
		}
		try {
			if (!directory.equals("")) {
				File diret = new File(directory);
				if (!diret.exists()) {
					diret.mkdirs();
				}
			}
			File f = new File(filepath);
			if (!f.exists()) {
				f.createNewFile();
			}
			FileWriter fw = new FileWriter(f.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(filecontent);
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("There are errors in creating files or directories.");
			System.exit(1);
		}
	}

	public static void CopyFile(File f1, File f2) {
		int length = 2048;
		try {
			FileInputStream in = new FileInputStream(f1);
			FileOutputStream out = new FileOutputStream(f2);
			byte[] buffer = new byte[length];
			while (true) {
				int ins = in.read(buffer);
				if (ins == -1) {
					in.close();
					out.flush();
					out.close();
				} else
					out.write(buffer, 0, ins);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void WriteToFile(File file, String content) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void DeleteFile(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File f : files) {
					DeleteFile(f);
				}
			} else {
				file.delete();
			}
		}
	}

}
