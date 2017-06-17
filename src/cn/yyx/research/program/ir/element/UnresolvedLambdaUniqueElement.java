package cn.yyx.research.program.ir.element;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;

import cn.yyx.research.program.ir.IRElementMeta;

public class UnresolvedLambdaUniqueElement extends VirtualDefinedElement implements IMethod {
	
	String represent = null;
	IMember parent_im = null;
	// Map<IJavaElement, IRForOneInstruction> env = new HashMap<IJavaElement, IRForOneInstruction>();
	
	public UnresolvedLambdaUniqueElement(String represent, IMember parent_im) {
		// , Map<IJavaElement, IRForOneInstruction> env
		this.represent = represent;
		this.parent_im = parent_im;
		// this.env.putAll(env);
	}
	
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public IJavaElement getAncestor(int ancestorType) {
		return null;
	}

	@Override
	public String getAttachedJavadoc(IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	@Override
	public IResource getCorrespondingResource() throws JavaModelException {
		return null;
	}

	@Override
	public String getElementName() {
		return IRElementMeta.UnresolvedLambdaUniqueElement + "#" + represent;
	}

	@Override
	public int getElementType() {
		return 0;
	}

	@Override
	public String getHandleIdentifier() {
		return represent;
	}

	@Override
	public IJavaModel getJavaModel() {
		return null;
	}

	@Override
	public IJavaProject getJavaProject() {
		return null;
	}

	@Override
	public IOpenable getOpenable() {
		return null;
	}

	@Override
	public IJavaElement getParent() {
		return null;
	}

	@Override
	public IPath getPath() {
		return null;
	}

	@Override
	public IJavaElement getPrimaryElement() {
		return null;
	}

	@Override
	public IResource getResource() {
		return null;
	}

	@Override
	public ISchedulingRule getSchedulingRule() {
		return null;
	}

	@Override
	public IResource getUnderlyingResource() throws JavaModelException {
		return null;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public boolean isStructureKnown() throws JavaModelException {
		return false;
	}

	@Override
	public String[] getCategories() throws JavaModelException {
		return null;
	}

	@Override
	public IClassFile getClassFile() {
		return null;
	}

	@Override
	public ICompilationUnit getCompilationUnit() {
		return null;
	}

	@Override
	public IType getDeclaringType() {
		return null;
	}

	@Override
	public int getFlags() throws JavaModelException {
		return 0;
	}

	@Override
	public ISourceRange getJavadocRange() throws JavaModelException {
		return null;
	}

	@Override
	public int getOccurrenceCount() {
		return 0;
	}

	@Override
	public ITypeRoot getTypeRoot() {
		return null;
	}

	@Override
	public IType getType(String name, int occurrenceCount) {
		return null;
	}

	@Override
	public boolean isBinary() {
		return false;
	}

	@Override
	public String getSource() throws JavaModelException {
		return represent;
	}

	@Override
	public ISourceRange getSourceRange() throws JavaModelException {
		return null;
	}

	@Override
	public ISourceRange getNameRange() throws JavaModelException {
		return null;
	}

	@Override
	public void copy(IJavaElement container, IJavaElement sibling, String rename, boolean replace,
			IProgressMonitor monitor) throws JavaModelException {
	}

	@Override
	public void delete(boolean force, IProgressMonitor monitor) throws JavaModelException {
	}

	@Override
	public void move(IJavaElement container, IJavaElement sibling, String rename, boolean replace,
			IProgressMonitor monitor) throws JavaModelException {
	}

	@Override
	public void rename(String name, boolean replace, IProgressMonitor monitor) throws JavaModelException {
	}

	@Override
	public IJavaElement[] getChildren() throws JavaModelException {
		return null;
	}

	@Override
	public boolean hasChildren() throws JavaModelException {
		return false;
	}

	@Override
	public IAnnotation getAnnotation(String name) {
		return null;
	}

	@Override
	public IAnnotation[] getAnnotations() throws JavaModelException {
		return null;
	}

	@Override
	public IMemberValuePair getDefaultValue() throws JavaModelException {
		return null;
	}

	@Override
	public String[] getExceptionTypes() throws JavaModelException {
		return null;
	}

	@Override
	public String[] getTypeParameterSignatures() throws JavaModelException {
		return null;
	}

	@Override
	public ITypeParameter[] getTypeParameters() throws JavaModelException {
		return null;
	}

	@Override
	public int getNumberOfParameters() {
		return -1;
	}

	@Override
	public ILocalVariable[] getParameters() throws JavaModelException {
		return null;
	}

	@Override
	public String getKey() {
		return null;
	}

	@Override
	public String[] getParameterNames() throws JavaModelException {
		return null;
	}

	@Override
	public String[] getParameterTypes() {
		return null;
	}

	@Override
	public String[] getRawParameterNames() throws JavaModelException {
		return null;
	}

	@Override
	public String getReturnType() throws JavaModelException {
		return null;
	}

	@Override
	public String getSignature() throws JavaModelException {
		return null;
	}

	@Override
	public ITypeParameter getTypeParameter(String name) {
		return null;
	}

	@Override
	public boolean isConstructor() throws JavaModelException {
		return false;
	}

	@Override
	public boolean isMainMethod() throws JavaModelException {
		return false;
	}

	@Override
	public boolean isLambdaMethod() {
		return true;
	}

	@Override
	public boolean isResolved() {
		return false;
	}

	@Override
	public boolean isSimilar(IMethod method) {
		if (method instanceof UnresolvedLambdaUniqueElement)
		{
			UnresolvedLambdaUniqueElement ulue = (UnresolvedLambdaUniqueElement)method;
			if (represent.equals(ulue.represent))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return getElementName();
	}
	
}
