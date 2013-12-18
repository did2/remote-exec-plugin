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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

public class RemoteExecMainTab extends AbstractLaunchConfigurationTab {

	// remote host UI widget
	protected Text fUserText;
	protected Text fHostText;
	protected Text fPortText;
	protected Text fRemoteWorkingDirectoryText;

	// path to external command line tools UI widget
	protected Text fSshText;
	protected Text fScpText;

	// command line parameter UI widget
	protected Button fPlinkPscpStyleParameterRadioButton;
//	protected Text fPlinkPresetParameterText;
//	protected Text fPscpPresetParameterText;
	protected Button fSshScpStyleParameterRadioButton;
//	protected Text fSshPresetParameterText;
//	protected Text fScpPresetParameterText;
	protected Button fOriginalStyleParameterRadioButton;
	protected Text fSshParameterText;
	protected Text fScpParameterText;
	protected Text fScpDirParameterText;

	// remote debug UI widget
	protected Button fRemoteDebugCheckButton;
	protected Text fTunnelingLocalPortText;
	protected Text fRemoteDebugPortText;

	protected Text fCommandPlanText;

	private WidgetListener fListener = new WidgetListener();

	private class WidgetListener implements ModifyListener, SelectionListener {
		@Override
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
			updateLocalCommandPlan();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			/*do nothing*/
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

		createCommandParameterTypeEditor(comp);
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

		SWTFactory.createLabel(group, "Host Name or IP Address (e.g. example.net):", 1);
		this.fHostText = SWTFactory.createSingleText(group, 1);
		this.fHostText.addModifyListener(this.fListener);

		SWTFactory.createLabel(group, "SSH Port (e.g. 22):", 1);
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

	protected void createCommandParameterTypeEditor(Composite parent) {
		Group group = SWTFactory.createGroup(parent, "Parameter Template", 1, 5, GridData.FILL_HORIZONTAL);

		this.fPlinkPscpStyleParameterRadioButton = createRadioButton(group, "plink, pscp style (for Win)");
//		Composite comp1 = SWTFactory.createComposite(group, 2, 1, GridData.FILL_HORIZONTAL);
		this.fPlinkPscpStyleParameterRadioButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RemoteExecMainTab.this.fSshParameterText.setEnabled(false);
				RemoteExecMainTab.this.fScpParameterText.setEnabled(false);
				RemoteExecMainTab.this.fScpDirParameterText.setEnabled(false);

				RemoteExecMainTab.this.fSshParameterText.setText(CommandParameterTemplate.PLINK_DEFAULT);
				RemoteExecMainTab.this.fScpParameterText.setText(CommandParameterTemplate.PSCP_DEFAULT);
				RemoteExecMainTab.this.fScpDirParameterText.setText(CommandParameterTemplate.PSCP_DIR_DEFAULT);
			}
		});

		this.fSshScpStyleParameterRadioButton = createRadioButton(group, "ssh, scp style (for Mac/Linux)");
