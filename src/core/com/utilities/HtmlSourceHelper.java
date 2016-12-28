package univision.com.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlSourceHelper {

	public static String getValueByExpression(String html, String expression)
	{
		String value= "";
		Pattern pattern = Pattern.compile(
				expression,
				Pattern.DOTALL);
		Matcher matcher = pattern.matcher(html);
		matcher = pattern.matcher(html);
		if (matcher.find()) {
			value = matcher.group(1);
		} 
		else
		{
			return null;
		}
		return value;
	}
	
}
