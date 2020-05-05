package org.processmining.widgets.ui.utils;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public class JSplitPaneUtils {

	public static JSplitPane borderlessSplitPane(int orientation, JComponent left, JComponent right) {
		JSplitPane pane = new JSplitPane(orientation, left, right);
		// svzelst@20151022 the fix for no borders comes from stack-overflow
		// as commented in the thread, this fix is not guaranteed to work cross-platform
		//http://stackoverflow.com/questions/12799640/why-does-jsplitpane-add-a-border-to-my-components-and-how-do-i-stop-it
		pane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		BasicSplitPaneUI flatDividerSplitPaneUI = new BasicSplitPaneUI() {
			@Override
			public BasicSplitPaneDivider createDefaultDivider() {
				return new BasicSplitPaneDivider(this) {

					private static final long serialVersionUID = -7728070024389270847L;

					@Override
					public void setBorder(Border b) {
					}
				};
			}
		};
		pane.setUI(flatDividerSplitPaneUI);
		pane.setBorder(null);
		pane.setOpaque(false);
		return pane;
	}

}
