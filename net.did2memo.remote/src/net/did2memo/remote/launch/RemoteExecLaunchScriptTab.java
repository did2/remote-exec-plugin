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

public class RemoteExecLaunchScriptTab extends AbstractLaunchConfigurationTab {
	protected Button fDefaultScriptTemplateRadioButton;
	protected Button fOriginalScriptTemplateRadioButton;
	protected Text fOriginalScriptTemplateText;

	private WidgetListener fListener = new WidgetListener();

	private class WidgetListener implements ModifyListener, SelectionListener {
		@Override
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			/*do nothing*/
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			updateLaunchConfigurationDialog();
		}
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH);
		((GridLayout) comp.getLayout()).verticalSpacing = 0;

		createScriptTemplateEditor(comp);

		setControl(comp);
	}

	protected void createScriptTemplateEditor(Composite parent) {
		Group group = SWTFactory.createGroup(parent, "Execution script template", 1, 4, GridData.FILL_HORIZONTAL);

		this.fDefaultScriptTemplateRadioButton = createRadioButton(group, "use default script template");
		this.fDefaultScriptTemplateRadioButton.addSelectionListener(this.fListener);
		this.fDefaultScriptTemplateRadioButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RemoteExecLaunchScriptTab.this.fOriginalScriptTemplateText.setEnabled(false);
			}
		});

		this.fOriginalScriptTemplateRadioButton = createRadioButton(group, "use original script template");
		this.fOriginalScriptTemplateRadioButton.addSelectionListener(this.fListener);
		this.fOriginalScriptTemplateRadioButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RemoteExecLaunchScriptTab.this.fOriginalScriptTemplateText.setEnabled(true);
			}
		});

		this.fOriginalScriptTemplateText = new Text(group, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 400;
		gd.widthHint = 100;
		this.fOriginalScriptTemplateText.setLayoutData(gd);
		this.fOriginalScriptTemplateText.setFont(parent.getFont());
		this.fOriginalScriptTemplateText.addModifyListener(this.fListener);

	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		updateScriptTemplateFromConfiguration(configuration);
	}

	private void updateScriptTemplateFromConfiguration(ILaunchConfiguration configuration) {
		int style = IRemoteExecConfigurationConstants.SCRIPT_TEMPLATE_STYLE_DEFAULT;
		try {
			style = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_SCRIPT_TEMPLATE_STYLE, style);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		if (style == IRemoteExecConfigurationConstants.SCRIPT_TEMPLATE_STYLE_DEFAULT) {
			this.fDefaultScriptTemplateRadioButton.setSelection(true);
			this.fOriginalScriptTemplateRadioButton.setSelection(false);
			this.fOriginalScriptTemplateText.setEnabled(false);
			this.fOriginalScriptTemplateText.setText(LaunchScriptTemplate.SCRIPT_TEMPLATE_DEFAULT);
		} else if (style == IRemoteExecConfigurationConstants.SCRIPT_TEMPLATE_STYLE_ORIGINAL) {
			this.fDefaultScriptTemplateRadioButton.setSelection(false);
			this.fOriginalScriptTemplateRadioButton.setSelection(true);
			this.fOriginalScriptTemplateText.setEnabled(true);
			String template = LaunchScriptTemplate.SCRIPT_TEMPLATE_DEFAULT;
			try {
				template = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_SCRIPT_TEMPLATE, template);
			} catch (CoreException ce) {
				setErrorMessage(ce.getStatus().getMessage());
			}
			this.fOriginalScriptTemplateText.setText(template);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		int style;
		if (this.fDefaultScriptTemplateRadioButton.getSelection()) {
			style = IRemoteExecConfigurationConstants.SCRIPT_TEMPLATE_STYLE_DEFAULT;
		} else if (this.fOriginalScriptTemplateRadioButton.getSelection()) {
			style = IRemoteExecConfigurationConstants.SCRIPT_TEMPLATE_STYLE_ORIGINAL;
		} else {
			style = IRemoteExecConfigurationConstants.SCRIPT_TEMPLATE_STYLE_DEFAULT;
		}
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_SCRIPT_TEMPLATE_STYLE, style);

		String template = LaunchScriptTemplate.SCRIPT_TEMPLATE_DEFAULT;
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_SCRIPT_TEMPLATE, this.fOriginalScriptTemplateText.getText());
	}

	@Override
	public String getName() {
		return "Launch Script";
	}

}
