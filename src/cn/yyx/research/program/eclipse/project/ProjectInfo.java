package cn.yyx.research.program.eclipse.project;

public class ProjectInfo {
	
	private long id = System.currentTimeMillis();
	private String name = null;
	private String basedir = null;
	
	public ProjectInfo(String name, String basedir) {
		this.name = name;
		this.basedir = basedir;
	}
	
	public long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getBasedir() {
		return basedir;
	}
	
}
