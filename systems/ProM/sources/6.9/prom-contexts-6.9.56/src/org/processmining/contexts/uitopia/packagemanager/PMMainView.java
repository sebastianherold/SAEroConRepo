package org.processmining.contexts.uitopia.packagemanager;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.deckfour.uitopia.ui.conf.ConfigurationSet;
import org.deckfour.uitopia.ui.conf.UIConfiguration;
import org.deckfour.uitopia.ui.main.MainViewport;
import org.deckfour.uitopia.ui.main.Overlayable;
import org.deckfour.uitopia.ui.main.Viewable;
import org.deckfour.uitopia.ui.overlay.OverlayEnclosure;
import org.deckfour.uitopia.ui.overlay.TwoButtonOverlayDialog;
import org.processmining.framework.packages.PackageManager;

public class PMMainView extends JPanel implements Overlayable {

	private static final long serialVersionUID = -3555712585055859714L;

	public enum View {

		WORKSPACE();

		private Viewable viewable;

		public Viewable getViewable() {
			return viewable;
		}

		public void setViewable(Viewable viewable) {
			this.viewable = viewable;
		}

	}

	private final PMController controller;

	private final ConfigurationSet conf;
	//	private MainToolbar toolbar;
	private final MainViewport viewport;

	private View activeView;
	private final PMWorkspaceView workspaceView;

	private OverlayEnclosure overlay;
	private final PMListener listener;

	public PMMainView(PMController controller) {
		this.controller = controller;
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new BorderLayout());
		viewport = new MainViewport();
		//		toolbar = new MainToolbar(this);
		//		this.add(toolbar, BorderLayout.NORTH);
		this.add(viewport, BorderLayout.CENTER);
		JPanel blankView = new JPanel();
		blankView.setBackground(new Color(50, 50, 50));
		show(blankView);
		workspaceView = new PMWorkspaceView(controller);
		View.WORKSPACE.setViewable(workspaceView);
		conf = UIConfiguration.master().getChild(this.getClass().getCanonicalName());
		activeView = View.valueOf(conf.get("activeView", "WORKSPACE"));
		showView(activeView);

		listener = new PMListener(this);
		PackageManager.getInstance().addListener(listener);
	}

	public PMController controller() {
		return controller;
	}

	public PMWorkspaceView getWorkspaceView() {
		return workspaceView;
	}

	public void showWorkspaceView() {
		if (!activeView.equals(View.WORKSPACE)) {
			showView(View.WORKSPACE);
		}
	}

	public void showWorkspaceView(PMPackage selected) {
		workspaceView.showPackage(selected);
		showWorkspaceView();
	}

	protected void showView(View view) {
		if (activeView.getViewable() != view.getViewable()) {
			activeView.getViewable().viewFocusLost();
		}
		if (view.equals(View.WORKSPACE)) {
			show(workspaceView);
		}
		conf.set("activeView", view.name());
		if (activeView.getViewable() != view.getViewable()) {
			view.getViewable().viewFocusGained();
		}
		activeView = view;
		//		toolbar.activateTab(view);
	}

	public void show(JComponent view) {
		viewport.setView(view);
	}

	public void showOverlay(JComponent overlay) {
		final OverlayEnclosure enclosure = new OverlayEnclosure(overlay, 1024, 768);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//				toolbar.setEnabled(false);
				viewport.showOverlay(enclosure);
			}
		});
		this.overlay = enclosure;
	}

	public boolean showOverlayDialog(TwoButtonOverlayDialog dialog) {
		//		dialog.setMainView(this);
		//		showOverlay(dialog);
		//		return dialog.getResultBlocking();
		return true;
	}

	public void hideOverlay() {
		//		toolbar.setEnabled(true);
		viewport.hideOverlay();
	}

	public boolean hideOverlay(JComponent overlay) {
		if ((this.overlay != null) && (this.overlay.getEnclosed() == overlay)) {
			hideOverlay();
			return true;
		} else {
			return false;
		}
	}

}
