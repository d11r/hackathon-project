package connect.github;

import model.github.Issue;
import model.github.Repository;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;


public class GithubSourceTask extends SourceTask {
    private static final String GITHUB_USER = "marko-brodarski";
    private static final String GITHUB_PASSWORD = "CQAn57N7j4NzWQp";

    private GithubApi api = new GithubApi(GITHUB_USER, GITHUB_PASSWORD);

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

//    private Date snapshotDate;

    // millis of last poll
    private long lastPoll = 0;

    private Logger log = Logger.getLogger(GithubSourceTask.class.getName());

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        String repository_full_name = "rust-lang/rust";
        Repository repository = api.getRepository(repository_full_name);

        List<SourceRecord> records = new ArrayList<>();

        // Throttle
//        log.info("lastPollDelta:" + (System.currentTimeMillis() - lastPoll) + " interval:" + interval);
//        if (lastPoll != 0) {
//            if (System.currentTimeMillis() < (lastPoll + (interval * 1000))) {
//                log.info("----------------------------------------------------------- exit polling, " + (System.currentTimeMillis() - lastPoll) / 1000 + " secs since last poll.");
//                Thread.sleep(1000);
//                return records;
//            }
//        }
//        lastPoll = System.currentTimeMillis();
//        String snapshotDateString = ymd.format(snapshotDate);
        // end throttle

        // in order to start from 1, we have to assign it 0 before the do-while loop.
        int page = 0;
        List<Issue> issues;

        do {
            page++;
            issues = api.getIssues(repository, page);

            System.out.println("Got " + issues.size() + " issues from page " + page);

            issues.stream().map(this::getSourceRecords).forEach(records::add);
        } while (issues.size() > 0);

//        if (sonarBaseComponentKey != null && !sonarBaseComponentKey.isEmpty()) {
//
//            page = 0;
//            SonarcubeMeasuresResult smr;
//            do {
//                page++;
//                smr = SonarqubeApi.getMeasures(sonarURL, sonarUser, sonarPass, sonarMetrics, sonarBaseComponentKey, page);
//                records.addAll(getSonarMeasureRecords(smr, snapshotDateString));
//            } while (page * smr.paging.pageSize < smr.paging.total);
//
//        }

        return records;
    }

    public SourceRecord getSourceRecords(Issue issue) {
        Schema schema = GithubSchema.githubIssue;

        System.out.println("beginning");
        System.out.println(issue);
        System.out.println("end");
        Struct struct = new Struct(schema);

        struct.put(GithubSchema.FIELD_GITHUB_ISSUE_CREATED_AT, issue.created_at);
        struct.put(GithubSchema.FIELD_GITHUB_ISSUE_ID, issue.id);
        struct.put(GithubSchema.FIELD_GITHUB_ISSUE_AUTHOR_ASSOCIATION, issue.author_association);
        struct.put(GithubSchema.FIELD_GITHUB_ISSUE_BODY, issue.body);
        struct.put(GithubSchema.FIELD_GITHUB_ISSUE_CLOSED_AT, issue.closed_at);
        struct.put(GithubSchema.FIELD_GITHUB_ISSUE_COMMENTS, issue.comments);
        struct.put(GithubSchema.FIELD_GITHUB_ISSUE_COMMENTS_URL, issue.comments_url);
        struct.put(GithubSchema.FIELD_GITHUB_ISSUE_EVENTS_URL, issue.events_url);
        struct.put(GithubSchema.FIELD_GITHUB_ISSUE_HTML_URL, issue.html_url);
        struct.put(GithubSchema.FIELD_GITHUB_ISSUE_LABELS, issue.labels);
        struct.put(GithubSchema.FIELD_GITHUB_ISSUE_LOCKED, issue.locked);
        struct.put(GithubSchema.FIELD_GITHUB_ISSUE_LABELS_URL, issue.labels_url);
        struct.put(GithubSchema.FIELD_GITHUB_ISSUE_NODE_ID, issue.node_id);
        struct.put(GithubSchema.FIELD_GITHUB_ISSUE_NUMBER, issue.number);
        struct.put(GithubSchema.FIELD_GITHUB_ISSUE_REPOSITORY_URL, issue.repository_url);
        struct.put(GithubSchema.FIELD_GITHUB_ISSUE_URL, issue.url);
        struct.put(GithubSchema.FIELD_GITHUB_ISSUE_TITLE, issue.title);
        struct.put(GithubSchema.FIELD_GITHUB_ISSUE_STATE, issue.state);
        struct.put(GithubSchema.FIELD_GITHUB_ISSUE_UPDATED_AT, issue.updated_at);
        struct.put(GithubSchema.FIELD_GITHUB_ISSUE_CLOSED_AT, issue.closed_at);

        // dummy partition map
        Map<String, String> hm = new HashMap<>();
        hm.put("2", "2");

        return new SourceRecord(hm, hm, githubIssuesTopic, schema, struct);
    }

    @Override
    public void start(Map<String, String> props) {

        log.info("connect-github: start");
        log.info(props.toString());

        githubUser = props.get(GithubSourceConfig.GITHUB_USER_CONFIG);
        githubPass = props.get(GithubSourceConfig.GITHUB_PASS_CONFIG);
        githubIssuesTopic = props.get(GithubSourceConfig.GITHUB_ISSUES_TOPIC_CONFIG);
        githubInterval = props.get(GithubSourceConfig.GITHUB_INTERVAL_SECONDS_CONFIG);
//		githubAPIUrl			= props.get( GithubSourceConfig.GITHUB_)

        if ((githubInterval == null || githubInterval.isEmpty())) {
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
    }
}
