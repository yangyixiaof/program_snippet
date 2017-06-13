package cn.yyx.research.program.ir.storage.node.lowlevel;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import cn.yyx.research.program.ir.IRElementMeta;
import cn.yyx.research.program.ir.IRMeta;
import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;

public class IRForOneSentinel extends IRForOneInstruction {

	public IRForOneSentinel(IJavaElement im, IRCode parent_env, Class<? extends IIRNodeTask> task_class) {
		super(im, parent_env, task_class);
	}
	
	@Override
	public String ToVisual() {
		String im_str = im.getElementName();
		if (im instanceof ILocalVariable) {
			ILocalVariable lv = (ILocalVariable)im;
			im_str = IRElementMeta.Variable + "#" + lv.getElementName() + "#" + lv.getTypeSignature();
		} else if (im instanceof IField) {
			IField ifd = (IField)im;
			String ts = "Unknown_Type";
			try {
				ts = ifd.getTypeSignature();
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			im_str = IRElementMeta.Variable + "#" + ifd.getElementName() + "#" + ts;
		} else if (im instanceof IMethod) {
			IMethod method = (IMethod)im;
			String sig = "Unknown_Sig";
			try {
				sig = method.getSignature();
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			im_str = IRElementMeta.Method + "#" + method.getElementName() + "#" + sig;
		} else if (im instanceof IType) {
			IType it = (IType)im;
			im_str = IRElementMeta.Type + "#" + it.getElementName();
		}
		return im.getElementName() + "$" + IRMeta.VirtualSentinel + im_str;
	}
	
	@Override
	public String toString() {
		return ToVisual();
	}
	
}
