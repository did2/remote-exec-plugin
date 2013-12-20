package net.did2memo.remote.launch;

import java.util.regex.Matcher;

public class LaunchScriptTemplate {
	public static final String SCRIPT_TEMPLATE_DEFAULT = "#!/bin/sh" + "\n" +
			"java" +
			//			" -agentlib:jdwp=transport=dt_socket,suspend=y,server=y,timeout=10000,address=$remote_debug_port" +
//			" -agentlib:jdwp=transport=dt_socket,suspend=n,server=y,timeout=10000,address=$remote_debug_port" +
			" $vm_args" +
			" -Dfile.encoding=UTF-8" +
			" -classpath \"$classpath\"" +
			" $main_class" +
			" $program_args";

	public static final String VAR_NAME_REMOTE_DEBUG_PORT = "\\$remote_debug_port";
	public static final String VAR_NAME_REMOTE_DEBUG_OPTION = "\\$remote_debug_option";
	public static final String VAR_NAME_VM_ARGS = "\\$vm_args";
	public static final String VAR_NAME_CLASSPATH = "\\$classpath";
	public static final String VAR_NAME_MAIN_CLASS = "\\$main_class";
	public static final String VAR_NAME_PROGRAM_ARGS = "\\$program_args";

	private final String template;
	private String script;

	public LaunchScriptTemplate(String template) {
		this.template = template;
		this.script = template;
	}

	public void replace(String varName, String varValue) {
		this.script = this.script.replaceAll(varName, Matcher.quoteReplacement(varValue));
	}

	public String getScript() {
		return this.script;
	}

	public boolean containsVarPrefix() {
		return this.script.contains("$");
	}
}
