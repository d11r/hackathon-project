package model.github;

public class Issue {
	
	public String url;
	public String repository_url;
	public String labels_url;
	public String comments_url;
	public String events_url;
	public String html_url;
	public String id;
	public String node_id;
	public Long number;
	public String title;
	
	public User user;
	
	public Label[] labels;
	
	public String state;
	public Boolean locked;
	
	public User assignee;
	public User[] assignees;
	
	public Milestone milestone;
	
	public Long comments;
	
	// timestamps, e.g. "2019-10-11T08:24:05Z"
	public String created_at;
	public String updated_at;
	public String closed_at;
	
	public String author_association;
	
	public Repository repository;
	
	public String body;
	
}
