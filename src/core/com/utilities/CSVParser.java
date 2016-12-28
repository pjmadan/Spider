package univision.com.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import Entity.CrawlerLinkProp;
import univision.com.crawler.crawlerbotlist;

public class CSVParser {

	public static int websitecount = 0;
	
	public static ArrayList<crawlerbotlist> parseLinksCsv (String filepath)
	{
		BufferedReader br=null;
		String line = "";
		String cvsSplitBy = ",";
		ArrayList<crawlerbotlist> websitelist = new ArrayList<crawlerbotlist>();

		try {

		  br = parsecsvfile(filepath);

			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] lines = line.split(cvsSplitBy);
				crawlerbotlist items = new crawlerbotlist();
				items.identifier = websitecount;
				items.websiteurl = lines[0];
				items.elementype = lines[1];
				websitelist.add(items);
				websitecount++;
			}

		}

		catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		catch (IOException e) {
			e.printStackTrace();
		}

		finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return websitelist;
	}
	
	public static ArrayList<CrawlerLinkProp> parseLinksPropCsv(String filepath)
	{
		BufferedReader br=null;
		String line = "";
		String cvsSplitBy = ",";
		ArrayList<CrawlerLinkProp> webSitePropList = new ArrayList<CrawlerLinkProp>();

		try {

		  br = parsecsvfile(filepath);

			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] lines = line.split(cvsSplitBy);
				CrawlerLinkProp items = new CrawlerLinkProp();
				items.setTage(lines[0]);
				items.setAttribute(lines[1]);
				items.setType(lines[2]);
				items.setAssertion(lines[3]);
				webSitePropList.add(items);
			}

		}

		catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		catch (IOException e) {
			e.printStackTrace();
		}

		finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return webSitePropList;
	}
	
	private static BufferedReader parsecsvfile(String filepath) {

		/*
		 * This subroutine will parse an csv file and return an array that
		 * contains objects which have website URL's, elements that need to be
		 * verified and the assertions configured for each
		 */

		String csvFile = filepath;
		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(csvFile));

		}

		catch (FileNotFoundException e) {
			e.printStackTrace();
		}


		return br;
	}
	
	public static String[] parsecsvfileToSet(String filepath) {
		String[] lines = {};
		String csvFile = filepath;
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {
			br = new BufferedReader(new FileReader(
					System.getProperty("user.dir") + File.separator + csvFile));

			while ((line = br.readLine()) != null) {
				lines = line.split(cvsSplitBy);
			}
		}

		catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		catch (IOException e) {
			e.printStackTrace();
		}

		finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return lines;
	}
	public static String[] parseCsvUrls (String[] fileNames,String urlItemName)
	{
		BufferedReader br=null;
		String line = "";
		String cvsSplitBy = ",";
		int itemIndex=-1;
		ArrayList<String> urls = new ArrayList<String>();

		for(int x=0; x< fileNames.length;x++)
		{
		try {
		  br = parsecsvfile(System.getProperty("user.dir") + File.separator + "resources" + File.separator +fileNames[x]);
		 // String headLine = br.readLine();
	//	  String[] headLineItems = headLine.split(cvsSplitBy);
//			for(int i = 0; i < headLineItems.length ; i++)
//			{
//				if (headLineItems[i].equals(urlItemName))
//				{
//					itemIndex =i;
//					break;
//				}
//			}
//			if (itemIndex == -1)
//			{
//				String[] urlsList = new String[urls.size()];
//				urlsList =urls.toArray(urlsList);
//				return urlsList;
//				
//			}
			while ((line = br.readLine()) != null) {
				// use comma as separator
			//	String[] lines = line.split(cvsSplitBy);
				if (!line.trim().isEmpty()){
				urls.add(line);
				}
				//websitecount++;
			}

		}

		catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		catch (IOException e) {
			e.printStackTrace();
		}

		finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		}
		
		String[] urlsList = new String[urls.size()];
		urlsList =urls.toArray(urlsList);
		return urlsList;
	}
}
