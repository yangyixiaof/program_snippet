package cn.yyx.research.program.analysis.fulltrace.generation;

public class InvokeMethodHandleProperty {
	
	private int id = -1;
	private boolean infinite_loop= false;
	
	public InvokeMethodHandleProperty(int id, boolean infinite_loop) {
		this.setId(id);
		this.setInfinite_loop(infinite_loop);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isInfinite_loop() {
		return infinite_loop;
	}

	public void setInfinite_loop(boolean infinite_loop) {
		this.infinite_loop = infinite_loop;
	}
	
}
