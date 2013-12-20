package net.did2memo.remote.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;

public class RemoteExecLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

	public RemoteExecLaunchConfigurationTabGroup() {

	}

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] { new JavaMainTab(), new RemoteExecMainTab(), new RemoteExecTransferTab(), new RemoteExecLaunchScriptTab(), new JavaArgumentsTab(), new JavaClasspathTab(), new JavaJRETab(), new CommonTab() };
		setTabs(tabs);
	}
}
