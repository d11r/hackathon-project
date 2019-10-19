/* Copyright (C) 2019 Fraunhofer IESE
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package connect.github;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

/**
 * Kafka Sonarqube Connector
 * @author Axel Wickenkamp
 *
 */
public class GithubSourceConnector extends SourceConnector {
	
	private Logger log = Logger.getLogger(GithubSourceConnector.class.getName());
	
	private String githubUser;
	private String githubPass;	
	private String githubIssuesTopic;
	private String githubInterval;


	@Override
	public String version() {
		return "0.0.1";
	}

	@Override
	public void start(Map<String, String> props) {
		
		log.info(props.toString());
		
		githubUser = props.get( GithubSourceConfig.GITHUB_USER_CONFIG );
		githubPass = props.get( GithubSourceConfig.GITHUB_PASS_CONFIG );
		githubIssuesTopic = props.get( GithubSourceConfig.GITHUB_ISSUES_TOPIC_CONFIG );
		githubInterval = props.get( GithubSourceConfig.GITHUB_INTERVAL_SECONDS_CONFIG );

	}

	@Override
	public Class<? extends Task> taskClass() {
		return GithubSourceTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {
		  ArrayList<Map<String, String>> configs = new ArrayList<>();
		  
		  Map<String, String> config = new HashMap<>();
		  
		  config.put( GithubSourceConfig.GITHUB_USER_CONFIG, githubUser );
		  config.put( GithubSourceConfig.GITHUB_PASS_CONFIG, githubPass );
		  config.put( GithubSourceConfig.GITHUB_ISSUES_TOPIC_CONFIG, githubIssuesTopic );
		  config.put( GithubSourceConfig.GITHUB_INTERVAL_SECONDS_CONFIG, "" + githubInterval);

		  configs.add(config);
		  return configs;
	}

	@Override
	public void stop() {
		// Nothing to do since Connector has no background monitoring.
	}

	@Override
	public ConfigDef config() {
		return GithubSourceConfig.DEFS;
	}

}
