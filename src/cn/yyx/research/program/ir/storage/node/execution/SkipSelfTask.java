package cn.yyx.research.program.ir.storage.node.execution;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.analysis.fulltrace.storage.FullTrace;
import cn.yyx.research.program.analysis.fulltrace.storage.connection.DynamicConnection;
import cn.yyx.research.program.analysis.fulltrace.storage.node.DynamicNode;
import cn.yyx.research.program.ir.storage.connection.ConnectionInfo;
import cn.yyx.research.program.ir.storage.connection.EdgeBaseType;
import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class SkipSelfTask extends IIRNodeTask {

	public SkipSelfTask(IRForOneInstruction iirnode) {
		super(iirnode);
	}

	@Override
	public void HandleOutConnection(DynamicNode source, DynamicNode target, ConnectionInfo connect_info,
			FullTrace ft) {
		// Solved. need to handle IRForOneRawMethodBarrier.
		
		// debugging.
		if (source.toString().trim().startsWith("@Sentinel_URE#")) {
			System.currentTimeMillis();
		}
		if (source.toString().trim().startsWith("EmptyConstructor:")) {
			System.currentTimeMillis();
		}
		
		int final_type = TaskExecutionHelper.ComputeFinalType(source, target, connect_info);
		
		Set<DynamicConnection> in_conns = ft.GetInConnections(source);
		
		if (in_conns.isEmpty()) {
			ft.AddConnection(new DynamicConnection(source, target, connect_info.getType(), connect_info.getNum()));
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
					int num = dc.getNum() + connect_info.getNum();
					if (dc.getNum() > 0 && connect_info.getNum() > 0) {
						num = dc.getNum() * connect_info.getNum();
					}
					DynamicConnection new_dc = new DynamicConnection(nsource, ntarget, dc.getType() & final_type | addition, num);
					ft.AddConnection(new_dc);
				}
				ft.HandleRootsAfterRemovingAllConnections(in_conns);
			} else {
				ft.AddConnection(new DynamicConnection(source, target, connect_info.getType(), connect_info.getNum()));
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
