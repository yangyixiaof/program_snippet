package cn.yyx.research.program.ir.storage.node.lowlevel;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
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
		String im_str = im.getElementName();
		if (im instanceof ILocalVariable) {
			ILocalVariable lv = (ILocalVariable)im;
			IMember member = lv.getDeclaringMember();
			im_str = member.getDeclaringType().getFullyQualifiedName();
		} else if (im instanceof IField) {
			IField ifd = (IField)im;
			im_str = ifd.getDeclaringType().getFullyQualifiedName();
		} else if (im instanceof IMethod) {
			IMethod method = (IMethod)im;
			String sig = "()V";
			try {
				sig = method.getSignature();
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			im_str = method.getElementName() + sig;
		} else if (im instanceof IType) {
			IType it = (IType)im;
			im_str = it.getFullyQualifiedName();
		} else {
			im_str = "Unknown Kind:" + im.getClass();
		}
		return IRMeta.VirtualSentinel + im_str;
	}

}
