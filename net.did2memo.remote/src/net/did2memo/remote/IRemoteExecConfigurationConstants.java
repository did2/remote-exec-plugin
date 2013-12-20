package net.did2memo.remote;

public interface IRemoteExecConfigurationConstants {
	public String CONFIG_ID = "net.did2memo.remote.config";

	public String ATTR_USER = CONFIG_ID + ".user";
	public String ATTR_HOST = CONFIG_ID + ".host";
	public String ATTR_PORT = CONFIG_ID + ".port";
	public String ATTR_REMOTE_WORKING_DIR = CONFIG_ID + ".remote_working_dir";

	public String ATTR_SSH = CONFIG_ID + ".ssh";
	public String ATTR_SCP = CONFIG_ID + ".scp";

	public String ATTR_PARAMETER_TEMPLATE_STYLE = CONFIG_ID + ".parameter_template_style";
	public int PARAMETER_TEMPLATE_PUTTY_STYLE = 1;
	public int PARAMETER_TEMPLATE_UNIX_STYLE = 2;
	public int PARAMETER_TEMPLATE_ORIGINAL_STYLE = 3;
	public String ATTR_SSH_PARAMETER_TEMPLATE = CONFIG_ID + ".ssh_parameter_template";
	public String ATTR_SCP_PARAMETER_TEMPLATE = CONFIG_ID + ".scp_parameter_template";
	public String ATTR_SCP_DIR_PARAMETER_TEMPLATE = CONFIG_ID + ".scp_dir_parameter_template";

	public String ATTR_REMOTE_DEBUG = CONFIG_ID + ".remote_debug";
	public String ATTR_TUNNELING_LOCAL_PORT = CONFIG_ID + ".tunneling_local_port";
	public String ATTR_REMOTE_DEBUG_PORT = CONFIG_ID + ".remote_debug_port";

	public String ATTR_TRANSFER_STRATEGY = CONFIG_ID + ".transfer-strategy";
	public int PARAMETER_TRANSFER_ALL = 1;
	public int PARAMETER_TRANSFER_SELECTED = 2;
	public String ATTR_TRANSFER_SELECTED_CLASSPATH_LIST = CONFIG_ID + ".transfer-selected"; // value tyep is List<String>
}
