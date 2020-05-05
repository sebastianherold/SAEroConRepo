package org.processmining.contexts.uitopia.packagemanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import org.deckfour.uitopia.ui.util.ImageLoader;
import org.deckfour.uitopia.ui.util.LinkLabel;
import org.deckfour.uitopia.ui.util.Tooltips;

import com.fluxicon.slickerbox.components.RoundedPanel;
import com.fluxicon.slickerbox.components.SlickerSearchField;

public class PMSearchView extends RoundedPanel {

	private static final long serialVersionUID = 5079107169836528703L;

	private final PMWorkspaceBrowser pmWorkspaceBrowser;
	private final PMController controller;
	private SlickerSearchField filterSearch;

	public PMSearchView(PMWorkspaceBrowser pmWorkspaceBrowser, PMController controller) {
		super(20, 5, 0);
		this.pmWorkspaceBrowser = pmWorkspaceBrowser;
		this.controller = controller;
		setBackground(new Color(160, 160, 160));
		setLayout(new BorderLayout());
		setupUI();
	}

	private void setupUI() {
		// Filter: Text search
		filterSearch = new SlickerSearchField(50, 40, new Color(140, 140, 140), new Color(80, 80, 80),
				new Color(40, 40, 40), new Color(20, 20, 20));
		filterSearch.requestFocusInWindow();
		filterSearch.setFont(filterSearch.getFont().deriveFont(Font.BOLD, 14.0f));
		filterSearch.addSearchListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateFilter();
			}
		});
		filterSearch.setToolTipText(Tooltips.ACTIONFILTERSEARCHFIELD);
		this.add(filterSearch);		
		
		// Add clickable ProM icon.
		Image appIcon = ImageLoader.load("prom_logo_101x40.png");
		JLabel logoLabel = new LinkLabel(appIcon, "http://www.promtools.org");
		logoLabel.setOpaque(false);
		logoLabel.setBorder(BorderFactory.createEmptyBorder());
		this.add(logoLabel, BorderLayout.WEST);
	}

	private void updateFilter() {
		// construct search reges from search field
		String search = filterSearch.getSearchText().toLowerCase().trim();
		if (search.length() > 0) {
			search.replaceAll("\\w", "(.*)");
			search = "(.*)" + search + "(.*)";
		} else {
			search = "(.*)";
		}
		// filter list of packages according to search field
		controller.setQuery(search);
		if (!search.isEmpty())
			pmWorkspaceBrowser.update();
	}
}
