package net.did2memo.remote.launch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.did2memo.remote.IRemoteExecConfigurationConstants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.pde.internal.ui.elements.DefaultContentProvider;
import org.eclipse.pde.internal.ui.util.SharedLabelProvider;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

public class RemoteExecTransferTab extends AbstractLaunchConfigurationTab {
	// c.f. org.eclipse.pde.internal.ui.launcher.AbstractPluginBlock

	protected Button fRefreshRemoteWorkingDirectoryRadioButton;
	protected Button fPartialTransferRadioButton;
	protected FilteredTree fClasspathFilteredTree;
	protected CheckboxTreeViewer fClasspathTreeViewer;

	protected String[] fClasspaths;

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

		createRemoteTransferStrategyEditor(comp);

		setControl(comp);
	}

	protected void createRemoteTransferStrategyEditor(Composite parent) {
		Group group = SWTFactory.createGroup(parent, "Transfer Detail", 1, 4, GridData.FILL_HORIZONTAL);

		this.fRefreshRemoteWorkingDirectoryRadioButton = createRadioButton(group, "transfer all files in classpath");
		this.fRefreshRemoteWorkingDirectoryRadioButton.addSelectionListener(this.fListener);
		this.fRefreshRemoteWorkingDirectoryRadioButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RemoteExecTransferTab.this.fClasspathFilteredTree.setEnabled(false);
				RemoteExecTransferTab.this.fClasspathFilteredTree.getFilterControl().setEnabled(false);
				RemoteExecTransferTab.this.fClasspathTreeViewer.getTree().setEnabled(false);
			}
		});

		this.fPartialTransferRadioButton = createRadioButton(group, "transfer partical entries selected below");
		this.fPartialTransferRadioButton.addSelectionListener(this.fListener);
		this.fPartialTransferRadioButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RemoteExecTransferTab.this.fClasspathFilteredTree.setEnabled(true);
				RemoteExecTransferTab.this.fClasspathFilteredTree.getFilterControl().setEnabled(true);
				RemoteExecTransferTab.this.fClasspathTreeViewer.getTree().setEnabled(true);
			}
		});

		this.fClasspathFilteredTree = new ClasspathFilteredTree(group, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, new PatternFilter(), true);

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 500;
		gd.widthHint = 100;
		this.fClasspathFilteredTree.setLayoutData(gd);

		this.fClasspathTreeViewer = (CheckboxTreeViewer) this.fClasspathFilteredTree.getViewer();
		this.fClasspathTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateLaunchConfigurationDialog();
			}
		});
		this.fClasspathTreeViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateLaunchConfigurationDialog();
			}
		});
		final Tree tree = this.fClasspathTreeViewer.getTree();
		tree.addSelectionListener(this.fListener);

		TreeColumn column = new TreeColumn(tree, SWT.LEFT);
		column.setText("classpath");
		column.setWidth(600);
		this.fClasspathTreeViewer.setUseHashlookup(true);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		updateTransferStrategyFromConfiguration(configuration);
	}

	private void updateTransferStrategyFromConfiguration(ILaunchConfiguration configuration) {
		IRuntimeClasspathEntry[] entries;
		try {
			entries = JavaRuntime.computeUnresolvedRuntimeClasspath(configuration);
			entries = JavaRuntime.resolveRuntimeClasspath(entries, configuration);
			List<String> userEntries = new ArrayList<String>(entries.length);
			Set<String> set = new HashSet<String>(entries.length);
			for (int i = 0; i < entries.length; i++) {
				if (entries[i].getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {
					String location = entries[i].getLocation();
					if (location != null) {
						if (!set.contains(location)) {
							userEntries.add(location);
							set.add(location);
						}
					}
				}
			}
			this.fClasspaths = userEntries.toArray(new String[userEntries.size()]);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		int strategy = IRemoteExecConfigurationConstants.PARAMETER_TRANSFER_ALL;
		try {
			strategy = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_TRANSFER_STRATEGY, strategy);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		if (strategy == IRemoteExecConfigurationConstants.PARAMETER_TRANSFER_ALL) {
			this.fRefreshRemoteWorkingDirectoryRadioButton.setSelection(true);
			this.fPartialTransferRadioButton.setSelection(false);
			this.fClasspathFilteredTree.setEnabled(false);
		} else if (strategy == IRemoteExecConfigurationConstants.PARAMETER_TRANSFER_SELECTED) {
			this.fRefreshRemoteWorkingDirectoryRadioButton.setSelection(false);
			this.fPartialTransferRadioButton.setSelection(true);
			this.fClasspathFilteredTree.setEnabled(true);
		}

		List<String> selected = Arrays.asList(this.fClasspaths);
		try {
			selected = configuration.getAttribute(IRemoteExecConfigurationConstants.ATTR_TRANSFER_SELECTED_CLASSPATH_LIST, selected);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		this.fClasspathTreeViewer.setContentProvider(new ClasspathContentProvider());
		this.fClasspathTreeViewer.setLabelProvider(new SharedLabelProvider() {
			@Override
			public String getText(Object element) {
				// TODO Auto-generated method stub
				return super.getText(element.toString());
			}
		});
		this.fClasspathTreeViewer.setInput(this.fClasspaths);
		Tree tree = this.fClasspathTreeViewer.getTree();
		for (String classpath : selected) {
			TreeItem[] items = tree.getItems();
			for (int i = 0; i < items.length; i++) {
				if (((String) items[i].getData()).equals(classpath)) {
					items[i].setChecked(true);
				}
			}
		}
	}

	class ClasspathContentProvider extends DefaultContentProvider implements ITreeContentProvider {
		@Override
		public boolean hasChildren(Object parent) {
			return false;
		}

		@Override
		public Object[] getChildren(Object parent) {
			return new Object[0];
		}

		@Override
		public Object getParent(Object child) {
			return null;
		}

		@Override
		public Object[] getElements(Object input) {
			return (Object[]) input;
//			return RemoteExecTransferTab.this.fClasspaths;
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		int style;
		if (this.fPartialTransferRadioButton.getSelection()) {
			style = IRemoteExecConfigurationConstants.PARAMETER_TRANSFER_SELECTED;
		} else if (this.fRefreshRemoteWorkingDirectoryRadioButton.getSelection()) {
			style = IRemoteExecConfigurationConstants.PARAMETER_TRANSFER_ALL;
		} else {
			style = IRemoteExecConfigurationConstants.PARAMETER_TRANSFER_ALL;
		}
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_TRANSFER_STRATEGY, style);

		Object[] selected = this.fClasspathTreeViewer.getCheckedElements();
		List<Object> list = Arrays.asList(selected);
		configuration.setAttribute(IRemoteExecConfigurationConstants.ATTR_TRANSFER_SELECTED_CLASSPATH_LIST, list);
	}

	@Override
	public String getName() {
		return "Transfer";
	}

}