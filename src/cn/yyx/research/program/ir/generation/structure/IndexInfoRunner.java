package cn.yyx.research.program.ir.generation.structure;

public abstract class IndexInfoRunner implements Runnable {
	
	private int index = -1;
	
	public IndexInfoRunner(int index) {
		this.setIndex(index);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
