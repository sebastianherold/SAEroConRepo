package org.processmining.contexts.uitopia.packagemanager;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.deckfour.uitopia.ui.components.TiledPanel;
import org.deckfour.uitopia.ui.components.ViewHeaderBar;
import org.deckfour.uitopia.ui.main.Viewable;
import org.deckfour.uitopia.ui.util.ImageLoader;

public class PMWorkspaceView extends JPanel implements Viewable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8491411287370473317L;
	private ViewHeaderBar header;
	private final JPanel contents;
	private final PMWorkspaceBrowser browser;

	public PMWorkspaceView(final PMController controller) {
		setLayout(new BorderLayout());
		setOpaque(true);
		setBorder(BorderFactory.createEmptyBorder());
		contents = new TiledPanel(ImageLoader.load("tile_wooden.jpg"));
		contents.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
		contents.setLayout(new BorderLayout());
		browser = new PMWorkspaceBrowser(controller);
		contents.add(browser, BorderLayout.CENTER);
		this.add(contents, BorderLayout.CENTER);
	}

	public ViewHeaderBar getHeader() {
		return header;
	}

	public void showPackage(PMPackage pack) {
		browser.showPackage(pack);
	}

	public void showParentsOf(PMPackage pack) {
		browser.showParentsOf(pack);
	}

	public void showChildrenOf(PMPackage pack) {
		browser.showChildrenOf(pack);
	}

	public void updatePackages() {
		browser.updatePackages();
	}

	public void viewFocusGained() {
	}

	public void viewFocusLost() {
	}

	public PMPackageView getPackageView() {
		return browser.getSelectedBrowser().getPackageView();

	}

	//	public void showFavorites() {
	//		browser.showFavorites();
	//	}
}
