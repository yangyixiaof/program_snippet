package cn.yyx.research.program.ir.storage.node.execution;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.analysis.fulltrace.storage.FullTrace;
import cn.yyx.research.program.analysis.fulltrace.storage.connection.DynamicConnection;
import cn.yyx.research.program.analysis.fulltrace.storage.node.DynamicNode;
import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.node.connection.StaticConnectionInfo;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class SkipSelfTask extends IIRNodeTask {

	public SkipSelfTask(IRForOneInstruction iirnode) {
		super(iirnode);
	}

	@Override
	public void HandleOutConnection(DynamicNode source, DynamicNode target, StaticConnectionInfo connect_info,
			FullTrace ft) {
		int final_type = TaskExecutionHelper.ComputeFinalType(source, target, connect_info);
		Set<DynamicConnection> in_conns = ft.GetInConnections(source);
		if (in_conns.isEmpty()) {
			// IRForOneInstruction instr = source.getInstr();
			// if (instr instanceof IRForOneSentinel) {
			ft.AddConnection(new DynamicConnection(source, target, connect_info.getType()));
			// }
		} else {
			// System.err.println("====== Real skip task is running!");
			boolean skip = true;
			IJavaElement oim = null;
			Iterator<DynamicConnection> icitr = in_conns.iterator();
			while (icitr.hasNext()) {
				DynamicConnection dc = icitr.next();
				DynamicNode sour = dc.GetSource();
				IJavaElement im = sour.getInstr().getIm();
				if (oim == null) {
					oim = im;
				} else {
					if (!oim.equals(im)) {
						skip = false;
						break;
					}
				}
			}
			if (skip) {
				Iterator<DynamicConnection> iitr = in_conns.iterator();
				while (iitr.hasNext()) {
					DynamicConnection dc = iitr.next();
					ft.RemoveConnection(dc);
					DynamicNode nsource = dc.GetSource();
					DynamicNode ntarget = target;
					int addition = ntarget.getInstr().getIm().equals(nsource.getInstr().getIm()) ? EdgeBaseType.Self.Value() : 0;
					DynamicConnection new_dc = new DynamicConnection(nsource, ntarget, dc.getType() & final_type | addition);
					ft.AddConnection(new_dc);
				}
			} else {
				ft.AddConnection(new DynamicConnection(source, target, connect_info.getType()));
			}
//			DynamicConnection conn = ft.GetSpecifiedConnection(source, target);
//			if (conn == null) {
//				System.err.println("Strange! specified connection is null!" + ";Source:" + source + ";Target:" + target);
//			}
//			ft.RemoveConnection(conn);
		}
		System.currentTimeMillis();
	}
	
}
