package net.did2memo.remote.launch;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.did2memo.remote.IRemoteExecConfigurationConstants;
import net.did2memo.remote.RemoteExecConsole;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMConnector;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsoleOutputStream;

public class RemoteExecLaunchConfiguration extends AbstractJavaLaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// afraid to use "rm" command
		// eclipse console

		boolean attrRemoteDebug = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_REMOTE_DEBUG, false);
		boolean remoteDebug = attrRemoteDebug && "debug".equals(mode);

		String attrRemoteWorkingDirectory = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_REMOTE_WORKING_DIR, ".");
		String attrTunnelingLocalPort = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_TUNNELING_LOCAL_PORT, "61620");
		String attrRemoteDebugPort = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_REMOTE_DEBUG_PORT, "61620");

		String mainClassName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "");
		String programArguments = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "");
		String vmArguments = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""));
		String localRemoteExecDirName = "remote-exec";
		String localRemoteExecDirectory = project.getLocation().toOSString() + File.separator + localRemoteExecDirName;

		String localRootPath = localRemoteExecDirectory;
		String remoteRootParentPath = (attrRemoteWorkingDirectory == "" ? "./remote-exec" : attrRemoteWorkingDirectory);
		String remoteRootPath = remoteRootParentPath + "/" + localRemoteExecDirName;
//		String localClasspathDirPath = localRootPath + File.separator + "classpath";
		String remoteClasspathDirPath = remoteRootPath + "/" + "classpath";

		final String SCRIPT_NAME = "launch-script";
		String localScriptPath = localRootPath + File.separator + SCRIPT_NAME;
		String remoteScriptPath = remoteRootPath + "/" + SCRIPT_NAME;

		ExternalCommand externalCommand = new ExternalCommand(getWorkingDirectory(configuration), this.getConsole(configuration), launch, configuration);

		// prepare external command object
		this.printLaunchInfo("### scp classpath elements ###\n");
		externalCommand.execSsh("mkdir -p " + remoteClasspathDirPath);
		String classpathArg = ".";
		String[] classpaths = getClasspath(configuration);
		for (String classpath : classpaths) {
			File source = new File(classpath);
			if (source.isDirectory()) {
				classpathArg += ":" + remoteClasspathDirPath + "/" + source.getName();
			} else if (source.isFile()) {
				classpathArg += ":" + remoteClasspathDirPath + "/" + source.getName();
			}
		}

		int attrRemoteTransferStrategy = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_TRANSFER_STRATEGY, IRemoteExecConfigurationConstants.PARAMETER_TRANSFER_ALL);
		switch (attrRemoteTransferStrategy) {
		case IRemoteExecConfigurationConstants.PARAMETER_TRANSFER_ALL:
			for (String classpath : classpaths) {
				File source = new File(classpath);
				if (source.isDirectory()) {
					externalCommand.execScp(source.getAbsolutePath(), remoteClasspathDirPath, true);
				} else if (source.isFile()) {
					externalCommand.execScp(source.getAbsolutePath(), remoteClasspathDirPath + "/" + source.getName(), false);
				}
			}
			break;
		case IRemoteExecConfigurationConstants.PARAMETER_TRANSFER_SELECTED:
			List<Object> selectedClasspathList = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_TRANSFER_SELECTED_CLASSPATH_LIST, Arrays.asList(new Object[0]));
			for (Object classpath : selectedClasspathList) {
				File source = new File((String) classpath);
				if (source.isDirectory()) {
					externalCommand.execScp(source.getAbsolutePath(), remoteClasspathDirPath, true);
				} else if (source.isFile()) {
					externalCommand.execScp(source.getAbsolutePath(), remoteClasspathDirPath + "/" + source.getName(), false);
				}
			}
			break;
		default:

		}

		// careate local working folder
		this.printLaunchInfo("### prepare local directories ###\n");
		boolean created = new File(localRootPath).mkdirs();
		if (created) {
			this.printLaunchInfo("created directory : " + localRootPath + "\n");
		}

		// create execute script
		this.printLaunchInfo("### compose script ###\n");
		String script = "#!/bin/sh" + "\n";
		script += "java";
		if (remoteDebug) {
			script += " -agentlib:jdwp=transport=dt_socket,suspend=y,server=y,timeout=10000,address=" + attrRemoteDebugPort;
		}
		script += " " + vmArguments;
		script += " -Dfile.encoding=UTF-8";
		script += " -classpath " + "\"" + classpathArg + "\"";
		script += " " + mainClassName;
		script += " " + programArguments;
		this.printLaunchInfo(script + "\n");

		// save execute script
		this.printLaunchInfo("### save script ###\n");
		this.printLaunchInfo("write to " + localScriptPath);
		try {
			FileUtils.writeStringToFile(new File(localScriptPath), script);
			this.printLaunchInfo("success.\n\n");
		} catch (IOException e) {
			this.printLaunchInfo("fail.\n\n");
			e.printStackTrace();
		}

