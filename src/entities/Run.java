package Entity;

public class Run {
	private int runId;
	private int linksCount;
	private double executionTime;
	
	public int getRunId() {
		return runId;
	}
	public void setRunId(int runId) {
		this.runId = runId;
		
	}
	public int getLinksCount() {
		return linksCount;
	}
	public void setLinksCount(int linksCount) {
		this.linksCount = linksCount;
	}
	public double getExecutionTime() {
		return executionTime;
	}
	public void setExecutionTime(double executionTime) {
		this.executionTime = executionTime;
	}

}
