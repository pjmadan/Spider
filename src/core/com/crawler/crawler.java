/* This crawler bot will accept a list of websites with their root URL as an XML/Excel file
 * The crawler bot will then iterate through each website and access every element that is
 * configured to be verified. These elements can include links, images, headers, footers, divs 
 * logos, lists, tables along with a list of configured assertions for each element type. For 
 * links the crawler will go as many nested levels as needed till it encounters circular references
 * or duplicate links. The intent is to verify that every resource is accessible. The crawler bot 
 * will store a configurable list of properties for each element alongwith the expected result. 
 * The output of the scan will be generated as an HTML report. This crawler bot can be configured 
 * to run in any environment
 */

package univision.com.crawler;

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;



import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.authentication.AuthInfo;
import edu.uci.ics.crawler4j.crawler.authentication.BasicAuthInfo;
import edu.uci.ics.crawler4j.crawler.authentication.FormAuthInfo;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import univision.com.dao.CrawlerDao;
import univision.com.utilities.CSVParser;
import univision.com.utilities.CustomReporter;
import univision.com.utilities.EnvirommentManager;
import univision.com.utilities.MainFrame;
import Entity.Log;
import Entity.Run;

public class crawler {

	/* Input Type can be either "csv" or "xml" */
	public ArrayList<resultset> finalresultset;
	public static Run run;
	public static Log log;
	public final static String SitesPathKey = "LINKSNAME";
	public final static String SitesPropPathKey = "LINKSPROPNAME";
	private static String runId;
	public static String linksPerSecond;
	public static boolean botisrunning = true;
	public static int crawledNumber = 0;
	private static Timer timer = new Timer();
	private static boolean enableRobotsTxt = Boolean
			.parseBoolean(EnvirommentManager.getInstance().getProperty(
					"EnableRobotsTxt"));
	public static String resultFileName = "results-"
			+ Long.toString(System.nanoTime()) + ".csv";
	public static String resultPrimaryAndParenTagFileName = "results-primary-parent-tag-"
			+ Long.toString(System.nanoTime()) + ".csv";
	private static boolean hasBasicAuth = Boolean
			.parseBoolean(EnvirommentManager.getInstance().getProperty(
					"HasBasicAuth"));
	private static boolean readUrlsFromCsvFile = Boolean
			.parseBoolean(EnvirommentManager.getInstance().getProperty(
					"ReadUrlsFromCsvFile"));
	private static boolean isCheckHtmlEnabled = Boolean
			.parseBoolean(EnvirommentManager.getInstance().getProperty(
					"CheckHtmlEnabled"));

	private static boolean hasFormAuth = Boolean
			.parseBoolean(EnvirommentManager.getInstance().getProperty(
					"HasFormAuth"));
	private static String loginPage = EnvirommentManager.getInstance()
			.getProperty("LoginPage");

	private static String UserAgent = EnvirommentManager.getInstance()
			.getProperty("UserAgent");
	private static String userNameAttribute = EnvirommentManager.getInstance()
			.getProperty("UserNameAttribute");
	private static String userNameValue = EnvirommentManager.getInstance()
			.getProperty("UserNameValue");
	private static String passwordAttribute = EnvirommentManager.getInstance()
			.getProperty("PasswordAttribute");
	private static String passwordValue = EnvirommentManager.getInstance()
			.getProperty("PasswordValue");

