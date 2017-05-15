package cn.yyx.research.program.ir.debug.raw;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.core.IType;

import cn.yyx.research.program.fileutil.FileUtil;
import cn.yyx.research.program.ir.IRVisualMeta;
import cn.yyx.research.program.ir.generation.IRGeneratorForOneProject;
import cn.yyx.research.program.ir.storage.node.highlevel.IRForOneClass;

public class PrintAllIRToDots {
	
	public PrintAllIRToDots(IRGeneratorForOneProject irgfop)
	{
		Set<IType> types = irgfop.GetAllClasses();
		Iterator<IType> citr = types.iterator();
		while (citr.hasNext())
		{
			IType it = citr.next();
			IRForOneClass irfoc = irgfop.GetClassIR(it);
			// debugging.
			System.out.println(irfoc);
			
			StringBuffer dot_content = new StringBuffer("");
			dot_content.append("digraph {\n");
			dot_content.append("edge[fontname=\"SimSun\",fontcolor=red];\n");
			dot_content.append("node[fontname=\"SimSun\",size=\"20,20\"];\n");
			dot_content.append("\n");
			
			dot_content.append("21->22;\n");
			
			
			dot_content.append("\n}\n");
			
			FileUtil.WriteToFile(new File(IRVisualMeta.dot_directory + "/" + it.getFullyQualifiedName()), dot_content.toString());
		}
	}
	
}
