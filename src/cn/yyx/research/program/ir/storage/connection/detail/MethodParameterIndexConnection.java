package cn.yyx.research.program.ir.storage.connection.detail;

import org.eclipse.jdt.core.dom.ASTNode;

import cn.yyx.research.program.ir.exception.ConflictConnectionDetailException;
import cn.yyx.research.program.ir.exception.NotCastConnectionDetailException;

public class MethodParameterIndexConnection extends ConnectionDetail {
	
	private ASTNode node = null;
	private int index = 0;
	
	public MethodParameterIndexConnection(ASTNode node, int index) {
		this.setNode(node);
		this.setIndex(index);
	}
	
	@Override
	public void HorizontalMergeCheck(ConnectionDetail cd) throws NotCastConnectionDetailException {
	}

	@Override
	public ConnectionDetail VerticalMerge(ConnectionDetail cd) throws ConflictConnectionDetailException {
		if ((cd instanceof DefaultConnection)) {
			throw new ConflictConnectionDetailException();
		}
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MethodParameterIndexConnection) {
			MethodParameterIndexConnection mpic = (MethodParameterIndexConnection)obj;
			if (getIndex() == mpic.getIndex() && node.equals(mpic.node)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new MethodParameterIndexConnection(node, getIndex());
	}

	public int getIndex() {
		return index;
	}

	private void setIndex(int index) {
		this.index = index;
	}
	
	public ASTNode getNode() {
		return node;
	}

	private void setNode(ASTNode node) {
		this.node = node;
	}

}
