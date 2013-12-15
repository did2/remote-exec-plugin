package net.did2memo.remote;

public interface IRemoteExecConfigurationConstants {
	public String CONFIG_ID = "net.did2memo.remote.config";
	public String ATTR_USER = CONFIG_ID + ".user";
	public String ATTR_HOST = CONFIG_ID + ".host";
	public String ATTR_PORT = CONFIG_ID + ".port";
	public String ATTR_REMOTE_WORKING_DIR = CONFIG_ID + ".remote_working_dir";
	public String ATTR_SSH = CONFIG_ID + ".ssh";
	public String ATTR_SCP = CONFIG_ID + ".scp";
	public String ATTR_TUNNELING_LOCAL_PORT = CONFIG_ID + ".tunneling_local_port";
	public String ATTR_REMOTE_DEBUG_PORT = CONFIG_ID + ".tunneling_local_port";
}
