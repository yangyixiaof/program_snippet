package cn.yyx.research.program.ir.debug.raw;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IType;

import cn.yyx.research.program.fileutil.FileUtil;
import cn.yyx.research.program.ir.IRResourceMeta;
import cn.yyx.research.program.ir.IRVisualMeta;
import cn.yyx.research.program.ir.generator.IRGeneratorForOneProject;
import cn.yyx.research.program.ir.storage.highlevel.IRForOneClass;

public class PrintAllIRToDots {
	
	public PrintAllIRToDots(IRGeneratorForOneProject irgfop)
	{
		Map<IType, IRForOneClass> cirs = irgfop.GetClassIR();
		Set<IType> ckeys = cirs.keySet();
		Iterator<IType> citr = ckeys.iterator();
		while (citr.hasNext())
		{
			IType it = citr.next();
			IRForOneClass irfoc = cirs.get(it);
			
			StringBuffer dot_content = new StringBuffer("");
			
			
			FileUtil.WriteToFile(new File(IRVisualMeta.dot_directory + "/" + it.getFullyQualifiedName()), dot_content.toString());
		}
	}
	
}
