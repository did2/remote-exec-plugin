package net.did2memo.remote.launch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.did2memo.remote.ConsoleOutputType;
import net.did2memo.remote.IRemoteExecConfigurationConstants;
import net.did2memo.remote.RemoteExecConsole;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ui.console.IOConsoleOutputStream;

public class ExternalCommand {
	private final File workingDirectory;
	private final IOConsoleOutputStream consoleOut;
	private final RemoteExecConsole console;
	private final ILaunch launch;
	private final ILaunchConfiguration configuration;

	private String sshPath = "";
	private String scpPath = "";
	private String port = "";
	private String user = "";
	private String host = "";

	private final CommandParameterTemplate template;

	public ExternalCommand(File workingDirectory, RemoteExecConsole console, ILaunch launch, ILaunchConfiguration configuration) throws CoreException {
		this.sshPath = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_SSH, "");
		this.scpPath = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_SCP, "");
		this.user = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_USER, "");
		this.host = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_HOST, "");
		this.port = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_PORT, "22");

		this.workingDirectory = workingDirectory;
		this.console = console;
		this.consoleOut = console.newOutputStream();
		this.launch = launch;
		this.configuration = configuration;

		String sshParameterTemplate = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_SSH_PARAMETER_TEMPLATE, "");
		String scpParameterTemplate = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_SCP_PARAMETER_TEMPLATE, "");
		String scpDirParameterTemplate = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_SCP_DIR_PARAMETER_TEMPLATE, "");
		this.template = new CommandParameterTemplate(sshParameterTemplate, scpParameterTemplate, scpDirParameterTemplate);

	}

	public int execSsh(String remoteCommandLine) throws CoreException {
		String[] parameter = this.template.generateSshParameter(this.user, this.host, this.port, remoteCommandLine);
		String[] commandLine = ArrayUtils.addAll(new String[] { this.sshPath }, parameter);
		return this.exec(commandLine);
	}

	public Process execAsyncSsh(String remoteCommandLine) throws CoreException {
		String[] parameter = this.template.generateSshParameter(this.user, this.host, this.port, remoteCommandLine);
		String[] commandLine = ArrayUtils.addAll(new String[] { this.sshPath }, parameter);
		return this.exec0(commandLine);
	}

	public int execScp(String localPath, String remotePath, boolean directory) throws CoreException {
		String[] parameter;
		String[] commandLine;
		if (directory) {
			parameter = this.template.generateScpDirCommandLine(this.user, this.host, this.port, localPath, remotePath);
//			commandLine = new String[] { this.scpPath, "-r", "-P", this.port, localPath, this.user + "@" + this.host + ":" + remotePath };
		} else {
			parameter = this.template.generateScpCommandLine(this.user, this.host, this.port, localPath, remotePath);
//			commandLine = new String[] { this.scpPath, "-P", this.port, localPath, this.user + "@" + this.host + ":" + remotePath };
		}
		commandLine = ArrayUtils.addAll(new String[] { this.scpPath }, parameter);
		return this.exec(commandLine);
	}

	public Process execAsyncSshTonneling(String localPort, String remoteHost, String remoteHostPort) throws CoreException {
		String[] commandLine = new String[] { this.sshPath, "-P", this.port, "-L", localPort + ":" + this.host + ":" + remoteHostPort, this.user + "@" + this.host };
		return this.exec0(commandLine);
	}

	public int exec(final String[] commandLine) throws CoreException {
		Process process = this.exec0(commandLine);

		try {
			process.waitFor();
		} catch (InterruptedException e) {
			return 3;
		}

		return process.exitValue();
	}

	private Process exec0(final String[] commandLine) throws CoreException {
		final Process process = DebugPlugin.exec(commandLine, this.workingDirectory);
		final InputStream errin = process.getErrorStream();
		final InputStream stdin = process.getInputStream();

		this.console.setRemoteExecOutputStream(process.getOutputStream());

//		final IOConsoleOutputStream commandErrorOutputStream = this.console.newOutputStream();
//		commandErrorOutputStream.setColor(new Color(Display.getDefault(), new RGB(255, 70, 7)));
		final IOConsoleOutputStream commandErrorOutputStream = this.console.getConsoleOutputStream(ConsoleOutputType.COMMAND_ERR_OUT);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
		final BufferedReader errReader = new BufferedReader(new InputStreamReader(errin));
		Thread errThread = new Thread(new Runnable() {
			@Override
			public void run() {
				String line = "";
				try {
					while ((line = errReader.readLine()) != null) {
						commandErrorOutputStream.write(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		errThread.start();

//		final IOConsoleOutputStream commandOutputStream = this.console.newOutputStream();
//		commandOutputStream.setColor(new Color(Display.getDefault(), new RGB(200, 140, 7)));
		final IOConsoleOutputStream commandOutputStream = this.console.getConsoleOutputStream(ConsoleOutputType.COMMAND);
//		final IOConsoleOutputStream commandResultOutputStream = this.console.newOutputStream();
//		commandResultOutputStream.setColor(new Color(Display.getDefault(), new RGB(76, 121, 37)));
		final IOConsoleOutputStream commandResultOutputStream = this.console.getConsoleOutputStream(ConsoleOutputType.COMMAND_STD_OUT);
		Thread stdThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					commandOutputStream.write("[ExternalCommand] > " + StringUtils.join(commandLine, " ") + "\n");
					commandOutputStream.flush();

					byte[] bytes = new byte[1];
					while (stdin.read(bytes) > 0) {
						if (bytes[0] == '\r') {
							commandResultOutputStream.flush();
							continue;
						}
						commandResultOutputStream.write(bytes);
					}
					commandResultOutputStream.write("\n");
					commandResultOutputStream.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		stdThread.start();

		return process;
	}
}
