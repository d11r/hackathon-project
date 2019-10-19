package connect.github;

import com.google.gson.Gson;
import model.github.Issue;
import model.github.Repository;
import util.RESTInvoker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GithubApi {
	public static Issue[] getIssues( String ghUrl, String username, String password ) {
		// get amount of issues
		String repoURL = "https://api.github.com/repos/flutter/flutter";
		RESTInvoker ri = new RESTInvoker(repoURL, username, password);
		Gson gson = new Gson();

		List<Issue> allIssues = new ArrayList<Issue>();

		try {
			Repository repository = gson.fromJson(ri.getDataFromServer(""), Repository.class);
			System.out.println(repository);
			Long open_issues = repository.open_issues_count;
			Long noOfPaginations = open_issues / 100;

			String templ = "/issues?page=";
			for (long i = 0; i < noOfPaginations + 1; i++) {
				System.out.println(i);
				Issue[] is1=gson.fromJson(ri.getDataFromServer(templ + i), Issue[].class);
				allIssues.addAll(Arrays.asList(is1));
			}

			System.out.println(allIssues);

		} catch (Exception e){
			System.out.println(e.toString());
			return null;
		}

		return (Issue[]) allIssues.toArray();
	}

	public static void main(String[] args) {
		String URL = "https://api.github.com/repos/flutter/flutter/issues";
		Issue[] issues = getIssues(URL, "marko-brodarski", "CQAn57N7j4NzWQp");
	}
}