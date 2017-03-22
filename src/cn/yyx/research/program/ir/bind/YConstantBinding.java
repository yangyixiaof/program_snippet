package cn.yyx.research.program.ir.bind;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class YConstantBinding implements IBinding {
	
	String represent = null;
	ITypeBinding tb = null;
	Object resolved_constant = null;
	
	public YConstantBinding(String represent, ITypeBinding tb, Object resolved_constant) {
		this.represent = represent;
		this.tb = tb;
		this.resolved_constant = resolved_constant;
	}
	
	@Override
	public IAnnotationBinding[] getAnnotations() {
		return null;
	}

	@Override
	public int getKind() {
		return 0;
	}

	@Override
	public String getName() {
		String name = tb + ";" + resolved_constant;
		return name;
	}

	@Override
	public int getModifiers() {
		return 0;
	}

	@Override
	public boolean isDeprecated() {
		return false;
	}

	@Override
	public boolean isRecovered() {
		return true;
	}

	@Override
	public boolean isSynthetic() {
		return false;
	}

	@Override
	public IJavaElement getJavaElement() {
		return null;
	}

	@Override
	public String getKey() {
		return null;
	}

	@Override
	public boolean isEqualTo(IBinding binding) {
		if (binding instanceof YConstantBinding)
		{
			YConstantBinding ycb = (YConstantBinding)binding;
			if (tb != null) {
				if (tb.equals(ycb.tb))
				{
					if (resolved_constant != null) {
						return resolved_constant.equals(ycb.resolved_constant);
					} else {
						if (ycb.resolved_constant == null)
						{
							return true;
						}
					}
				}
			} else {
				if (ycb.tb == null)
				{
					if (resolved_constant != null) {
						return resolved_constant.equals(ycb.resolved_constant);
					} else {
						if (ycb.resolved_constant == null)
						{
							return true;
						}
					}
				}
			}
			if (tb == null && resolved_constant == null)
			{
				if (represent == null && ycb.represent == null)
				{
					return true;
				}
				if (represent != null && represent.equals(ycb.represent))
				{
					return true;
				}
			}
		}
		return false;
	}
	
}
