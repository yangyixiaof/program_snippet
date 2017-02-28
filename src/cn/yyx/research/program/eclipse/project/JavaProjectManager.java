package cn.yyx.research.program.eclipse.project;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

import cn.yyx.research.program.eclipse.exception.ProjectAlreadyExistsException;

public class JavaProjectManager {

	private static JavaProjectManager unique = new JavaProjectManager();
	Set<String> projects = new TreeSet<String>();

	private JavaProjectManager() {
	}

	public IJavaProject CreateJavaProject(String projname) throws ProjectAlreadyExistsException, CoreException {
		IProject project = null;
		IJavaProject javaProject = null;
		{
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			project = root.getProject(projname);
			if (project.exists())
			{
				System.err.println("Project " + projname + " already existed! It will be deleted!");
				DeleteJavaProject(projname);
			}
			project.create(null);
			project.open(null);
		}

		{
			IProjectDescription description = project.getDescription();
			description.setNatureIds(new String[] { JavaCore.NATURE_ID });
			project.setDescription(description, null);
		}

		{
			javaProject = JavaCore.create(project);

			IFolder binFolder = project.getFolder("bin");
			binFolder.create(false, true, null);
			javaProject.setOutputLocation(binFolder.getFullPath(), null);

			List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
			IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
			LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vmInstall);
			for (LibraryLocation element : locations) {
				entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
			}
			// add libs to project class path
			javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);

			IFolder sourceFolder = project.getFolder("src");
			sourceFolder.create(false, true, null);

			IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(sourceFolder);
			IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
			IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
			System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
			newEntries[oldEntries.length] = JavaCore.newSourceEntry(root.getPath());
			javaProject.setRawClasspath(newEntries, null);
		}
		// add to set to record.
		projects.add(projname);

		return javaProject;
	}

	public void DeleteJavaProject(String projname) throws CoreException {
		try {
			IProject project = null;
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			project = root.getProject(projname);
			project.delete(false, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void DeleteAllJavaProject() throws CoreException {
		Iterator<String> pitr = projects.iterator();
		while (pitr.hasNext())
		{
			String projname = pitr.next();
			DeleteJavaProject(projname);
		}
		projects.clear();
	}

	public static JavaProjectManager UniqueManager() {
		return unique;
	}

}
