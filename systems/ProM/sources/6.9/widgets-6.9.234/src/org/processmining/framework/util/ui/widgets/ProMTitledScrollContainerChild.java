package org.processmining.framework.util.ui.widgets;

import java.awt.BorderLayout;

/**
 * @author michael
 * 
 */
public class ProMTitledScrollContainerChild extends ProMScrollContainerChild {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1497690974696615552L;

	private final String title;

	/**
	 * @param title
	 * @param parent
	 */
	public ProMTitledScrollContainerChild(final String title, final ProMScrollContainer parent) {
		this(title, parent, false);
	}

	/**
	 * @param title
	 * @param parent
	 * @param minimized
	 */
	public ProMTitledScrollContainerChild(final String title, final ProMScrollContainer parent, final boolean minimized) {
		super(parent, minimized);

		this.title = title;

		initialize();
	}

	/**
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	private void initialize() {
		final LeftAlignedHeader header = new LeftAlignedHeader(getTitle());

		getTitlePanel().setLayout(new BorderLayout());
		getTitlePanel().setBackground(WidgetColors.PROPERTIES_BACKGROUND);
		getTitlePanel().add(header);

	}

}
