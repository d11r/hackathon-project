package connect.github;

import model.github.Issue;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import static connect.github.GithubApi.getIssues;


public class GithubSourceTask extends SourceTask {

	private static TimeZone tzUTC = TimeZone.getTimeZone("UTC");
	private static DateFormat dfZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	private static DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	static {
		dfZULU.setTimeZone(tzUTC);
	}

	private String version = "0.0.1";
	
	private String githubUser;
	private String githubPass;
	private String githubIssuesTopic;
	private String githubInterval;
	private Integer interval;

	private Date snapshotDate;

	// millis of last poll
	private long lastPoll = 0;

	private Logger log = Logger.getLogger(GithubSourceTask.class.getName());

	@Override
	public List<SourceRecord> poll() throws InterruptedException {
//		String URL = "https://api.github.com/repos/flutter/flutter/issues";
//		Issue[] issues = getIssues(URL, "marko-brodarski", "CQAn57N7j4NzWQp");

		List<SourceRecord> records = new ArrayList<>();

		// log.info("lastPollDelta:" + (System.currentTimeMillis() - lastPoll) + " interval:" + interval );

		if ( lastPoll != 0 ) {
			if ( System.currentTimeMillis() < ( lastPoll + (interval * 1000) ) ) {
				log.info("----------------------------------------------------------- exit polling, " + ( System.currentTimeMillis() - lastPoll ) / 1000 + " secs since last poll.");
				Thread.sleep(1000);
				return records;
			}
		}

		lastPoll = System.currentTimeMillis();

		String snapshotDateString = ymd.format(snapshotDate);

		int page = 0;

		if ( sonarProjectKeys!= null && !sonarProjectKeys.isEmpty()) {

			SonarcubeIssuesResult iResult;
			do {
				page++;
				iResult = SonarqubeApi.getIssues(sonarURL, sonarUser, sonarPass, sonarProjectKeys, page);
				records.addAll( getSonarIssueRecords(iResult, snapshotDateString) );
			} while ( page*iResult.paging.pageSize < iResult.paging.total );

		}

		if ( sonarBaseComponentKey != null && !sonarBaseComponentKey.isEmpty() ) {

			page = 0;
			SonarcubeMeasuresResult smr;
			do {
				page++;
				smr = SonarqubeApi.getMeasures(sonarURL, sonarUser, sonarPass, sonarMetrics, sonarBaseComponentKey, page);
				records.addAll( getSonarMeasureRecords(smr, snapshotDateString) );
			} while ( page * smr.paging.pageSize < smr.paging.total );

		}

		return records;
	}

	private List<SourceRecord>  getSourceRecords( Issue[] issues ) {
		//TODO: IMPLEMENT!
		return null;
	}
	
	@Override
	public void start(Map<String, String> props) {

		log.info("connect-github: start");
		log.info(props.toString());

		githubUser 				= props.get( GithubSourceConfig.GITHUB_USER_CONFIG );
		githubPass 				= props.get( GithubSourceConfig.GITHUB_PASS_CONFIG );
		githubIssuesTopic 		= props.get( GithubSourceConfig.GITHUB_ISSUES_TOPIC_CONFIG );
		githubInterval 			= props.get( GithubSourceConfig.GITHUB_INTERVAL_SECONDS_CONFIG );
//		githubAPIUrl			= props.get( GithubSourceConfig.GITHUB_)
		
		if ( (githubInterval == null || githubInterval.isEmpty()) ) {
			interval = 3600;
		} else {
			interval = Integer.parseInt(githubInterval);
		}
	}

	@Override
	public void stop() {

	}

	public String version() {
		return version;
	}2
}
