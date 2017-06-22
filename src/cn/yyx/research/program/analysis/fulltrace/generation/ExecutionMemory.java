package cn.yyx.research.program.analysis.fulltrace.generation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;

import cn.yyx.research.program.ir.storage.connection.StaticConnection;
import cn.yyx.research.program.ir.storage.node.lowlevel.IRForOneInstruction;

public class ExecutionMemory {
	
	Set<IRForOneInstruction> executed_nodes = new HashSet<IRForOneInstruction>();
	Set<StaticConnection> executed_conns = new HashSet<StaticConnection>();
	// Set<IRForOneInstruction> executed_nodes = new HashSet<IRForOneInstruction>();
	Map<IJavaElement, Set<IRForOneInstruction>> last_waiting_execution = new HashMap<IJavaElement, Set<IRForOneInstruction>>();
	
	public ExecutionMemory() {
	}
	
//	@Override
//	public Object clone() throws CloneNotSupportedException {
//		ExecutionMemory em = new ExecutionMemory();
//		em.executed_conns.addAll(executed_conns);
//		em.executed_conns.addAll(executed_conns);
//		Set<IJavaElement> lkeys = last_waiting_execution.keySet();
//		Iterator<IJavaElement> lkitr = lkeys.iterator();
//		while (lkitr.hasNext()) {
//			IJavaElement lk_ije = lkitr.next();
//			em.last_waiting_execution.put(lk_ije, new HashSet<IRForOneInstruction>(last_waiting_execution.get(lk_ije)));
//		}
//		return em;
//	}

}
