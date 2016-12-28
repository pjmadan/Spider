package univision.com.utilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import Entity.ConnectionInfo;
import Entity.Run;

public class UrlConnectionReader {
	Run run = new Run();

	//
	public static ConnectionInfo GetHtmlInfo(String urlLink) {

		String xmlstring = "";
		ConnectionInfo info = new ConnectionInfo();
		try {
			URL url = new URL(urlLink);
			long starTime = System.currentTimeMillis();
			URLConnection conn = url.openConnection();
			int respCode = ((HttpURLConnection) conn).getResponseCode();
			long elasedTime = System.currentTimeMillis() - starTime;
			info.setStatusCode(respCode);
			info.setResponseTime(elasedTime);

			if (respCode == 200) {
				// open the stream and put it into BufferedReader
				BufferedReader br = new BufferedReader(new InputStreamReader(
						conn.getInputStream()));

				String inputLine;
				while ((inputLine = br.readLine()) != null) {
					xmlstring = xmlstring + inputLine;
				}

				info.setHtml(xmlstring);
				br.close();
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}

		return info;
	}
}
