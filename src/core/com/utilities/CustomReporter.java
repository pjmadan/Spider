package univision.com.utilities;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import Entity.Log;
import univision.com.crawler.SiteCrawler;
import univision.com.crawler.crawler;

public class CustomReporter {

	static String logReportText;
	static Integer passedTestCount;
	static Integer failedTestCount;
	static String testCasesHeaders;
	static String testCasesExecutionTimeValues;
	static double totalExecutionTime;
	static HashMap<String, Boolean> allItems = new HashMap<String, Boolean>();
	public static HashMap<Integer, Integer> failedUrlsCount = new HashMap<Integer, Integer>();
	static ArrayList<Log> partialLogList = new ArrayList<Log>();
	static ArrayList<String> htmlPropertyErrors =  new ArrayList<String>();
	static ArrayList<String> primaryAndParentTagErrors =  new ArrayList<String>();
	static ArrayList<String> htmlDevPropertyErrors =  new ArrayList<String>();
	long startTime;
	long accumlatedCounter;
	long maxPagesToWriteReport;
	// Delimiter used in CSV file
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	// CSV file header
	private static final String FILE_HEADER = "Failed Link";
	private static final String FILE_HEADER_HOME = "Home Link";
	private static final String FILE_HEADER_PARENT = "Parent Link";
	private static final String FILE_HEADER_RESPONSE = "Response";
	private static final String FILE_HEADER_DEPTH = "Depth";
	private static String pieChartData = "";
	private static CustomReporter singleton = new CustomReporter();

	private CustomReporter() {
		clearFailedUrlCount();
		reset();
	}

	private void reset() {
		logReportText = "";
		passedTestCount = 0;
		failedTestCount = 0;
		testCasesHeaders = "";
		testCasesExecutionTimeValues = "";
		totalExecutionTime = 0;
		accumlatedCounter = 0;
		partialLogList = new ArrayList<Log>();
	}

	public static CustomReporter getInstance() {
		return singleton;
	}

