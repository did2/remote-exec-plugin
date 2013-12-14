package net.did2memo.remote.launch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.did2memo.remote.Activator;
import net.did2memo.remote.IRemoteExecConfigurationConstants;
import net.did2memo.remote.RemoteExecConsole;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILibrary;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.launching.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsUtil;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.console.PatternMatchListener;
import org.eclipse.ui.internal.console.PatternMatchListenerExtension;

public class RemoteExecLaunchConfiguration extends
	AbstractJavaLaunchConfigurationDelegate {
	
	private ILaunch launch = null;
	
	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		this.launch = launch;
		
		ILaunchConfigurationWorkingCopy copy = configuration.getWorkingCopy();
		String userArgs = copy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
		
		String user = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_USER, "");
		String host = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_HOST, "");
		String port = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_PORT, "22");
		String sshPath = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_SSH, "");
		String scpPath = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_SCP, "");
		
//		this.execExternal("ping 192.168.11.1", getWorkingDirectory(configuration), configuration, launch);
//		this.execExternal("cmd.exe /c cd", getWorkingDirectory(configuration), configuration, launch);
		this.execExternal(new String[] {
			sshPath,
			"-P",
			port,
			"user" + "@" + host,
			"ls"
		}, getWorkingDirectory(configuration), configuration, launch);
		
//		this.execExternal(sshPath + " -P " + port + " " , getWorkingDirectory(configuration), configuration, launch);
//		Process rawProc = DebugPlugin.exec(new String[]{"X:\\freeware\\putty\\putty-0.60-JP_Y-2007-08-06\\pscp.exe", "-v", "-r", "-P", "1002", "src", "ngo@shudo02.is.titech.ac.jp:."}, getWorkingDirectory(configuration));
	}
	
	private int execExternal(String commandLine, File workingDirectory, ILaunchConfiguration configuration, ILaunch launch) throws CoreException {
		return this.execExternal(commandLine.split(" "), workingDirectory, configuration, launch);
	}
	
	private int execExternal(final String[] commandLine, File workingDirectory, ILaunchConfiguration configuration, ILaunch launch) throws CoreException {
		// does not use buffer
		// threads orz
		
//		Process rawProc;
		IProcess proc;
		
		final Process rawProc = null;//DebugPlugin.exec(commandLine, workingDirectory);
		Process rawProc2 = null;
		try {
			ProcessBuilder pb = new ProcessBuilder(commandLine);
			pb.redirectErrorStream(true);
			rawProc2 = pb.start();
//			rawProc2 = Runtime.getRuntime().exec(commandLine);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		final Process rawProc3 = rawProc2;
		final InputStream errin = rawProc2.getErrorStream();
		final InputStream stdin = rawProc2.getInputStream();
		((RemoteExecConsole)this.getConsole()).setRemoteExecOutputStream(rawProc2.getOutputStream());
		
//		InputStream input = new SequenceInputStream(std, err);
//		new PipedInputStream(new PipedOutputStream().
		
		final IOConsoleOutputStream out = this.getOutputStream();
		
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
		final BufferedReader errReader = new BufferedReader(new InputStreamReader(errin));
		Thread errThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				String line = "";
				byte[] bytes = new byte[1];
				try {
					while(errReader.ready()) {
						line = errReader.readLine();
//					while(errin.read(bytes) > 0) {
						RemoteExecLaunchConfiguration.this.getConsole().newOutputStream().write(line.getBytes());
						System.out.println();
//						out.write(bytes);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println();
			}
		});
		errThread.start();
		
		int ret = 1;
		Thread stdThread = new Thread(new Runnable() {
			@Override
			public void run() {
		try {
			out.write("> " + StringUtils.join(commandLine, " ") + "\n");
			String line = "";
			byte[] bytes = new byte[1];
			while(stdin.read(bytes) > 0) {
//				out.write("[RemoteExec] ");
				out.write(bytes);
//				out.write('\n');
				
			}
//			while ((line = reader.readLine()) != null) {
//				out.write("[RemoteExec] " + line + "\n");
//			}
			out.write("\n");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		}});
		stdThread.start();
		
		try {
			rawProc3.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return rawProc3.exitValue();
	}
	
	private IOConsoleOutputStream outputStream = null;
	private IOConsoleOutputStream getOutputStream() {
		if (this.outputStream == null) {
			this.outputStream = this.getConsole().newOutputStream();
		}
		return this.outputStream;
	}
	
	private IStreamListener streamListener = null;
	private IStreamListener getStreamListener() {
		if (this.streamListener == null) {
			final IOConsoleOutputStream out = this.getOutputStream();
			this.streamListener = new IStreamListener() {
				@Override
				public void streamAppended(String text, IStreamMonitor monitor) {
					try {
						out.write(text + "\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
		}
		return this.streamListener;
	}
	
	private IOConsole console = null;
	private IOConsole getConsole() {
		if ( this.console != null)
			return this.console;
		
		final String CONSOLE_NAME = "net.did2memo.remote.console";
		ConsolePlugin consolePlugin = ConsolePlugin.getDefault();
		IConsoleManager consoleManager = consolePlugin.getConsoleManager();
		IConsole[] consoles = consoleManager.getConsoles();
		IOConsole console = null;
		for (IConsole console0 : consoles) {
			if (CONSOLE_NAME.equals(console0.getName())) {
				console = (IOConsole) console0;
				break;
			}
		}
		if (console == null) {
			
			this.console = new RemoteExecConsole(null, null);
			consoleManager.addConsoles(new IConsole[]{this.console});
		}
		return this.console;
	}
}
