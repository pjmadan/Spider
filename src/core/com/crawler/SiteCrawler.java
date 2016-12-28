/**
 * 
 */
package univision.com.crawler;

/**
 * @author Mhasasneh
 *
 */
import java.awt.GraphicsEnvironment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import univision.com.dao.CrawlerDao;
import univision.com.utilities.CustomReporter;
import univision.com.utilities.EnvirommentManager;
import univision.com.utilities.HtmlSourceHelper;
import univision.com.utilities.MainFrame;
import univision.com.utilities.TitleExtractor;
import Entity.Log;

public class SiteCrawler extends WebCrawler {
	private final static Pattern EXTENSION_FILTERS = Pattern.compile(".*(\\.("
			+ EnvirommentManager.getInstance()
					.getProperty("UrlExtensionFilter") + ")).*");
	private final static String STR_INCLUDE_DOMAINS = EnvirommentManager
			.getInstance().getProperty("IncludeDomains");
	private final static String[] INCLUDE_DOMAINS =STR_INCLUDE_DOMAINS.split("\\|");
	private final static String strIncludeUrlsContains = EnvirommentManager
			.getInstance().getProperty("IncludeUrlsContains");
	private final static String[] includeUrlsContains =strIncludeUrlsContains.split("\\|");
	private static String urlContainsFilter = EnvirommentManager.getInstance()
			.getProperty("UrlContainsFilter");
	private static boolean isCheckPrimaryAndTopicValue = Boolean
			.parseBoolean(EnvirommentManager.getInstance().getProperty(
					"CheckPrimaryAndTopicValue"));
	private static boolean parseTitle = Boolean
			.parseBoolean(EnvirommentManager.getInstance().getProperty(
					"ParseTitle"));
	private static boolean ParseRedirectedToUrl = Boolean
			.parseBoolean(EnvirommentManager.getInstance().getProperty(
					"ParseRedirectedToUrl"));
	private static boolean checkHtmlEnabled = Boolean
			.parseBoolean(EnvirommentManager.getInstance().getProperty(
					"CheckHtmlEnabled"));
	private final static Pattern CONTAINS_FILTERS = Pattern
			.compile(urlContainsFilter);
	private final static String URL_BASED_ON_RESPONSE_FILTER = EnvirommentManager
			.getInstance().getProperty("UrlBasedOnResponseFilter");
	private final static String Crawl_Urls_Just_Include = EnvirommentManager
			.getInstance().getProperty("CrawlUrlsJustInclude");
	public static ArrayList<Log> loglist = new ArrayList<Log>();
	CrawlerDao crawlerDao = new CrawlerDao();
	public static Integer totalCrwaled = 0;
	public static int runId;
	public static List<Integer> reportedStatusCode;
	public static HashMap<Integer, Integer> failedUrlsCount = new HashMap<Integer, Integer>();
	public static String[] words = null;
	private static Integer totalPassedUrls = 0;
	private static Integer totalFailedUrls = 0;
	private static Integer maxPagesToSaveDatabase = EnvirommentManager
			.getInstance().getInteger("MaxPagesToSaveDatabase");
	public static HashMap<String, String> tagsDictionarySellable = new HashMap<String, String>();
	public static HashMap<String, String> tagsDictionaryNonSellable = new HashMap<String, String>();
	public static HashMap<String, String> isSensitive = new HashMap<String, String>();

	/**
	 * You should implement this function to specify whether the given url
	 * should be crawled or not (based on your crawling logic).
	 */

	@Override
	public boolean shouldVisit(Page page, WebURL url) {
		try {

			boolean includedSite = false;
			boolean urlHaveAccess =true;
			String href = url.getURL().toLowerCase();
			try {
				URL urlHref = new URL(href);
				String[] allIncludedSites =null;
				if(!STR_INCLUDE_DOMAINS.trim().isEmpty()){
				 allIncludedSites = ArrayUtils.addAll(words,INCLUDE_DOMAINS);
				}else{
					allIncludedSites = words;
				}
			
				if(!Crawl_Urls_Just_Include.isEmpty()){
					if(!href.toLowerCase().contains( Crawl_Urls_Just_Include.toLowerCase())){
						urlHaveAccess = false;
					}
				}

				for (String string : allIncludedSites) {

					URL urlSite = new URL(string);
					if(!strIncludeUrlsContains.isEmpty()){
					for (String urlToContain : includeUrlsContains) {
						for (String parentSite : words) {
							URL parentSiteUrl = new URL(parentSite);
							if (new URL(url.getParentUrl())
									.getHost()
									.toLowerCase()
									.replace("www.", "")
									.contains(
											parentSiteUrl.getHost()
													.toLowerCase()
													.replace("www.", ""))) {
								if (href.contains(urlToContain)) {
									includedSite = true;
								}
							}
						}

					}
					}
					if (urlHref
							.getHost()
							.toLowerCase()
							.replace("www.", "")
							.contains(
									urlSite.getHost().toLowerCase()
											.replace("www.", ""))) {
						includedSite = true;
						
						break;
					}
				}
			} catch (MalformedURLException e) {
				includedSite = false;
			}

			if (urlContainsFilter.isEmpty()) {
				return !EXTENSION_FILTERS.matcher(href).matches()
						&& includedSite && urlHaveAccess;
			} else {
				return !EXTENSION_FILTERS.matcher(href).matches()
						&& !CONTAINS_FILTERS.matcher(href).find()
						&& includedSite && urlHaveAccess;

			}
		} catch (Exception ex) {
			System.out.println("there is an error in check visit");
			return false;
		}
	}

