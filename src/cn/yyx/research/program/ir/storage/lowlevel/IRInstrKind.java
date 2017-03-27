package cn.yyx.research.program.ir.storage.lowlevel;

public enum IRInstrKind {
	
	Direct,
	Strong,
	Medium,
	Weak;
	
	public static IRInstrKind ComputeKind(int count)
	{
		if (count < 2) {
			return Direct;
		} else if (count < 4) {
			return Strong;
		} else if (count < 6) {
			return Medium;
		} else {
			return Weak;
		}
	}
	
}