	public static void main(String[] args) {
		MainFrame mainFrame = null;

		System.out.println("start");
		if (!MainFrame.isHeadless) {
			mainFrame = new MainFrame();
		}
		long execStartTime = System.nanoTime();
		run = new Run();

		System.out.println("1");
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				long execTickTime = System.nanoTime();
				long tick = execTickTime - execStartTime;
				double seconds = (double) tick / 1000000000.0;

				// getExecutionTimeFormatted(seconds);

				String tickSeconds = new DecimalFormat("#").format(seconds);
				MainFrame.setDetailsLabel("TimeElapsed",
						getExecutionTimeFormatted(seconds));

				if (seconds > 0) {
					double average = SiteCrawler.totalCrwaled / seconds;
					tickSeconds = new DecimalFormat("#.##").format(average);
					linksPerSecond = tickSeconds;
					MainFrame.setDetailsLabel("CrawleSpeed", tickSeconds
							+ " urls/second");
				}
			}
		}, 0, 1000);

		// generate run ID;

		System.out.println("2");

		try {

			CrawlerDao crawlerDao = new CrawlerDao();
			crawlerDao.insertintoRun(run);

			System.out.println("3");
			// Disable apache log 4j
			DisableLogging();

			CustomReporter.getInstance().startTest();

			ArrayList<Integer> reportedStatusCode = new ArrayList<Integer>();
			String[] reportedStatusCodeArray = EnvirommentManager.getInstance()
					.getProperty("ReportedHttpStatus").split(",");
			for (String string : reportedStatusCodeArray) {
				reportedStatusCode.add(Integer.parseInt(string));
				SiteCrawler.failedUrlsCount.put(Integer.parseInt(string), 0);
			}

			System.out.println("4");
			SiteCrawler.reportedStatusCode = reportedStatusCode;
			String[] fileNames = EnvirommentManager.getInstance()
					.getProperty("CsvFileToFetchUrls").split("\\|");
			// System.getProperty("user.dir") + File.separator + "resources" +
			// File.separator +
			String[] words;
			if (isCheckHtmlEnabled) {
				initialzeTagsDictionary();
			}

			System.out.println("start read");
			if (readUrlsFromCsvFile) {
				words = CSVParser.parseCsvUrls(fileNames, EnvirommentManager
						.getInstance().getProperty("CsvUrlColumnName"));
				
			} else {
				words = EnvirommentManager.getInstance()
						.getProperty("CrawlerSites").split("\\|");
			}
			System.out.println("fineto reed");
			SiteCrawler.words = words;
			FileWriter fw;
			try {
				fw = new FileWriter(System.getProperty("user.dir")
						+ File.separator + "reports" + File.separator
						+ resultFileName);
				fw.write("URL,AdSlotValue,Content Type,parent_topic,IsParentTopicSellable,IsParentTopicSensitive,primary_tag,IsPrimaryTagSellable,IsPrimaryTagSensitive,ParentTopicAdValue,PrimaryTagAdValue,error \r\n");
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
			try {
				fw = new FileWriter(System.getProperty("user.dir")
						+ File.separator + "reports" + File.separator
						+ resultPrimaryAndParenTagFileName);
				fw.write("URL,parent_topic,primary_tag\r\n");
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
			try {
				fw = new FileWriter(System.getProperty("user.dir")
						+ File.separator + "reports" + File.separator
						+ "Dev.csv");
				fw.write("Url,AdSlotValue,ContentType,ParentTopic,IsParentTopicSellable,PrimaryTag,IsPrimaryTagSellable,error");
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
			StartCrawler(words);
			CustomReporter.getInstance().writeReport();
			CustomReporter.getInstance().writeHtmlPropertyError();
			CustomReporter.getInstance().writeHtmlDevPropertyError();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("************* DONE ************");
		long execEndTime = System.nanoTime();
		long duration = execEndTime - execStartTime;
		double seconds = (double) duration / 1000000000.0;
		String execSeconds = new DecimalFormat("#.####").format(seconds);
		System.out.print(" (" + execSeconds + ")");
		if (!MainFrame.isHeadless) {
			mainFrame.dispose();
		}
	}

	private static void DisableLogging() {
		// Logger.getLogger("ac.biu.nlp.nlp.engineml").setLevel(Level.OFF);
		// Logger.getLogger("org.BIU.utils.logging.ExperimentLogger").setLevel(Level.OFF);
		// Logger.getRootLogger().setLevel(Level.OFF);
	}

	private static void StartCrawler(String[] siteList) throws Exception {

		String crawlStorageFolder = "crawlStorageFolder";
		int numberOfCrawlers = Integer.parseInt(EnvirommentManager
				.getInstance().getProperty("numberOfCrawlers"));

		CrawlConfig config = new CrawlConfig();
		//config.setUserAgentString(UserAgent);
	System.out.println(	config.getUserAgentString());
		config.setCrawlStorageFolder(crawlStorageFolder);
		config.setConnectionTimeout(Integer.parseInt(EnvirommentManager
				.getInstance().getProperty("ConnectionTimeout")));
		config.setMaxDepthOfCrawling(Integer.parseInt(EnvirommentManager
				.getInstance().getProperty("MaxDepthOfCrawling")));
		config.setMaxPagesToFetch(Integer.parseInt(EnvirommentManager
				.getInstance().getProperty("MaxPagesToFetch")));
		config.setIncludeBinaryContentInCrawling(false);
		config.setMaxConnectionsPerHost(Integer.parseInt(EnvirommentManager
				.getInstance().getProperty("MaxConnectionsPerHost")));
		config.setMaxTotalConnections(Integer.parseInt(EnvirommentManager
				.getInstance().getProperty("MaxTotalConnections")));
		config.setPolitenessDelay(Integer.parseInt(EnvirommentManager
				.getInstance().getProperty("PolitenessDelay")));
		config.setFollowRedirects(true);
		config.setConnectionTimeout(300000);
		config.setSocketTimeout( 200000);
		config.setIncludeHttpsPages(true);
		config.setMaxDownloadSize(10485760);
		config.setMaxOutgoingLinksToFollow(100000);
		config.setResumableCrawling(false);
		if (hasBasicAuth) {
			AuthInfo basicAuth = new BasicAuthInfo(userNameValue,
					passwordValue, loginPage);
			config.addAuthInfo(basicAuth);
		}
		if (hasFormAuth) {
			AuthInfo formAuth = new FormAuthInfo(userNameValue, passwordValue,
					loginPage, userNameAttribute, passwordAttribute);
			config.addAuthInfo(formAuth);
		}
		CustomReporter.getInstance().setMaxPagesToWriteReport(
				Integer.parseInt(EnvirommentManager.getInstance().getProperty(
						"MaxPagesToWriteReport")));

		/*
		 * Instantiate the controller for this crawl.
		 */
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		robotstxtConfig.setEnabled(enableRobotsTxt);
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig,
				pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher,
				robotstxtServer);

		/*
		 * For each crawl, you need to add some seed urls. These are the first
		 * URLs that are fetched and then the crawler starts following links
		 * which are found in these pages
		 */
		System.out.println("before seed");
		for (String site : siteList) {
			controller.addSeed(site);
		}
		System.out.println("after seed");
		/*
		 * Start the crawl. This is a blocking operation, meaning that your code
		 * will reach the line after this only when crawling is finished.
		 */
		
		controller.start(SiteCrawler.class, numberOfCrawlers);
		CrawlerDao crawlerDao = new CrawlerDao();
		crawlerDao.insertIntoLog(SiteCrawler.loglist);
		crawlerDao.updateLinksCount(SiteCrawler.totalCrwaled);
	}

	public static void initialzeTagsDictionary() {
		String csvFile = System.getProperty("user.dir") + File.separator
				+ "resources" + File.separator + "Dictionary.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = "\",\"";
		try {
			br = new BufferedReader( new InputStreamReader(
                    new FileInputStream(csvFile), "ISO-8859-1"));
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] Word = line.split(cvsSplitBy);
				if (Word[4].replaceAll("\"", "").equals("true")) {
					SiteCrawler.tagsDictionarySellable.put(
							Word[1].toLowerCase(), Word[3].toLowerCase());
				}

				else if (Word[4].replaceAll("\"", "").equals("false")) {
					SiteCrawler.tagsDictionaryNonSellable.put(
							Word[1].toLowerCase(), Word[2].toLowerCase());
				}
				SiteCrawler.isSensitive.put(Word[1].toLowerCase(), Word[5].replaceAll("\"", "").toLowerCase());

			}
			br.close();
		} catch (Exception ex) {

		}

	}

	public static String getExecutionTimeFormatted(double seconds) {
		int numberOfDays;
		int numberOfHours;
		int numberOfMinutes;
		int numberOfSeconds;

		if (seconds > (60 * 60 * 24)) // days
		{
			numberOfDays = (int) seconds / 86400;
			numberOfHours = ((int) seconds % 86400) / 3600;
			numberOfMinutes = (((int) seconds % 86400) % 3600) / 60;
			numberOfSeconds = (((int) seconds % 86400) % 3600) % 60;
			return numberOfDays + " Days, " + numberOfHours + " Hours, "
					+ numberOfMinutes + " Minutes";
		} else if (seconds > (60 * 60)) // hours
		{
			numberOfHours = ((int) seconds % 86400) / 3600;
			numberOfMinutes = (((int) seconds % 86400) % 3600) / 60;
			numberOfSeconds = (((int) seconds % 86400) % 3600) % 60;
			return numberOfHours + " Hours, " + numberOfMinutes + " Minutes";
		} else if (seconds > 60) // minutes
		{
			numberOfMinutes = (((int) seconds % 86400) % 3600) / 60;
			numberOfSeconds = (((int) seconds % 86400) % 3600) % 60;
			return numberOfMinutes + " Minutes, " + numberOfSeconds
					+ " Seconds";
		} else {
			String formattedSeconds = new DecimalFormat("#").format(seconds);
			return formattedSeconds + " Seconds";
		}

	}
}
