package cn.yyx.research.test;

import java.util.HashSet;

public class TestHashMechanism {
	
	public static void main(String[] args) {
		TT1 t1 = new TT1(1);
		TT1 t2 = new TT1(2);
		TT1_wrapper tw1 = new TT1_wrapper(t1);
		TT1_wrapper tw2 = new TT1_wrapper(t1);
		TT1_wrapper tw3 = new TT1_wrapper(t1);
		TT1_wrapper tw4 = new TT1_wrapper(t2);
		TT1_wrapper tw5 = new TT1_wrapper(t2);
		HashSet<TT1_wrapper> tw_set = new HashSet<TT1_wrapper>();
		tw_set.add(tw1);
		tw_set.add(tw2);
		tw_set.add(tw3);
		tw_set.add(tw4);
		tw_set.add(tw5);
		System.out.println("tw_set_size:"+tw_set.size());
	}
	
}

class TT1_wrapper
{
	TT1 t = null;
	
	public TT1_wrapper(TT1 t) {
		this.t = t;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TT1_wrapper)
		{
			return t.equals(((TT1_wrapper)obj).t);
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return t.hashCode();
	}
	
}

class TT1
{
	int x = 0;
	
	public TT1(int x) {
		this.x = x;
	}
}
