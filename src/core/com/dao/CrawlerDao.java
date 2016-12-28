package univision.com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import univision.com.crawler.SiteCrawler;
import Entity.Log;
import Entity.Run;

public class CrawlerDao {

	private Connection connection;
	private Statement statement;

	public CrawlerDao() {
	}

	public void insertintoRun(Run run) throws SQLException {
		// the mysql insert statement
		String query = " insert into run (links_count,run_time,execution_time) values (?,?,?)";
		ResultSet rs = null;
		PreparedStatement preparedStmt = null;
		try {
			int runId = 0;
			long time = getDateInTimeZone(System.currentTimeMillis(), "UTC")
					.getTime();
			Timestamp now = new Timestamp(time);

			connection = ConnectionFactory.getConnection();
			preparedStmt = connection.prepareStatement(query,
					Statement.RETURN_GENERATED_KEYS);
			preparedStmt.setInt(1, run.getLinksCount());
			preparedStmt.setTimestamp(2, now);
			preparedStmt.setInt(3, run.getLinksCount());
			preparedStmt.addBatch();
			preparedStmt.executeBatch();
			rs = preparedStmt.getGeneratedKeys();
			if (rs.next()) {
				runId = rs.getInt(1);
				SiteCrawler.runId = runId;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(preparedStmt);
			DbUtil.close(statement);
			DbUtil.close(connection);
		}
	}

	public void updateLinksCount(int linksCount) throws SQLException {
		String query = " update run set links_count = ? where run_id = ? ";
		ResultSet rs = null;
		PreparedStatement preparedStmt = null;
		try {

			connection = ConnectionFactory.getConnection();
			preparedStmt = connection.prepareStatement(query);
			preparedStmt.setInt(1, linksCount);
			preparedStmt.setInt(2, SiteCrawler.runId);
			preparedStmt.addBatch();
			preparedStmt.executeBatch();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(preparedStmt);
			DbUtil.close(statement);
			DbUtil.close(connection);
		}

	}

	public void insertIntoLog(ArrayList<Log> loglist) throws SQLException {

		if (loglist == null || loglist.size() <= 0) {
			return;
		}

		// the mysql insert statement
		String query = " insert into log (run_id,home_url,parent_url,link,depth,it_id,response,title,url_redirected_to)"
				+ " values (?,?,?,?,?,?,?,?,?)";
		ResultSet rs = null;
		PreparedStatement preparedStmt = null;
		try {
			connection = ConnectionFactory.getConnection();
			statement = connection.createStatement();

			// create the mysql insert preparedstatement
			preparedStmt = connection.prepareStatement(query);

			for (Log log : loglist) {
				preparedStmt.setString(1, String.valueOf(log.getRunId()));
				preparedStmt.setString(2, log.getHomeUrl());
				preparedStmt.setString(3, log.getParentUrl());
				preparedStmt.setString(4, log.getLink());
				preparedStmt.setInt(5, log.getDepth());
				preparedStmt.setInt(6, log.getLtId());
				preparedStmt.setInt(7, log.getResponse());
				preparedStmt.setString(8, log.getTitle());
				preparedStmt.setString(9, log.getUrlRedirectedTo());
				preparedStmt.addBatch();
			}
			// execute the preparedstatement
			preparedStmt.executeBatch();
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			DbUtil.close(rs);
			DbUtil.close(preparedStmt);
			DbUtil.close(statement);
			DbUtil.close(connection);
		}
	}

	private Date getDateInTimeZone(long currentDate, String timeZoneId) {

		Calendar mbCal = new GregorianCalendar(TimeZone.getTimeZone(timeZoneId));
		mbCal.setTimeInMillis(currentDate);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, mbCal.get(Calendar.YEAR));
		cal.set(Calendar.MONTH, mbCal.get(Calendar.MONTH));
		cal.set(Calendar.DAY_OF_MONTH, mbCal.get(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.HOUR_OF_DAY, mbCal.get(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, mbCal.get(Calendar.MINUTE));
		cal.set(Calendar.SECOND, mbCal.get(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, mbCal.get(Calendar.MILLISECOND));
		return cal.getTime();
	}

}
