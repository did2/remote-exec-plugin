package net.did2memo.remote.launch;

import net.did2memo.remote.IRemoteExecConfigurationConstants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

public class RemoteExecMainTab extends AbstractLaunchConfigurationTab {

	protected Text fUserText;
	protected Text fHostText;
	protected Text fPortText;
	protected Text fRemoteWorkingDirectoryText;

	protected Text fSshText;
	protected Text fScpText;

	protected Text fTunnelingLocalPortText;
	protected Text fRemoteDebugPortText;

	protected Text fCommandPlanText;

	private WidgetListener fListener = new WidgetListener();

	private class WidgetListener implements ModifyListener, SelectionListener {
		@Override
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {/*do nothing*/
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
		}
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH);
		((GridLayout) comp.getLayout()).verticalSpacing = 0;

		createRemoteHostEditor(comp);
		SWTFactory.createVerticalSpacer(comp, 5);

		createExternalSshProgramEditor(comp);
		SWTFactory.createVerticalSpacer(comp, 5);

		createTunnelingConfigEditor(comp);
		SWTFactory.createVerticalSpacer(comp, 5);

		createCommandPlanDisplay(comp);

		setControl(comp);
	}

	protected void createRemoteHostEditor(Composite parent) {
		Group group = SWTFactory.createGroup(parent, "Remote Host", 2, 4, GridData.FILL_HORIZONTAL);

		SWTFactory.createLabel(group, "Login User Name:", 1);
		this.fUserText = SWTFactory.createSingleText(group, 1);
		this.fUserText.addModifyListener(this.fListener);

		SWTFactory.createLabel(group, "Host Name or IP Address:", 1);
		this.fHostText = SWTFactory.createSingleText(group, 1);
		this.fHostText.addModifyListener(this.fListener);

		SWTFactory.createLabel(group, "Port:", 1);
		this.fPortText = SWTFactory.createSingleText(group, 1);
		this.fPortText.addModifyListener(this.fListener);

		SWTFactory.createLabel(group, "Working Directory (e.g. /home/user/remote-exec):", 1);
		this.fRemoteWorkingDirectoryText = SWTFactory.createSingleText(group, 1);
		this.fRemoteWorkingDirectoryText.addModifyListener(this.fListener);

	}

	protected void createExternalSshProgramEditor(Composite parent) {
		Group group = SWTFactory.createGroup(parent, "Path to External SSH Command Line Tools", 2, 2, GridData.FILL_HORIZONTAL);

		SWTFactory.createLabel(group, "SSH (e.g. plink.exe):", 1);
		this.fSshText = SWTFactory.createSingleText(group, 1);
		this.fSshText.addModifyListener(this.fListener);

		SWTFactory.createLabel(group, "SCP (e.g. pscp.exe):", 1);
		this.fScpText = SWTFactory.createSingleText(group, 1);
		this.fScpText.addModifyListener(this.fListener);
	}

	protected void createTunnelingConfigEditor(Composite parent) {
		Group group = SWTFactory.createGroup(parent, "SSH Tunneling to Remote JVM", 2, 2, GridData.FILL_HORIZONTAL);

		SWTFactory.createLabel(group, "Local Port (e.g. 61620):", 1);
		this.fTunnelingLocalPortText = SWTFactory.createSingleText(group, 1);
		this.fTunnelingLocalPortText.addModifyListener(this.fListener);

		SWTFactory.createLabel(group, "Remote Debug Port (e.g. 61620):", 1);
		this.fRemoteDebugPortText = SWTFactory.createSingleText(group, 1);
		this.fRemoteDebugPortText.addModifyListener(this.fListener);
	}

	protected void createCommandPlanDisplay(Composite parent) {
		Group group = SWTFactory.createGroup(parent, "Local Command Plan", 1, 1, GridData.FILL_BOTH);

		this.fCommandPlanText = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		this.fCommandPlanText.setEditable(false);

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 30;
		gd.widthHint = 100;
		this.fCommandPlanText.setLayoutData(gd);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		updateUserFromConfiguration(configuration);
		updateHostFromConfiguration(configuration);
		updatePortFromConfiguration(configuration);
		updateRemoteWorkingDirectoryFromConfiguration(configuration);
		updateSshFromConfiguration(configuration);
		updateScpFromConfiguration(configuration);
		updateTunnelingLocalPortText(configuration);
		updateRemoteDebugPortText(configuration);

		this.fCommandPlanText.setText(this.fScpText.getText() + " -P " + this.fPortText.getText() + " ");
	}

	private void updateUserFromConfiguration(ILaunchConfiguration configuration) {
		String user = "";
		String DEFAULT = "root";
		try {
			user = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_USER, DEFAULT);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		this.fUserText.setText(user);
	}

	private void updateHostFromConfiguration(ILaunchConfiguration configuration) {
		String host = "";
		String DEFAULT = "";
		try {
			host = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_HOST, DEFAULT);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		this.fHostText.setText(host);
	}

	private void updatePortFromConfiguration(ILaunchConfiguration configuration) {
		String port = "";
		final String DEFAULT = "22";
		try {
			port = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_PORT, DEFAULT);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		this.fPortText.setText(port);
	}

	private void updateRemoteWorkingDirectoryFromConfiguration(ILaunchConfiguration configuration) {
		String remoteWorkingDirectory = "";
		final String DEFAULT = "~/remote-exec";
		try {
			remoteWorkingDirectory = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_REMOTE_WORKING_DIR, DEFAULT);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		this.fRemoteWorkingDirectoryText.setText(remoteWorkingDirectory);
	}

	private void updateSshFromConfiguration(ILaunchConfiguration configuration) {
		String ssh = "";
		final String DEFAULT = "path\\to\\plink.exe";
		try {
			ssh = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_SSH, DEFAULT);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		this.fSshText.setText(ssh);
	}

	private void updateScpFromConfiguration(ILaunchConfiguration configuration) {
		String scp = "";
		final String DEFAULT = "path\\to\\pscp.exe";
		try {
			scp = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_SCP, DEFAULT);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		this.fScpText.setText(scp);
	}

	private void updateTunnelingLocalPortText(ILaunchConfiguration configuration) {
		String tunnelingLocalPort = "";
		final String DEFAULT = "61620";
		try {
			tunnelingLocalPort = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_TUNNELING_LOCAL_PORT, DEFAULT);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		this.fTunnelingLocalPortText.setText(tunnelingLocalPort);
	}

	private void updateRemoteDebugPortText(ILaunchConfiguration configuration) {
		String remoteDebugPort = "";
		final String DEFAULT = "61620";
		try {
			remoteDebugPort = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_TUNNELING_LOCAL_PORT, DEFAULT);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		this.fRemoteDebugPortText.setText(remoteDebugPort);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_USER, this.fUserText.getText().trim());
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_HOST, this.fHostText.getText().trim());
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_PORT, this.fPortText.getText().trim());
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_REMOTE_WORKING_DIR, this.fRemoteWorkingDirectoryText.getText().trim());
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_SSH, this.fSshText.getText().trim());
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_SCP, this.fScpText.getText().trim());
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_TUNNELING_LOCAL_PORT, this.fTunnelingLocalPortText.getText().trim());
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_REMOTE_DEBUG_PORT, this.fRemoteDebugPortText.getText().trim());
	}

	@Override
	public String getName() {
		return "[R] Remote Exec";
	}

}
