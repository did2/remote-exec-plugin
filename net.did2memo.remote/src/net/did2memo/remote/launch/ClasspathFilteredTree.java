package net.did2memo.remote.launch;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

public class ClasspathFilteredTree extends FilteredTree {

	public ClasspathFilteredTree(Composite parent, int treeStyle, PatternFilter filter, boolean useNewLook) {
		super(parent, treeStyle, filter, useNewLook);
	}

	@Override
	protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
		// TODO Auto-generated method stub
//		return super.doCreateTreeViewer(parent, style);
		return new CheckboxTreeViewer(parent, style);
	}
}
