package cn.yyx.research.program.ir.storage.node.lowlevel;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import cn.yyx.research.program.ir.IRMeta;
import cn.yyx.research.program.ir.storage.node.IIRNodeTask;
import cn.yyx.research.program.ir.storage.node.highlevel.IRCode;

public class IRForOneSentinel extends IRForOneInstruction {

	public IRForOneSentinel(IJavaElement im, IRCode parent_env, Class<? extends IIRNodeTask> task_class) {
		super(im, parent_env, task_class);
	}
	
	@Override
	public String ToVisual() {
		String im_str = null;
		if (im instanceof ILocalVariable) {
			ILocalVariable lv = (ILocalVariable)im;
			im_str = "VD#" + lv.getElementName() + "#" + lv.getTypeSignature();
		} else if (im instanceof IField) {
			IField ifd = (IField)im;
			String ts = "Unknown_Type";
			try {
				ts = ifd.getTypeSignature();
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			im_str = "VD#" + ifd.getElementName() + "#" + ts;
		} else if (im instanceof IMethod) {
			IMethod method = (IMethod)im;
			String sig = "Unknown_Sig";
			try {
				sig = method.getSignature();
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			im_str = "MD#" + method.getElementName() + "#" + sig;
		} else if (im instanceof IType) {
			IType it = (IType)im;
			im_str = "TD#" + it.getElementName();
		} else {
			im_str = "NanD#" + im.getElementName();
		}
		return IRMeta.VirtualSentinel + im_str;
	}

}
