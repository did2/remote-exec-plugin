package net.did2memo.remote.launch;

import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

public class CommandParameterTemplate {
	public static final String PLINK_DEFAULT = "-P $port $user@$host $command";
	public static final String PSCP_DEFAULT = "-P $port $src $user@$host:$dest";
	public static final String PSCP_DIR_DEFAULT = "-r -P $port $src $user@$host:$dest";
	public static final String SSH_DEFAULT = "-p $port $user@$host $command";
	public static final String SCP_DEFAULT = "-P $port $src $user@$host:$dest";
	public static final String SCP_DIR_DEFAULT = "-r -P $port $src $user@$host:$dest";

	public static final String VAR_NAME_USER = "\\$user";
	public static final String VAR_NAME_HOST = "\\$host";
	public static final String VAR_NAME_PORT = "\\$port";
	public static final String VAR_NAME_SCP_SRC = "\\$src";
	public static final String VAR_NAME_SCP_DEST = "\\$dest";
	public static final String VAR_NAME_COMMAND = "\\$command";

	public final String sshTemplate;
	public final String scpTemplate;
	public final String scpDirTemplate;

	public CommandParameterTemplate(String sshTemplate, String scpTemplate, String scpDirTemplate) {
		this.sshTemplate = sshTemplate;
		this.scpTemplate = scpTemplate;
		this.scpDirTemplate = scpDirTemplate;
	}

	public String[] generateSshParameter(String user, String host, String port, String command) {
		String parameters = replaceAll(this.sshTemplate, user, host, port, "", "", command).trim();
		return StringUtils.split(parameters, ' ');
	}

	public String[] generateScpCommandLine(String user, String host, String port, String src, String dest) {
		String parameters = replaceAll(this.scpTemplate, user, host, port, src, dest, "").trim();
		return StringUtils.split(parameters, ' ');
	}

	public String[] generateScpDirCommandLine(String user, String host, String port, String src, String dest) {
		String parameters = replaceAll(this.scpDirTemplate, user, host, port, src, dest, "").trim();
		return StringUtils.split(parameters, ' ');
	}

	private static String replaceAll(String template, String user, String host, String port, String src, String dest, String command) {
		String param = template;
		param = param.replaceAll(VAR_NAME_USER, Matcher.quoteReplacement(user));
		param = param.replaceAll(VAR_NAME_HOST, Matcher.quoteReplacement(host));
		param = param.replaceAll(VAR_NAME_PORT, Matcher.quoteReplacement(port));
		param = param.replaceAll(VAR_NAME_SCP_SRC, Matcher.quoteReplacement(src));
		param = param.replaceAll(VAR_NAME_SCP_DEST, Matcher.quoteReplacement(dest));
		param = param.replaceAll(VAR_NAME_COMMAND, Matcher.quoteReplacement(command));
		return param;
	}
}