//		// scp execute script
		this.printLaunchInfo("### scp remote-exec directory ###\n");
		externalCommand.execSsh("mkdir -p " + remoteRootParentPath);
		externalCommand.execScp(localRemoteExecDirectory, remoteRootParentPath, true);

		// chmod +x execute script
		externalCommand.execSsh("chmod +x " + remoteScriptPath);

		// execute via ssh
		String invokeCommand = "/bin/sh " + remoteScriptPath;
		if (!remoteDebug) {
			externalCommand.execSsh(invokeCommand);
			this.printLaunchInfo("### finish remote exec (not debug mode) ###\n");
			return;
		}

		Process invokeCommandProcess = externalCommand.execAsyncSsh(invokeCommand);

		// port forwarding
		this.printLaunchInfo("### begin port forwarding ###\n");
		String attrHost = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_HOST, "");
		Process tunnelingProcess = externalCommand.execAsyncSshTonneling(attrTunnelingLocalPort, attrHost, attrRemoteDebugPort);
		OutputStream tonnelingProcessOutputStream = tunnelingProcess.getOutputStream();

		// launch debug configuration
		// c.f. SocketAttachConnector#connect
		{
			Map<String, String> argMap = new HashMap<>();
			argMap.put("port", attrTunnelingLocalPort);
			try {
				argMap.put("hostname", Inet4Address.getLocalHost().getHostAddress());
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
			int connectTimeout = Platform.getPreferencesService().getInt(LaunchingPlugin.ID_PLUGIN, JavaRuntime.PREF_CONNECT_TIMEOUT, JavaRuntime.DEF_CONNECT_TIMEOUT, null);
			argMap.put("timeout", Integer.toString(connectTimeout));
			try {
				this.launchRemoteDebuggerlaunch(argMap, configuration, mode, launch, monitor);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// finish port forwarding
		this.printLaunchInfo("### end port forwarding ###\n");
		try {
			tonnelingProcessOutputStream.write("exit\n".getBytes());
			tonnelingProcessOutputStream.flush();
			this.printLaunchInfo("disconnected.\n\n");
		} catch (IOException e) {
			e.printStackTrace();
			this.printLaunchInfo("disconnect failure.\n\n");
		}

		this.printLaunchInfo("### finish remote exec (debug mode)###\n");
	}

	private void launchRemoteDebuggerlaunch(Map<String, String> arguments, ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask("Task Name", 3);
		if (monitor.isCanceled()) {
			return;
		}
		try {
			monitor.subTask(LaunchingMessages.JavaRemoteApplicationLaunchConfigurationDelegate_Verifying_launch_attributes____1);

			IVMConnector connector = JavaRuntime.getVMConnector(IJavaLaunchConfigurationConstants.ID_SOCKET_ATTACH_VM_CONNECTOR);

			// check for cancellation
			if (monitor.isCanceled()) {
				return;
			}

			monitor.worked(1);

			monitor.subTask(LaunchingMessages.JavaRemoteApplicationLaunchConfigurationDelegate_Creating_source_locator____2);
			// set the default source locator if required
			setDefaultSourceLocator(launch, configuration);
			monitor.worked(1);

			// connect to remote VM
			connector.connect(arguments, monitor, launch);

			// check for cancellation
			if (monitor.isCanceled()) {
				IDebugTarget[] debugTargets = launch.getDebugTargets();
				for (int i = 0; i < debugTargets.length; i++) {
					IDebugTarget target = debugTargets[i];
					if (target.canDisconnect()) {
						target.disconnect();
					}
				}
				return;
			}
		} finally {
			monitor.done();
		}
	}

	private IOConsoleOutputStream outputStream = null;

	private void printLaunchInfo(String string) {
		if (this.outputStream == null) {
			this.outputStream = this.console.newOutputStream();
		}

		try {
			this.outputStream.write(string);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private RemoteExecConsole console = null;

	private void prepareConsole(ILaunchConfiguration configuration) throws CoreException {
		final String CONSOLE_NAME = "net.did2memo.remote.console";
		ConsolePlugin consolePlugin = ConsolePlugin.getDefault();
		IConsoleManager consoleManager = consolePlugin.getConsoleManager();
		IConsole[] consoles = consoleManager.getConsoles();
		for (IConsole console0 : consoles) {
			if (CONSOLE_NAME.equals(console0.getName())) {
				this.console = (RemoteExecConsole) console0;
				break;
			}
		}
		if (this.console == null) {
			String attrEncoding = configuration.getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, (String) null);
			this.console = new RemoteExecConsole(null, attrEncoding);
			consoleManager.addConsoles(new IConsole[] { this.console });
		}
	}

	private RemoteExecConsole getConsole(ILaunchConfiguration configuration) throws CoreException {
		if (this.console == null)
			this.prepareConsole(configuration);
		return this.console;
	}
}
