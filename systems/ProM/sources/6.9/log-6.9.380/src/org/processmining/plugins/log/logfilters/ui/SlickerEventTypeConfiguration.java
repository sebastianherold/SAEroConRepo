package org.processmining.plugins.log.logfilters.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.fluxicon.slickerbox.components.GradientPanel;

public class SlickerEventTypeConfiguration extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 52715433965263148L;
	protected static Color colorText = new Color(255, 255, 255, 160);
	protected static Color colorKeep1 = new Color(10, 90, 1);
	protected static Color colorKeep2 = new Color(20, 140, 20);
	protected static Color colorRemove1 = new Color(90, 60, 10);
	protected static Color colorRemove2 = new Color(140, 100, 20);
	protected static Color colorSkipInstance1 = new Color(90, 10, 10);
	protected static Color colorSkipInstance2 = new Color(140, 20, 20);

	public enum EventTypeAction {
		KEEP, REMOVE, SKIP_INSTANCE;
	}

	protected Object[] objects;
	protected EventTypeConfigurationItem[] configurationItems;
	protected ChangeListener updateListener = null;

	public SlickerEventTypeConfiguration(Object[] objects) {
		this.objects = objects;
		setBackground(new Color(60, 60, 60));
		setMinimumSize(new Dimension(70, 40));
		setMaximumSize(new Dimension(1000, 5000));
		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		configurationItems = new EventTypeConfigurationItem[objects.length];
		int height = 0;
		for (int i = 0; i < objects.length; i++) {
			configurationItems[i] = new EventTypeConfigurationItem(objects[i]);
			this.add(configurationItems[i]);
			height += configurationItems[i].getPreferredSize().height + 2;
		}
		this.add(Box.createVerticalGlue());
		setPreferredSize(new Dimension(120, height));
		revalidate();
	}

	public void setUpdateListener(ChangeListener updateListener) {
		this.updateListener = updateListener;
	}

	public String[] getFilteredEventTypes(EventTypeAction action) {
		ArrayList<Object> types = new ArrayList<Object>();
		for (EventTypeConfigurationItem item : configurationItems) {
			if (item.getAction() == action) {
				types.add(item.getObject().toString());
			}
		}
		return types.toArray(new String[0]);
	}

	protected class EventTypeConfigurationItem extends GradientPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7596401396201481445L;
		protected Object object;
		protected EventTypeAction action;
		protected JLabel actionLabel;
		protected JLabel nameLabel;

		public EventTypeConfigurationItem(Object object) {
			super(colorKeep2, colorKeep1);
			setMinimumSize(new Dimension(70, 28));
			setMaximumSize(new Dimension(500, 28));
			setPreferredSize(new Dimension(120, 28));
			setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			this.object = object;
			nameLabel = new JLabel(object.toString());
			nameLabel.setOpaque(false);
			nameLabel.setForeground(colorText);
			nameLabel.setVerticalAlignment(SwingConstants.CENTER);
			nameLabel.setFont(nameLabel.getFont().deriveFont(13f));
			actionLabel = new JLabel("change_me");
			actionLabel.setOpaque(false);
			actionLabel.setForeground(colorText);
			actionLabel.setVerticalAlignment(SwingConstants.CENTER);
			actionLabel.setFont(actionLabel.getFont().deriveFont(12f).deriveFont(Font.ITALIC));
			if (object.toString().equals("reassign") || object.toString().equals("suspend")
					|| object.toString().equals("resume")) {
				setAction(EventTypeAction.REMOVE);
			} else if (object.toString().equals("withdraw") || object.toString().equals("ate_abort")
					|| object.toString().equals("pi_abort")) {
				setAction(EventTypeAction.SKIP_INSTANCE);
			} else {
				// schedule, assign, start, complete, autoskip, manualskip, rest
				setAction(EventTypeAction.KEEP);
			}
			this.add(nameLabel);
			this.add(Box.createHorizontalGlue());
			this.add(actionLabel);
			addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent arg0) {
					if (action == EventTypeAction.KEEP) {
						setAction(EventTypeAction.REMOVE);
					} else if (action == EventTypeAction.REMOVE) {
						setAction(EventTypeAction.SKIP_INSTANCE);
					} else if (action == EventTypeAction.SKIP_INSTANCE) {
						setAction(EventTypeAction.KEEP);
					}
					// notify update listener
					if (updateListener != null) {
						updateListener.stateChanged(new ChangeEvent(this));
					}
				}

				public void mouseEntered(MouseEvent arg0) { /* ignore */
				}

				public void mouseExited(MouseEvent arg0) { /* ignore */
				}

				public void mousePressed(MouseEvent arg0) { /* ignore */
				}

				public void mouseReleased(MouseEvent arg0) { /* ignore */
				}
			});
		}

		public EventTypeAction getAction() {
			return action;
		}

		public Object getObject() {
			return object;
		}

		public void setAction(EventTypeAction action) {
			this.action = action;
			if (action == EventTypeAction.KEEP) {
				actionLabel.setText("(keep)");
				super.setColors(colorKeep2, colorKeep1);
			} else if (action == EventTypeAction.REMOVE) {
				actionLabel.setText("(remove)");
				super.setColors(colorRemove2, colorRemove1);
			} else if (action == EventTypeAction.SKIP_INSTANCE) {
				actionLabel.setText("(discard instance)");
				super.setColors(colorSkipInstance2, colorSkipInstance1);
			}
		}
	}
}