//		Composite comp2 = SWTFactory.createComposite(group, 2, 1, GridData.FILL_HORIZONTAL);
		this.fSshScpStyleParameterRadioButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RemoteExecMainTab.this.fSshParameterText.setEnabled(false);
				RemoteExecMainTab.this.fScpParameterText.setEnabled(false);
				RemoteExecMainTab.this.fScpDirParameterText.setEnabled(false);

				RemoteExecMainTab.this.fSshParameterText.setText(CommandParameterTemplate.SSH_DEFAULT);
				RemoteExecMainTab.this.fScpParameterText.setText(CommandParameterTemplate.SCP_DEFAULT);
				RemoteExecMainTab.this.fScpDirParameterText.setText(CommandParameterTemplate.SCP_DIR_DEFAULT);
			}
		});

		this.fOriginalStyleParameterRadioButton = createRadioButton(group, "original parameter style");
		this.fOriginalStyleParameterRadioButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RemoteExecMainTab.this.fSshParameterText.setEnabled(true);
				RemoteExecMainTab.this.fScpParameterText.setEnabled(true);
				RemoteExecMainTab.this.fScpDirParameterText.setEnabled(true);
			}
		});

		Composite originalComp = SWTFactory.createComposite(group, 2, 2, GridData.FILL_HORIZONTAL);
		SWTFactory.createLabel(originalComp, "ssh parameters", 1);
		this.fSshParameterText = SWTFactory.createSingleText(originalComp, 1);
		SWTFactory.createLabel(originalComp, "scp parameters", 1);
		this.fScpParameterText = SWTFactory.createSingleText(originalComp, 1);
		SWTFactory.createLabel(originalComp, "scp(directory) parameters", 1);
		this.fScpDirParameterText = SWTFactory.createSingleText(originalComp, 1);
	}

	protected void createTunnelingConfigEditor(Composite parent) {
		Group group = SWTFactory.createGroup(parent, "Remote Debug", 1, 3, GridData.FILL_HORIZONTAL);

		this.fRemoteDebugCheckButton = SWTFactory.createCheckButton(group, "Enalbe Remote Debug (Experimental)", null, false, 1);
		this.fRemoteDebugCheckButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();

				boolean enabled = RemoteExecMainTab.this.fRemoteDebugCheckButton.getSelection();
				RemoteExecMainTab.this.fTunnelingLocalPortText.setEnabled(enabled);
				RemoteExecMainTab.this.fRemoteDebugPortText.setEnabled(enabled);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Composite confGroup = SWTFactory.createComposite(group, 2, 2, GridData.FILL_HORIZONTAL);

		SWTFactory.createLabel(confGroup, "Local Port (e.g. 61620):", 1);
		this.fTunnelingLocalPortText = SWTFactory.createSingleText(confGroup, 1);
		this.fTunnelingLocalPortText.addModifyListener(this.fListener);

		SWTFactory.createLabel(confGroup, "Remote Debug Port (e.g. 61620):", 1);
		this.fRemoteDebugPortText = SWTFactory.createSingleText(confGroup, 1);
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

		group.setVisible(false);
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
		updateParameterTemplateConfiguration(configuration);
		updateRemoteDebugCheckButton(configuration);
		updateTunnelingLocalPortText(configuration);
		updateRemoteDebugPortText(configuration);

		updateLocalCommandPlan();

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

	private void updateParameterTemplateConfiguration(ILaunchConfiguration configuration) {
		int style = IRemoteExecConfigurationConstants.PARAMETER_TEMPLATE_PUTTY_STYLE;
		String sshParameter = CommandParameterTemplate.PLINK_DEFAULT;
		String scpParameter = CommandParameterTemplate.PSCP_DEFAULT;
		String scpDirParameter = CommandParameterTemplate.PSCP_DIR_DEFAULT;
		try {
			style = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_PARAMETER_TEMPLATE_STYLE, style);
			sshParameter = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_SSH_PARAMETER_TEMPLATE, sshParameter);
			scpParameter = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_SCP_PARAMETER_TEMPLATE, scpParameter);
			scpDirParameter = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_SCP_DIR_PARAMETER_TEMPLATE, scpDirParameter);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}

		switch (style) {
		case IRemoteExecConfigurationConstants.PARAMETER_TEMPLATE_PUTTY_STYLE:
			this.fPlinkPscpStyleParameterRadioButton.setSelection(true);
			this.setOriginalParameterTextEnabled(false);
			break;
		case IRemoteExecConfigurationConstants.PARAMETER_TEMPLATE_UNIX_STYLE:
			this.fSshScpStyleParameterRadioButton.setSelection(true);
			this.setOriginalParameterTextEnabled(false);
			break;
		default:
			this.fOriginalStyleParameterRadioButton.setSelection(true);
			this.setOriginalParameterTextEnabled(true);
			break;
		}
		this.fSshParameterText.setText(sshParameter);
		this.fScpParameterText.setText(scpParameter);
		this.fScpDirParameterText.setText(scpDirParameter);
	}

	private void updateRemoteDebugCheckButton(ILaunchConfiguration configuration) {
		boolean enableRemoteDebug = false;
		final boolean DEFAULT = false;
		try {
			enableRemoteDebug = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_REMOTE_DEBUG, DEFAULT);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		this.fRemoteDebugCheckButton.setSelection(enableRemoteDebug);
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

		this.fTunnelingLocalPortText.setEnabled(this.fRemoteDebugCheckButton.getSelection());
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

		this.fRemoteDebugPortText.setEnabled(this.fRemoteDebugCheckButton.getSelection());
	}

	private void updateLocalCommandPlan() {
		this.fCommandPlanText.setText(this.fScpText.getText() + " -P " + this.fPortText.getText() + " ");
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_USER, this.fUserText.getText().trim());
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_HOST, this.fHostText.getText().trim());
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_PORT, this.fPortText.getText().trim());
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_REMOTE_WORKING_DIR, this.fRemoteWorkingDirectoryText.getText().trim());

		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_SSH, this.fSshText.getText().trim());
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_SCP, this.fScpText.getText().trim());

		int style;
		if (this.fPlinkPscpStyleParameterRadioButton.getSelection())
			style = IRemoteExecConfigurationConstants.PARAMETER_TEMPLATE_PUTTY_STYLE;
		else if (this.fSshScpStyleParameterRadioButton.getSelection())
			style = IRemoteExecConfigurationConstants.PARAMETER_TEMPLATE_UNIX_STYLE;
		else
			style = IRemoteExecConfigurationConstants.PARAMETER_TEMPLATE_ORIGINAL_STYLE;
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_PARAMETER_TEMPLATE_STYLE, style);
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_SSH_PARAMETER_TEMPLATE, this.fSshParameterText.getText());
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_SCP_PARAMETER_TEMPLATE, this.fScpParameterText.getText());
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_SCP_DIR_PARAMETER_TEMPLATE, this.fScpDirParameterText.getText());

		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_REMOTE_DEBUG, this.fRemoteDebugCheckButton.getSelection());
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_TUNNELING_LOCAL_PORT, this.fTunnelingLocalPortText.getText().trim());
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_REMOTE_DEBUG_PORT, this.fRemoteDebugPortText.getText().trim());
	}

	@Override
	public String getName() {
		return "Remote Execution";
	}

	private void setOriginalParameterTextEnabled(boolean enabled) {
		RemoteExecMainTab.this.fSshParameterText.setEnabled(enabled);
		RemoteExecMainTab.this.fScpParameterText.setEnabled(enabled);
		RemoteExecMainTab.this.fScpDirParameterText.setEnabled(enabled);
	}
}
