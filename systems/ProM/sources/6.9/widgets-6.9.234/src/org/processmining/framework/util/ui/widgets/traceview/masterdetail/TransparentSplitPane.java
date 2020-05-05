package org.processmining.framework.util.ui.widgets.traceview.masterdetail;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import org.processmining.framework.util.ui.widgets.WidgetColors;

public class TransparentSplitPane extends JPanel {

	private static final long serialVersionUID = 1L;

	private static class ImprovedSplitPaneDivider extends BasicSplitPaneDivider {
		/**
			 * 
			 */
		private static final long serialVersionUID = 1L;

		private ImprovedSplitPaneDivider(final BasicSplitPaneUI ui) {
			super(ui);
		}

		@Override
		public void paint(final Graphics g) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			paintComponents(g);
		}

		@Override
		protected JButton createLeftOneTouchButton() {
			final JButton b = new JButton() {
				/**
					 * 
					 */
				private static final long serialVersionUID = 1L;

				// Don't want the button to participate in focus traversable.
				@Override
				@Deprecated
				public boolean isFocusTraversable() {
					return false;
				}

				@Override
				public void paint(final Graphics g) {
					if (splitPane != null) {
						final int[] xs = new int[3];
						final int[] ys = new int[3];
						int blockSize;

						// Fill the background first ...
						g.setColor(ImprovedSplitPaneDivider.this.getBackground());
						g.fillRect(0, 0, getWidth(), getHeight());

						// ... then draw the arrow.
						g.setColor(ImprovedSplitPaneDivider.this.getForeground());
						if (orientation == JSplitPane.VERTICAL_SPLIT) {
							blockSize = Math.min(getHeight(), BasicSplitPaneDivider.ONE_TOUCH_SIZE);
							xs[0] = blockSize;
							xs[1] = 0;
							xs[2] = blockSize << 1;
							ys[0] = 0;
							ys[1] = ys[2] = blockSize;
							g.drawPolygon(xs, ys, 3); // Little trick to make the
							// arrows of equal size
						} else {
							blockSize = Math.min(getWidth(), BasicSplitPaneDivider.ONE_TOUCH_SIZE);
							xs[0] = xs[2] = blockSize;
							xs[1] = 0;
							ys[0] = 0;
							ys[1] = blockSize;
							ys[2] = blockSize << 1;
						}
						g.fillPolygon(xs, ys, 3);
					}
				}

				@Override
				public void setBorder(final Border b) {
				}
			};
			b.setMinimumSize(new Dimension(BasicSplitPaneDivider.ONE_TOUCH_SIZE, BasicSplitPaneDivider.ONE_TOUCH_SIZE));
			b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			b.setFocusPainted(false);
			b.setBorderPainted(false);
			b.setRequestFocusEnabled(false);
			b.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					leftClicked();
				}
			});
			return b;
		}

		/**
		 * Creates and return an instance of JButton that can be used to
		 * collapse the right component in the split pane.
		 */
		@Override
		protected JButton createRightOneTouchButton() {
			final JButton b = new JButton() {
				/**
					 * 
					 */
				private static final long serialVersionUID = 1L;

				// Don't want the button to participate in focus traversable.
				@Override
				@Deprecated
				public boolean isFocusTraversable() {
					return false;
				}

				@Override
				public void paint(final Graphics g) {
					if (splitPane != null) {
						final int[] xs = new int[3];
						final int[] ys = new int[3];
						int blockSize;

						// Fill the background first ...
						g.setColor(ImprovedSplitPaneDivider.this.getBackground());
						g.fillRect(0, 0, getWidth(), getHeight());

						// ... then draw the arrow.
						if (orientation == JSplitPane.VERTICAL_SPLIT) {
							blockSize = Math.min(getHeight(), BasicSplitPaneDivider.ONE_TOUCH_SIZE);
							xs[0] = blockSize;
							xs[1] = blockSize << 1;
							xs[2] = 0;
							ys[0] = blockSize;
							ys[1] = ys[2] = 0;
						} else {
							blockSize = Math.min(getWidth(), BasicSplitPaneDivider.ONE_TOUCH_SIZE);
							xs[0] = xs[2] = 0;
							xs[1] = blockSize;
							ys[0] = 0;
							ys[1] = blockSize;
							ys[2] = blockSize << 1;
						}
						g.setColor(ImprovedSplitPaneDivider.this.getForeground());
						g.fillPolygon(xs, ys, 3);
					}
				}

				@Override
				public void setBorder(final Border border) {
				}
			};
			b.setMinimumSize(new Dimension(BasicSplitPaneDivider.ONE_TOUCH_SIZE, BasicSplitPaneDivider.ONE_TOUCH_SIZE));
			b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			b.setFocusPainted(false);
			b.setBorderPainted(false);
			b.setRequestFocusEnabled(false);
			b.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					rightClicked();
				}
			});
			return b;
		}

		protected void leftClicked() {

		}

		protected void rightClicked() {

		}
	}

	public static final int HORIZONTAL_SPLIT = JSplitPane.HORIZONTAL_SPLIT;
	public static final int VERTICAL_SPLIT = JSplitPane.VERTICAL_SPLIT;

	private JSplitPane split;
	private JPanel top, bottom;

	/**
		 * 
		 */
	public TransparentSplitPane() {
		this(TransparentSplitPane.HORIZONTAL_SPLIT);
	}

	/**
	 * @param split
	 */
	public TransparentSplitPane(final int split) {
		super(new BorderLayout());
		setOpaque(true);
		//setBackground(WidgetColors.COLOR_ENCLOSURE_BG);
		//setForeground(WidgetColors.COLOR_ENCLOSURE_BG);		
		top = new JPanel();
		top.setLayout(new BorderLayout());
		//top.setBackground(Color.LIGHT_GRAY);
		top.setOpaque(false);
		bottom = new JPanel();
		bottom.setLayout(new BorderLayout());
		//bottom.setBackground(Color.LIGHT_GRAY);
		bottom.setOpaque(false);
		this.split = new JSplitPane(split, true);
		this.split.setLeftComponent(top);
		this.split.setRightComponent(bottom);
		this.split.setBorder(BorderFactory.createEmptyBorder());
		this.split.setBackground(WidgetColors.COLOR_ENCLOSURE_BG);
		this.split.setOpaque(false);
		this.split.setUI(new BasicSplitPaneUI() {
			@Override
			public BasicSplitPaneDivider createDefaultDivider() {
				final BasicSplitPaneDivider divider = new ImprovedSplitPaneDivider(this) {
					/**
						 * 
						 */
					private static final long serialVersionUID = 1L;

					@Override
					public void leftClicked() {
						TransparentSplitPane.this.leftClicked();
					}

					@Override
					public void rightClicked() {
						TransparentSplitPane.this.rightClicked();
					}
				};
				divider.setBackground(WidgetColors.COLOR_ENCLOSURE_BG);
				divider.setForeground(Color.LIGHT_GRAY);
				return divider;
			}
		});
		add(this.split);
	}

	/**
	 * @param c
	 */
	public void setBottomComponent(final Component c) {
		bottom.removeAll();
		bottom.add(c);
		validate();
	}

	/**
	 * @param d
	 */
	public void setDividerLocation(final double d) {
		split.setDividerLocation(d);

	}

	/**
	 * @param location
	 */
	public void setDividerLocation(final int location) {
		split.setDividerLocation(location);
	}

	/**
	 * @param size
	 */
	public void setDividerSize(final int size) {
		split.setDividerSize(size);
	}

	/**
	 * @param c
	 */
	public void setLeftComponent(final Component c) {
		setTopComponent(c);
	}

	/**
	 * @param expandable
	 */
	public void setOneTouchExpandable(final boolean expandable) {
		split.setOneTouchExpandable(expandable);
	}

	/**
	 * @param d
	 */
	public void setResizeWeight(final double d) {
		split.setResizeWeight(d);
	}

	/**
	 * @param c
	 */
	public void setRightComponent(final Component c) {
		setBottomComponent(c);
	}

	/**
	 * @param c
	 */
	public void setTopComponent(final Component c) {
		top.removeAll();
		top.add(c);
		validate();
	}

	protected void leftClicked() {

	}

	protected void rightClicked() {

	}

	public void setForeground(Color fg) {
		super.setForeground(fg);
		if (top != null) {
			top.setForeground(fg);
		}
		if (bottom != null) {
			bottom.setForeground(fg);
		}
	}

	public void setBackground(Color bg) {
		super.setBackground(bg);
		// Workaround because setOpaque -> false does not work for some reason
		if (top != null) {
			top.setBackground(bg);
		}
		if (bottom != null) {
			bottom.setBackground(bg);
		}
	}

}
