package Entity;

import java.net.MalformedURLException;

public class Log {
	private String logId;
	private int runId;
	private String homeUrl;
	private String parentUrl;
	private String link;
	private int depth;
	private int ltId;
	private String title;
	private int response;
	private String urlRedirectedTo;
	
	public String getLogId() {
		return logId;
	}
	public void setLogId(String logId) {
		this.logId = logId;
	}
	public String getUrlRedirectedTo() {
		return urlRedirectedTo;
	}
	public void setUrlRedirectedTo(String urlRedirectedTo) {
		this.urlRedirectedTo = urlRedirectedTo;
	}
	public int getRunId() {
		return runId;
	}
	public void setRunId(int runId) {
		this.runId = runId;
	}
	public String getHomeUrl() {
		return homeUrl;
	}
	public void setHomeUrl(String homeUrl) {
		this.homeUrl = homeUrl;
	}
	public String getParentUrl() {
		return parentUrl;
	}
	public void setParentUrl(String parentUrl) {
		this.parentUrl = parentUrl;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) throws MalformedURLException {	
		this.link = link;
	}
	public int getLtId() {
		return ltId;
	}
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	public void setLtId(int ltId) {
		this.ltId = ltId;
	}
	public int getResponse() {
		return response;
	}
	public void setResponse(int response) {
		this.response = response;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}	
}
