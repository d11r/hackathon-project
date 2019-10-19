package connect.github;

import com.google.gson.Gson;
import model.github.Issue;
import model.github.Repository;
import org.joda.time.Duration;
import org.joda.time.Instant;
import util.RESTInvoker;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GithubApi {

    public GithubApi(String username, String password) {
        this.username = username;
        this.password = password;
    }

    private String username;
    private String password;

    /**
     * According to GitHub API, authenticated users can make up to 5000 requests per hour,
     * which is on average more than 80 requests per minute, thus 1 second throttling is enough.
     */
    private static final Duration THROTTLE_DURATION = new Duration(1000);
    private Instant lastUse = new Instant();

    public Repository getRepository(String repository) {
        throttle();

        RESTInvoker ri = new RESTInvoker(baseRepositoryUrl(repository), username, password);
        String response = ri.getDataFromServer("");
        return new Gson().fromJson(response, Repository.class);
    }

    public List<Issue> getIssues(Repository repository, int page) {
        assert page >= 1;
        try {
            throttle();

            System.out.println(repository);

            String issuesUrl = makeIssuesUrl(repository, page);
            RESTInvoker ri = new RESTInvoker(issuesUrl, username, password);
            String response = ri.getDataFromServer();
            Issue[] issuesArray = new Gson().fromJson(response, Issue[].class);
            List<Issue> issues = Arrays.asList(issuesArray);

            System.out.println(issues);

            return issues;
        } catch (Exception e) {
            System.out.println(e.toString());
            return Collections.emptyList();
        }
    }

    private void throttle() {
        throttle(THROTTLE_DURATION);
    }

    private void throttle(Duration duration) {
        if (secondsSinceLastUse() < 1) {
            System.out.println("Throttling for " + duration.getStandardSeconds() + " seconds");
            try {
                Thread.sleep(duration.getMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lastUse = new Instant();
        }
    }

    private long secondsSinceLastUse() {
        Duration duration = new Duration(lastUse, new Instant());
        return duration.getStandardSeconds();
    }

    // which is maximum GitHub allows us, by the way
    private static final int PER_PAGE = 100;

    String baseRepositoryUrl(String repository) {
        return "https://api.github.com/repos/" + repository;
    }

    String makeIssuesUrl(String repository, int page) {
        return baseRepositoryUrl(repository) + "/issues?page=" + page + "&per_page=" + PER_PAGE;
    }

    String makeIssuesUrl(Repository repository, int page) {
        return makeIssuesUrl(repository.full_name, page);
    }

    public static void main(String[] args) {
        String repository_full_name = "flutter/flutter";
        String username = "marko-brodarski";
        String password = "CQAn57N7j4NzWQp";

        GithubApi githubApi = new GithubApi(username, password);
        Repository repository = githubApi.getRepository(repository_full_name);

        List<Issue> issues = githubApi.getIssues(repository, 1);
    }
}