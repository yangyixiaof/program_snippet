package cn.yyx.research.program.ir.storage.node.lowlevel;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;

import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;

public class IRForOneEmptyConstructorInvocation extends IRForOneSourceMethodInvocation {
	
	private IType it = null;
	// private IJavaElement parent_im = null;
	// this is set when handling a MethodInvocation.
	// private HashMap<IJavaElement, Integer> variable_parameter_order = new HashMap<IJavaElement, Integer>();
	// private List<HashMap<IJavaElement, Integer>> variable_parameter_orders = new LinkedList<HashMap<IJavaElement, Integer>>();
	// parameter order starts from 1, 0 refers to the invoking object.
	// this denotes the IJavaElement which im represents, it appers in which method parameter and its max instruction index.
	
	// this im has already contained the information about which IJavaElement this all about.
	// Solved. HashMap<IRForOneInstruction, Integer> should be HashMap<IRForOneInstruction, List<Integer>>.
	public IRForOneEmptyConstructorInvocation(IType it, String method_name, IRCode parent_env, IJavaElement im, Class<? extends IIRNodeTask> task_class) {
		super(method_name, im, parent_env, task_class);
		this.SetIType(it);
	}

	@Override
	public String ToVisual() {
		return im.getElementName() + "$EmptyConstructor:" + GetMethodName();
	}
	
	@Override
	public String toString() {
		return ToVisual();
	}

	public IType GetIType() {
		return it;
	}

	public void SetIType(IType it) {
		this.it = it;
	}
	
}
