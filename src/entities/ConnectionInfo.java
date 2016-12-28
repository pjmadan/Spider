package Entity;

public class ConnectionInfo {

	private String Html;
	private int StatusCode;
	private long responseTime;
	
	public String getHtml() {
		return Html;
	}
	public void setHtml(String html) {
		Html = html;
	}
	public int getStatusCode() {
		return StatusCode;
	}
	public void setStatusCode(int statusCode) {
		StatusCode = statusCode;
	}
	public void setResponseTime(long elasedTime) {
		// TODO Auto-generated method stub
		responseTime = elasedTime;
	}
	
	public long getResponseTime() {
		// TODO Auto-generated method stub
		return responseTime;
	}
	
}
