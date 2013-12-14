package net.did2memo.remote.launch;

import net.did2memo.remote.IRemoteExecConfigurationConstants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

public class RemoteExecMainTab extends AbstractLaunchConfigurationTab {
	
	protected Text fUserText;
	protected Text fHostText;
	protected Text fPortText;
//	private Button fHostButton;
	
	protected Text fSshText;
	protected Text fScpText;
	
	private WidgetListener fListener = new WidgetListener();
	private class WidgetListener implements ModifyListener, SelectionListener {
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}
		
		public void widgetDefaultSelected(SelectionEvent e) {/*do nothing*/}
		
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
		}
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH);
		((GridLayout)comp.getLayout()).verticalSpacing = 0;
		createRemoteHostEditor(comp);
		SWTFactory.createVerticalSpacer(comp, 5);
		createExternalSshProgramEditor(comp);
		
		setControl(comp);
	}
	
	protected void createRemoteHostEditor(Composite parent) {
		Group group = SWTFactory.createGroup(parent, "Remote Host", 2, 3, GridData.FILL_HORIZONTAL);
		
		SWTFactory.createLabel(group, "Login User Name:", 1);
		fUserText = SWTFactory.createSingleText(group, 1);
		fUserText.addModifyListener(fListener);
		
		SWTFactory.createLabel(group, "Host Name or IP Address:", 1);
		fHostText = SWTFactory.createSingleText(group, 1);
		fHostText.addModifyListener(fListener);
		
		SWTFactory.createLabel(group, "Port:", 1);
		fPortText = SWTFactory.createSingleText(group, 1);
		fPortText.addModifyListener(fListener);
		
//		fHostText.addModifyListener(fListener);
//		ControlAccessibleListener.addListener(fProjText, group.getText());
//		fProjButton = createPushButton(group, LauncherMessages.AbstractJavaMainTab_1, null); 
//		fProjButton.addSelectionListener(fListener);
	}
	protected void createExternalSshProgramEditor(Composite parent) {
		Group group = SWTFactory.createGroup(parent, "Path to External SSH Command Line Tools", 2, 2, GridData.FILL_HORIZONTAL);
		
		SWTFactory.createLabel(group, "SSH (e.g. plink.exe):", 1);
		fSshText = SWTFactory.createSingleText(group, 1);
		fSshText.addModifyListener(fListener);
		
		SWTFactory.createLabel(group, "SCP (e.g. pscp.exe):", 1);
		fScpText = SWTFactory.createSingleText(group, 1);
		fScpText.addModifyListener(fListener);
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
		updateSshFromConfiguration(configuration);
		updateScpFromConfiguration(configuration);
	}
	
	private void updateUserFromConfiguration(ILaunchConfiguration configuration) {
		String user = "";
		String DEFAULT = "root";
		try {
			user = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_USER, DEFAULT);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		fUserText.setText(user);
	}
	
	private void updateHostFromConfiguration(ILaunchConfiguration configuration) {
		String host = "";
		String DEFAULT = "";
		try {
			host = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_HOST, DEFAULT);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		fHostText.setText(host);
	}
	
	private void updatePortFromConfiguration(ILaunchConfiguration configuration) {
		String port = "";
		final String DEFAULT = "22";
		try {
			port = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_PORT, DEFAULT);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		fPortText.setText(port);
	}
	
	private void updateSshFromConfiguration(ILaunchConfiguration configuration) {
		String ssh = "";
		final String DEFAULT = "path\\to\\plink.exe";
		try {
			ssh = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_SSH, DEFAULT);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		fSshText.setText(ssh);
	}
	
	private void updateScpFromConfiguration(ILaunchConfiguration configuration) {
		String scp = "";
		final String DEFAULT = "path\\to\\pscp.exe";
		try {
			scp = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_SCP, DEFAULT);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		fScpText.setText(scp);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_USER, fUserText.getText().trim());
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_HOST, fHostText.getText().trim());
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_PORT, fPortText.getText().trim());
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_SSH, fSshText.getText().trim());
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_SCP, fScpText.getText().trim());
	}

	@Override
	public String getName() {
		return "[R] Remote Exec";
	}

}
