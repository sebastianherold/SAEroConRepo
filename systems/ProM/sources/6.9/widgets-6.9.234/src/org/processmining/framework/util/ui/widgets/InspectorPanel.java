package org.processmining.framework.util.ui.widgets;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;

import com.fluxicon.slickerbox.components.SlickerTabbedPane;

/**
 * Panel with an Inspector
 * 
 * @author mwesterg
 * 
 */
public class InspectorPanel extends ProMSplitPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel info;
	private final Inspector inspector;
	private final JPanel main;
	private boolean moved;
	private final JPanel tabs;
	boolean minimized = false;

	/**
	 * @deprecated, use InspectorPanel(Frame owner) or InspectorPanel(PluginContext context) instead.
	 */
	@Deprecated
	public InspectorPanel() {
		this((Frame) null);
	}

	public InspectorPanel(PluginContext context) {
		this(context instanceof UIPluginContext ? ((UIPluginContext) context).getGlobalContext().getUI() : (Frame) null);
	}

	public InspectorPanel(Frame owner) {
		super(JSplitPane.VERTICAL_SPLIT);
		final JPanel tmp = new JPanel();
		tmp.setOpaque(false);
		tabs = new JPanel(new BorderLayout());
		tabs.setOpaque(false);
		setTopComponent(tmp);
		setBottomComponent(tabs);
		main = tmp;
		setOneTouchExpandable(true);
		setDividerSize(0);
		setResizeWeight(1.0);
		inspector = new Inspector(owner) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			void minimized(final SlickerTabbedPane tabbedPane) {
				minimized = true;
				tabs.add(tabbedPane);
				setDividerSize(5);
				setDividerLocation(getHeight() - 100);
				tabs.validate();
				validate();
				repaint();
			}
		};
		info = inspector.addTab("Info");

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				resize();
			}
		});

		addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorAdded(final AncestorEvent arg0) {
				if (!minimized) {
					inspector.setLocationByPlatform(false);
					inspector.setVisible(true);
					validate();
					InspectorPanel.this.requestFocus();
				}
			}

			@Override
			public void ancestorMoved(final AncestorEvent arg0) {

			}

			@Override
			public void ancestorRemoved(final AncestorEvent arg0) {
				if (!minimized) {
					inspector.setVisible(false);
				}
			}
		});

	}

	/**
	 * @see java.awt.Container#add(java.awt.Component)
	 */
	@Override
	public Component add(final Component c) {
		if (main != null) {
			main.add(c);
		} else {
			super.add(c);
		}
		return c;
	}

	/**
	 * @param header
	 * @param component
	 */
	public void addInfo(final String header, final JComponent component) {
		inspector.addGroup(info, header, component);
	}

	/**
	 * @see java.awt.Container#remove(java.awt.Component)
	 */
	@Override
	public void remove(final Component c) {
		if (main != null) {
			main.remove(c);
		} else {
			super.remove(c);
		}
	}

	/**
	 * 
	 */
	public void resize() {
		if (!moved) {
			final Point point = getLocationOnScreen();
			inspector.setLocation((int) (point.getX() + getWidth() - inspector.getWidth() - 8),
					(int) (point.getY() + 8));
			moved = true;
		}
	}

	/**
	 * @see java.awt.Container#setLayout(java.awt.LayoutManager)
	 */
	@Override
	public void setLayout(final LayoutManager layout) {
		if (main != null) {
			main.setLayout(layout);
		} else {
			super.setLayout(layout);
		}
	}

	/**
	 * @see java.awt.Container#validate()
	 */
	@Override
	public void validate() {
		super.validate();
		if (!minimized) {
			setDividerLocation(5000);
		}
	}

	/**
	 * @return the inspector
	 */
	protected Inspector getInspector() {
		return inspector;
	}

	@Override
	protected void rightClicked() {
		minimized = false;
		inspector.restore();
		setDividerSize(0);
		setDividerLocation(getHeight());
		tabs.validate();
		validate();
		repaint();
	}
}
