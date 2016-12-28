package univision.com.utilities;

import java.awt.*;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.border.Border;

import univision.com.crawler.SiteCrawler;
import univision.com.crawler.crawler;
import univision.com.dao.CrawlerDao;

public class MainFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel contentPane;
	JLabel imageLabel = new JLabel();
	JLabel headerLabel = new JLabel();
	static JLabel pieChart;
	static JLabel barChart;
	JPanel detailsFrame;
	static String pieChartPath;
	static String barChartPath;
	public static boolean isHeadless = Boolean.parseBoolean(EnvirommentManager.getInstance().getProperty("HeadlessMode")) || GraphicsEnvironment.isHeadless();
	private static HashMap<String, JLabel> componentMap = new HashMap<String, JLabel>();

	public MainFrame() {
		try {
			JFrame mainFrme = this;
			// GridLayout experimentLayout = new GridLayout(5,1);
			// mainFrme.setLayout(experimentLayout);
			seticon();
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			contentPane = (JPanel) getContentPane();
			contentPane.setLayout(new BorderLayout());
			contentPane.setBackground(Color.WHITE);
			setFrameAsMaximized();
			setResizable(false);
			setTitle("Crawler bot");

			// add the header label
			Border paddingBorder = BorderFactory.createEmptyBorder(20, 10, 10,
					10);
			Border border = BorderFactory.createLineBorder(Color.white);
			headerLabel.setBorder(BorderFactory.createCompoundBorder(border,
					paddingBorder));
			headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
			headerLabel.setFont(new java.awt.Font("Arial", Font.BOLD, 16));
			SimpleDateFormat formatter = new SimpleDateFormat("MMM dd HH:mm:ss");
			headerLabel.setText("Crawler bot is crawling! ("
					+ formatter.format(Calendar.getInstance().getTime()) + ")");
			headerLabel.setBackground(Color.red);

			JPanel headerFrame = new JPanel();
			headerFrame.setBackground(Color.white);
			headerFrame.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridwidth = GridBagConstraints.REMAINDER;
			headerFrame.add(headerLabel, c);

			String path = System.getProperty("user.dir") + File.separator
					+ "resources" + File.separator;
			String imageFileName = path + "spider.gif";
			ImageIcon ii = new ImageIcon(imageFileName);
			JLabel spider = new JLabel();
			spider.setIcon(ii);
			spider.setIcon(ii);
			headerFrame.add(spider);

			contentPane.add(headerFrame, BorderLayout.PAGE_START);

			// Add charts
			JPanel chartsFrame = new JPanel();
			GridLayout chartsFrameLayout = new GridLayout(1, 2);
			chartsFrame.setLayout(chartsFrameLayout);
			chartsFrame.setBackground(Color.white);

			setPieChart(100, 0, 100);
			setBarChart(SiteCrawler.failedUrlsCount);
			chartsFrame.add(barChart);
			chartsFrame.add(pieChart);
			contentPane.add(chartsFrame, BorderLayout.CENTER);

			// Create buttons
			JButton buttonStop = new JButton("Stop");
			JButton buttonReport = new JButton("Generate report");
			buttonStop.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					CrawlerDao crawlerDao = new CrawlerDao();
			        try {
						crawlerDao.insertIntoLog(SiteCrawler.loglist);
				        crawlerDao.updateLinksCount(SiteCrawler.totalCrwaled);
					} catch (SQLException ex) {
						ex.printStackTrace();
					}
					CustomReporter.getInstance().writeReport();
					mainFrme.dispose();
					System.exit(ABORT);
				}
			});
			buttonReport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					CustomReporter.getInstance().writeReport();
				}
			});
			// Add bottom container
			detailsFrame = new JPanel();
			GridLayout detailsFrameLayout = new GridLayout(6, 2);
			detailsFrame.setLayout(detailsFrameLayout);
			// Add controls
			detailsFrame.add(getDetailsLabel("Number of links crawled",
					"title1"));
			detailsFrame.add(getDetailsLabel("-", "NUmberOfLinksCrawled"));
			detailsFrame.add(getDetailsLabel("Time elapsed", "title2"));
			detailsFrame.add(getDetailsLabel("-", "TimeElapsed"));
			detailsFrame
					.add(getDetailsLabel("Max depth of crawling", "title3"));
			detailsFrame.add(getDetailsLabel("-", "MaxDepth"));
			detailsFrame.add(getDetailsLabel("Root Url", "title4"));
			detailsFrame.add(getDetailsLabel("-", "Root"));
			detailsFrame.add(getDetailsLabel("Links crawled/sec", "title5"));
			detailsFrame.add(getDetailsLabel("-", "CrawleSpeed"));
			detailsFrame.add(buttonStop);
			detailsFrame.add(buttonReport);
			contentPane.add(detailsFrame, BorderLayout.PAGE_END);

			// Show form
			this.setLocationRelativeTo(null);
			this.setVisible(true);

		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public static void setDetailsLabel(String name, String text) {
		if (!isHeadless) {
			((JLabel) componentMap.get(name)).setText("<html><b>" + text
					+ "</b></html>");
		}
	}

	public static void setPieChart(Integer pass, Integer fail, Integer total) {
		if (!isHeadless) {
			try {

				if (pieChart == null)
					pieChart = new JLabel();

				if (pieChart.getWidth() == 0) {
					setUnAvailableLabel(pieChart);
					return;
				}
				String path = "https://chart.googleapis.com/chart?cht=p&chs="
						+ pieChart.getWidth() + "x" + pieChart.getHeight()
						+ "&chd=t:" + fail + "," + pass
						+ "&chl=Failed|Passed&chco=FF3300,33CC33&chds=0,"
						+ total;

				if (path.equals(pieChartPath))
					return;

				pieChartPath = path;

				URL url;
				url = new URL(path);
				BufferedImage image = ImageIO.read(url);
				pieChart.setIcon(new ImageIcon(image));
			} catch (Exception e) {
				setUnAvailableLabel(pieChart);
			}
		}
		
	}

	private static void setUnAvailableLabel(JLabel label) {
		String path = System.getProperty("user.dir") + File.separator
				+ "resources" + File.separator;
		String imageFileName = path + "loading.gif";
		ImageIcon ii = new ImageIcon(imageFileName);
		label.setIcon(ii);
	}

	private void seticon() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(
				Thread.currentThread().getContextClassLoader()
						.getResource("spider.jpg")));
	}

	public static void setBarChart(HashMap<Integer, Integer> failedUrlsCount) {
		if (!isHeadless) {
			try {

				if (barChart == null)
					barChart = new JLabel();
				if (barChart.getWidth() == 0) {
					setUnAvailableLabel(barChart);
					return;
				}
				String xAxisValue = "";
				String yAxisValue = "";
				for (Integer code : failedUrlsCount.keySet()) {
					xAxisValue += "|" + code;
					yAxisValue += failedUrlsCount.get(code) + ",";
				}
				yAxisValue = yAxisValue.substring(0, yAxisValue.length() - 1);

				String path = "https://chart.googleapis.com/chart?chxt=x,y&chs="
						+ barChart.getWidth()
						+ "x"
						+ barChart.getHeight()
						+ "&cht=bvs&chco=76A4FB&chls=2.0&chxl=0:"
						+ xAxisValue
						+ "&chbh=a,10,10&chd=t:"
						+ yAxisValue
						+ "&chds=a&chm=N,000000,0,-1,11";
				if (path.equals(barChartPath))
					return;

				barChartPath = path;

				URL url;
				url = new URL(path);
				BufferedImage image = ImageIO.read(url);
				barChart.setIcon(new ImageIcon(image));
			} catch (Exception e) {
				setUnAvailableLabel(barChart);
			}
		}
	}

	private JLabel getDetailsLabel(String text, String name) {
		JLabel label = new JLabel();
		label.setName(name);
		label.setText("<html><body>" + text + "</body></html>");
		Color customColor = new Color(4, 119, 205);
		label.setBackground(customColor);
		label.setForeground(Color.white);
		label.setOpaque(true);
		label.setFont(new java.awt.Font("Arial", Font.PLAIN, 12));
		Border paddingBorder = BorderFactory.createEmptyBorder(1, 10, 1, 10);
		Border border = BorderFactory.createLineBorder(Color.white);
		label.setBorder(BorderFactory.createCompoundBorder(border,
				paddingBorder));
		componentMap.put(name, label);
		return label;
	}

	private void setFrameAsMaximized() {
		final GraphicsConfiguration config = this.getGraphicsConfiguration();
		final int left = Toolkit.getDefaultToolkit().getScreenInsets(config).left;
		final int right = Toolkit.getDefaultToolkit().getScreenInsets(config).right;
		final int top = Toolkit.getDefaultToolkit().getScreenInsets(config).top;
		final int bottom = Toolkit.getDefaultToolkit().getScreenInsets(config).bottom;

		final Dimension screenSize = Toolkit.getDefaultToolkit()
				.getScreenSize();
		final int width = screenSize.width - left - right;
		final int height = screenSize.height - top - bottom;
		setSize(width, height);
	}
	
	public static void updateTerminalScreen(int totalPassedUrls,int totalFailedUrls,int totalCrwaled,int maxDepth)
	{
		System.out.println("Number Of Links Crawled:" +  totalCrwaled + " -- Total Passed Urls:" + totalPassedUrls + " -- Total Failed Urls:" + totalFailedUrls  + " -- CrawleSpeed: "+ crawler.linksPerSecond + " urls/second") ;
		
	}
}
