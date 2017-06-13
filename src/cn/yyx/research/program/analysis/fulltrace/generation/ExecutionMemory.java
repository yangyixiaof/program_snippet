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

}