	public void appendHtmlPropertyError(String errorInformation) {
		synchronized (htmlPropertyErrors) {
			
	
		htmlPropertyErrors.add(errorInformation);
		System.out.println("error" + errorInformation);
		if (htmlPropertyErrors.size() > EnvirommentManager.getInstance()
				.getInteger("MaxHtmlPropertyToSave")) {
			writeHtmlPropertyError();
		}
		}
	}
	public void appendHtmlPrimaryAndParentTagError(String errorInformation) {
		synchronized (primaryAndParentTagErrors) {
			
	
			primaryAndParentTagErrors.add(errorInformation);
	//	System.out.println("error" + errorInformation);
		if (primaryAndParentTagErrors.size() > EnvirommentManager.getInstance()
				.getInteger("MaxHtmlPropertyToSave")) {
			writePrimaryAndParentTagError();
		}
		}
	}
	public void appendHtmlDevPropertyError(String errorInformation) {
		synchronized (htmlDevPropertyErrors) {
		
		htmlDevPropertyErrors.add(errorInformation);
		System.out.println("error" + errorInformation);
		if (htmlDevPropertyErrors.size() > EnvirommentManager.getInstance()
				.getInteger("MaxHtmlPropertyToSave")) {
			writeHtmlDevPropertyError();
		}
		
		}

	}
	public  void writeHtmlDevPropertyError() {
		FileWriter fw;
		for (int i = 0; i < htmlDevPropertyErrors.size(); i++) {
			try {
				fw = new FileWriter(System.getProperty("user.dir")
						+ File.separator + "reports" + File.separator
						+ "dev.csv", true);
				fw.write("\n" + htmlDevPropertyErrors.get(i));
				fw.close();
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		}
		htmlDevPropertyErrors.clear();

	}
	public  void writeHtmlPropertyError() {
		FileWriter fw;
		for (int i = 0; i < htmlPropertyErrors.size(); i++) {
			try {
				fw = new FileWriter(System.getProperty("user.dir")
						+ File.separator + "reports" + File.separator
						+ crawler.resultFileName, true);
				fw.write("\n" + htmlPropertyErrors.get(i));
				fw.close();
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		}
		htmlPropertyErrors.clear();

	}
	
	public  void writePrimaryAndParentTagError() {
		FileWriter fw;
		for (int i = 0; i < primaryAndParentTagErrors.size(); i++) {
			try {
				fw = new FileWriter(System.getProperty("user.dir")
						+ File.separator + "reports" + File.separator
						+ crawler.resultPrimaryAndParenTagFileName, true);
				fw.write("\n" + primaryAndParentTagErrors.get(i));
				fw.close();
	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		}
		primaryAndParentTagErrors.clear();

	}

	public synchronized void appendReportContent(String title, boolean passed,
			String description, Log log) {

		if (passed)
			passedTestCount++;
		else
			failedTestCount++;

		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		double seconds = (double) duration / 1000000000.0;
		String execSeconds = new DecimalFormat("#.####").format(seconds);
		totalExecutionTime += seconds;

		if (!passed) {
			Integer count = failedUrlsCount.get(log.getResponse()) + 1;
			failedUrlsCount.put(log.getResponse(), count);
			accumlatedCounter++;
			logReportText += "<li class=\"fail\">";
			logReportText += "<div class=\"result-indicator left\"></div>";
			logReportText += "<div class=\"activity-name left\"><a href=\""
					+ title + "\" target=\"_blank\">" + title + "</a></div>";
			logReportText += "<div class=\"run-response \">response:("
					+ log.getResponse() + ")</div>|";
			logReportText += "<div class=\"run-timeleft \">(" + execSeconds
					+ " seconds)</div>";
			logReportText += "<div class=\"clear\"></div>";
			logReportText += "<div class=\"desc-container\">";
			logReportText += description;
			logReportText += "</div>";
			logReportText += "</li>";
			allItems.put(title, passed);
			if (log != null) {
				partialLogList.add(log);
			}
		}

		testCasesHeaders += "'" + title + "',";
		testCasesExecutionTimeValues += execSeconds + ",";

		if (maxPagesToWriteReport != -1
				&& accumlatedCounter >= maxPagesToWriteReport) {
			writeReport();
			reset();
		}
	}

	public void writeReport() {

		if (accumlatedCounter == 0 && maxPagesToWriteReport != -1) {
			return;
		}
		Integer totalTestCount = passedTestCount + failedTestCount;
		/*
		 * double passedPercent = (double) passedTestCount / (double)
		 * totalTestCount * 100; double failedPercent = (double) failedTestCount
		 * / (double) totalTestCount * 100;
		 */

		Date date = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("MMddhhmmss");
		String outputFolder = "";
		if (null == outputFolder || outputFolder.equals(""))
			outputFolder = System.getProperty("user.dir") + "/reports/";

		String path = System.getProperty("user.dir") + "/report/";
		String fileName = "report-" + UUID.randomUUID() + ".html";
		String templateFileName = "maven_report.html";
		String testCasesHeader = failedTestCount.toString()
				+ " URLs failed out of "
				+ String.valueOf(failedTestCount + passedTestCount)
				+ " - total execution time ("
				+ new DecimalFormat("#.####").format(totalExecutionTime)
				+ " seconds)";
		try {

			BufferedReader br = new BufferedReader(new FileReader(path
					+ templateFileName));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			String content = sb.toString();

			br.close();
			double percent = passedTestCount / (double) totalTestCount * 100;
			pieChartData += "['Response (" + 200 + ")'," + percent + "],";
			for (Integer key : failedUrlsCount.keySet()) {
				percent = failedUrlsCount.get(key).intValue()
						/ (double) totalTestCount * 100;
				pieChartData += "['Response (" + key.toString() + ")',"
						+ percent + "],";
			}
			pieChartData = pieChartData.substring(0, pieChartData.length() - 1);
			content = content.replaceAll("@@@TestCases@@@", logReportText);
			content = content.replaceAll("@@@TestPercent@@@", pieChartData);
			/*
			 * content = content.replaceAll("@@@failedTesta@@@",
			 * String.valueOf(failedPercent));
			 */
			content = content.replaceAll("@@@CasesTitles@@@", testCasesHeaders);
			content = content.replaceAll("@@@CasesExecution@@@",
					testCasesExecutionTimeValues);
			content = content.replaceAll("@@@testCasesHeader@@@",
					testCasesHeader);

			File file = new File(outputFolder);
			if (!file.exists()) {
				file.mkdir();
			}

			Writer writer;
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFolder + fileName), "UTF8"));

			writer.append(content);
			writer.flush();
			writer.close();

			FileWriter fileWriter = new FileWriter(outputFolder + fileName
					+ ".csv");
			fileWriter.append(FILE_HEADER_HOME.toString());
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(FILE_HEADER_PARENT.toString());
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(FILE_HEADER.toString());
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(FILE_HEADER_DEPTH.toString());
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(FILE_HEADER_RESPONSE.toString());
			fileWriter.append(NEW_LINE_SEPARATOR);

			for (Log item : partialLogList) {
				fileWriter.append("\"" + item.getHomeUrl() + "\"");
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append("\"" + item.getParentUrl() + "\"");
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append("\"" + item.getLink() + "\"");
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(item.getDepth()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(item.getResponse()));

				// fileWriter.append(COMMA_DELIMITER);
				// fileWriter.append(String.valueOf(allItems.get(item)));
				fileWriter.append(NEW_LINE_SEPARATOR);
			}
			fileWriter.flush();
			fileWriter.close();

			// Desktop.getDesktop().open(new File(outputFolder + fileName));

			if (Boolean.parseBoolean(EnvirommentManager.getInstance()
					.getProperty("EmailReport"))) {

				StringBuilder emailBody = new StringBuilder();
				emailBody.append(testCasesHeader);
				emailBody.append("\n\n");
				emailReport(outputFolder, fileName, emailBody.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		clearFailedUrlCount();
	}

	private void emailReport(String path, String filename, String emailBody) {
		String attachments = path + filename + "," + path + filename + ".csv";
		MailApi.send(emailBody.toString(), attachments);
	}

	public void startTest() {
		startTime = System.nanoTime();
	}

	public long getStartTime() {
		return startTime;
	}

	public void setMaxPagesToWriteReport(long value) {
		this.maxPagesToWriteReport = value;
	}

	public void setExecutionStartTime(long value) {
		this.startTime = value;
	}

	private void clearFailedUrlCount() {
		failedUrlsCount = new HashMap<Integer, Integer>();
		String[] reportedStatusCodeArray = EnvirommentManager.getInstance()
				.getProperty("ReportedHttpStatus").split(",");
		for (String string : reportedStatusCodeArray) {
			CustomReporter.failedUrlsCount.put(Integer.parseInt(string), 0);
		}
		pieChartData = "";
	}
}