	@Override
	public void visit(Page page) {
		if (checkHtmlEnabled) {
			checkHtml(page);
		}
		if(isCheckPrimaryAndTopicValue)
		{
			checkPrimaryAndTopicValues(page);
		}
	}
	public void checkPrimaryAndTopicValues(Page page)
	{

		String html = "";
		String primaryTag = "";
		String parentTopic = "";
		boolean primaryTagExist = false;
		boolean parentTopicExist = false;
		String error ="";
		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page
					.getParseData();

			html = htmlParseData.getHtml();
			String tagValue =HtmlSourceHelper.getValueByExpression(html,"data-tealium-utag(.*?)/div>");
			if(page.getWebURL().getURL().contains("ligamx.")){
				System.out.println(tagValue);
			}
			String value =	HtmlSourceHelper.getValueByExpression(tagValue, ",&quot;primary_tag&quot;:&quot;(.*?)&quot;,&quot;");
			if (value ==null) {
				value =	HtmlSourceHelper.getValueByExpression(tagValue, "primary_tag\":\"(.*?)\",\"");
			}
			if (value !=null) {
				primaryTag = value;
			} else {
				primaryTag ="N/A";
				error = "could not find primary tag";
			}
			value =null;
			value =	HtmlSourceHelper.getValueByExpression(tagValue, ",&quot;parent_topic&quot;:&quot;(.*?)&quot;,&quot;");
			if (value ==null) {
				value =	HtmlSourceHelper.getValueByExpression(tagValue, "parent_topic\":\"(.*?)\",\"");
			}
			if (value !=null) {
				parentTopic = value;
			} else {
				parentTopic = "N/A";
				error = "could not find parent topic";
			}
			if (!error.isEmpty()) {
				CustomReporter.getInstance().appendHtmlPrimaryAndParentTagError(
						page.getWebURL() + ","
								+ parentTopic + ","
								+ primaryTag );

			}
		}
	}
	public void checkHtml(Page page){
		String html = "";
		String primaryTag = "";
		String parentTopic = "";
		String contentType = "";
		boolean primaryTagExist = false;
		boolean parentTopicExist = false;
		boolean contentTypeThere = false;
		String adWrrapperSlotName = "";
		boolean isPrimaryTagSellable = false;
		boolean isParentTopicSellable = false;
		String error = "";
		String errorDev = "";
		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page
					.getParseData();

			html = htmlParseData.getHtml();
			String value =	HtmlSourceHelper.getValueByExpression(html, ",&quot;primary_tag&quot;:&quot;(.*?)&quot;,&quot;");
			if (value !=null) {
				primaryTag = value;
			} else {
				error = "could not find primary tag";
			}
			value =null;
			value =	HtmlSourceHelper.getValueByExpression(html, ",&quot;parent_topic&quot;:&quot;(.*?)&quot;,&quot;");
			
			if (value !=null) {
				parentTopic = value;
			} else {
				error = "could not find parent topic";
			}

			value =null;
			value =	HtmlSourceHelper.getValueByExpression(html, ",&quot;content_type&quot;:&quot;(.*?)&quot;,&quot;");
			
			if (value !=null) {
				contentType = value;
				if (contentType.equals("soccermatch")) {
					contentType = "matchcenter";
				}
				if (contentType.equals("section")) {
					contentType = "homepage";
				}
				if (contentType.equals("reactionpoll")) {
					contentType = "music";
				}

			} else {
				error = "could not find content type";
			}
			String adWrraperDiv = "";
			value =null;
			value =	HtmlSourceHelper.getValueByExpression(html, "ad_wrapper(.*?)/div");
			
			if (value !=null) {
				adWrraperDiv = value;

			} else {
				error = "could not find add wrraper div";
			}
			value =null;
			value =	HtmlSourceHelper.getValueByExpression(html,"data-slot-name=\"(.*?)\"");
			
			if (value !=null) {
				adWrrapperSlotName =value;
			} else {

				if (tagsDictionarySellable.containsKey(primaryTag)) {
					error += "The doubleClickAdSlot is not in the response and the (Primary Tag) is Sellable.";
				}

				if (tagsDictionarySellable.containsKey(parentTopic)) {
					error += "The doubleClickAdSlot is not in the response and the (Parent Topic) is Sellable.";
				}
				if (error.isEmpty()) {
					return;
				}
			}
		}
		if (error.isEmpty()) {
			String[] adWrapperSlotNameList = adWrrapperSlotName.split("/");

			if (!(tagsDictionarySellable.containsKey(primaryTag) || tagsDictionaryNonSellable
					.containsKey(primaryTag))) {
				errorDev += "(Primary Tag) is missing from the provided spanish name list./";
			}
			if (!(tagsDictionarySellable.containsKey(parentTopic) || tagsDictionaryNonSellable
					.containsKey(parentTopic))) {
				errorDev += "(Parent Topic) is missing from the provided spanish name list./";
			}

			isPrimaryTagSellable = tagsDictionarySellable
					.containsKey(primaryTag);
			isParentTopicSellable = tagsDictionaryNonSellable
					.containsKey(parentTopic);

			// Content Type
			// check----------------------------------------
			if (adWrrapperSlotName.equals("/sensitive")) {
				if (!((isSensitive.containsKey(primaryTag) && isSensitive
						.get(primaryTag).equals("true")) || isSensitive
						.containsKey(parentTopic)
						&& isSensitive.get(parentTopic).equals("true"))) {
					error += "The doubleClickAdSlot=\"/sensitive\" and non of Primary Tag and PArent topic is sensitive=true./ ";
				}
			}

			else {
				if (!Arrays.asList(adWrapperSlotNameList).contains(
						contentType.replaceAll(" ", "").trim())) {
					error += "(Content Type) not found./";
				}
				if ((tagsDictionarySellable.containsKey(primaryTag) || tagsDictionaryNonSellable
						.containsKey(primaryTag))
						&& Arrays
								.asList(adWrapperSlotNameList)
								.contains(
										(isPrimaryTagSellable ? tagsDictionarySellable
												.get(primaryTag).replace(
														" ", "")
												: tagsDictionaryNonSellable
														.get(primaryTag)
														.replace(" ", "")))) {
					primaryTagExist = true;
				}

				if ((tagsDictionarySellable.containsKey(parentTopic) || tagsDictionaryNonSellable
						.containsKey(parentTopic))
						&& Arrays
								.asList(adWrapperSlotNameList)
								.contains(
										(isParentTopicSellable ? tagsDictionarySellable
												.get(parentTopic).replace(
														" ", "")
												: tagsDictionaryNonSellable
														.get(parentTopic)
														.replace(" ", "")))) {
					parentTopicExist = true;
				}

				if (!primaryTagExist && isPrimaryTagSellable) {
					error += "(Primary Tag) is sellable but not found on Ad slot./";
				}
				if (!parentTopicExist && isParentTopicSellable) {
					error += "(Parent Topic) is sellable but not found on Ad slot./";

				}
				if (primaryTagExist && !isPrimaryTagSellable) {
					error += "(Primary Tag) is not sellable but found on Ad slot./";
				}
				if (parentTopicExist && !isParentTopicSellable) {
					error += "(Parent Topic) is not sellable but found on Ad slot./";
				}
			}
		}

		if (!error.isEmpty()) {
			CustomReporter.getInstance().appendHtmlPropertyError(
					page.getWebURL() + "," + adWrrapperSlotName + ","
							+ contentType + "," + parentTopic + ","
							+ (isParentTopicSellable ? "true" : "false")
							+ "," + isSensitive.get(parentTopic) + ","
							+ primaryTag + ","
							+ (isPrimaryTagSellable ? "true" : "false")
							+ "," + isSensitive.get(primaryTag) + ","
							+ tagsDictionarySellable.get(parentTopic) + ","
							+ tagsDictionarySellable.get(primaryTag) + ","
							+ error);

		}
		if (!errorDev.isEmpty()) {
			CustomReporter.getInstance().appendHtmlDevPropertyError(
					page.getWebURL() + ", " + adWrrapperSlotName + ", "
							+ contentType + "," + parentTopic + ","
							+ (isParentTopicSellable ? "true" : "false")
							+ "," + primaryTag + ","
							+ (isPrimaryTagSellable ? "true" : "false")
							+ "," + errorDev);
		}

	}
	/**
	 * This function is called when a page is fetched and ready to be processed
	 * by your program.
	 */

	int counter = 0;

	// @Override
	// public void visit(Page page) {
	// String url = page.getWebURL().getURL();
	//
	// if (page.getParseData() instanceof HtmlParseData) {
	// HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
	// List<WebURL> links = htmlParseData.getOutgoingUrls();
	// //System.out.println(String.valueOf(links.size()+" urls in the root "+url));
	// }
	// }
	public String getUrlRedirectedTo(String url){
		try {
			URLConnection con = new URL( url ).openConnection();
			con.connect();
			InputStream is = con.getInputStream();
			return con.getURL().toString();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "N/A";
	}
	@Override
	protected void handlePageStatusCode(WebURL webUrl, int statusCode,
			String statusDescription) {
		String url = webUrl.getURL();
		String title = "N/A";
		String urlRedirectedTo ="N/A"; 
		if(ParseRedirectedToUrl){
			urlRedirectedTo = getUrlRedirectedTo(webUrl.getURL());
		}
		
		if(parseTitle){
			title =StringEscapeUtils.unescapeHtml4(TitleExtractor.getPageTitle(url));
		}
		boolean result = true;
		// insert to database root crawler table just when links unavailable
		Log log = null;
		try {
			boolean filteredOnUrlAndStatus = false;
			if (!URL_BASED_ON_RESPONSE_FILTER.isEmpty()) {
				String[] UrlResponsesToFilter = URL_BASED_ON_RESPONSE_FILTER
						.split(",");

				for (String urlResponseFilter : UrlResponsesToFilter) {
					String[] urlResponse = urlResponseFilter.split("\\|");
					if (statusCode == Integer.parseInt(urlResponse[0])
							&& url.contains(urlResponse[1])) {
						filteredOnUrlAndStatus = true;
					}
				}
			}
			if (reportedStatusCode.contains(statusCode)
					&& !filteredOnUrlAndStatus) {
				totalFailedUrls++;
				result = false;
				log = new Log();
				// generate run ID;
				SimpleDateFormat ft = new SimpleDateFormat("MMddhhmmss");
				log.setLink(url);
				log.setParentUrl(webUrl.getParentUrl());
				log.setResponse(statusCode);
				log.setLtId(1);
				log.setDepth(webUrl.getDepth());
				log.setRunId(runId);
				log.setLogId("");
				log.setTitle(title);
				log.setUrlRedirectedTo(urlRedirectedTo);
				log.setHomeUrl((webUrl.getDomain()));
				synchronized (SiteCrawler.loglist) {
					loglist.add(log);
					if (loglist.size() >= maxPagesToSaveDatabase) {
						crawlerDao.insertIntoLog(loglist);
						crawlerDao.updateLinksCount(totalCrwaled);
						loglist = new ArrayList<Log>();
					}
				}

				Integer count = failedUrlsCount.get(statusCode) + 1;
				failedUrlsCount.put(statusCode, count);
			} else {
				totalPassedUrls++;
			}
		} catch (MalformedURLException | SQLException e) {
			e.printStackTrace();
		}

		finally {
			CustomReporter.getInstance().appendReportContent(
					(result ? "Success" : url), result, webUrl.getParentUrl(),
					log);
			totalCrwaled++;

			try {

				Integer maxDepth = Integer.parseInt(EnvirommentManager
						.getInstance().getProperty("MaxDepthOfCrawling"));
				// Integer maxCrwaling =
				// Integer.parseInt(EnvirommentManager.getInstance().getProperty("MaxPagesToFetch"));
				if (!MainFrame.isHeadless) {
					MainFrame.setDetailsLabel("NUmberOfLinksCrawled",
							totalCrwaled.toString());
					MainFrame.setDetailsLabel("MaxDepth",
							maxDepth == -1 ? "N/A" : maxDepth.toString());
					MainFrame.setDetailsLabel("Root", webUrl.getDomain());
					MainFrame.setPieChart(totalPassedUrls, totalFailedUrls,
							totalCrwaled);
					MainFrame.setBarChart(failedUrlsCount);
				} else {
					System.out.println(url);
					MainFrame.updateTerminalScreen(totalPassedUrls,
							totalFailedUrls, totalCrwaled, maxDepth);
				}

				// if (maxCrwaling) {
				// MainFrame.setDetailsLabel("CrawleSpeed",
				// "Generating report...");
				// }

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
