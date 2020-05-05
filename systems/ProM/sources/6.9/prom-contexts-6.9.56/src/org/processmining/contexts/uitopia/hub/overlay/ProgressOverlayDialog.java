package org.processmining.contexts.uitopia.hub.overlay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.uitopia.ui.components.ImageLozengeButton;
import org.deckfour.uitopia.ui.main.MainView;
import org.deckfour.uitopia.ui.overlay.AbstractOverlayDialog;
import org.deckfour.uitopia.ui.util.ImageLoader;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.events.ProgressEventListener;

import com.fluxicon.slickerbox.components.RoundedPanel;
import com.fluxicon.slickerbox.components.SlickerProgressBar;

public class ProgressOverlayDialog extends AbstractOverlayDialog implements ProgressEventListener {

	public static interface CancellationListener {
		public void cancel();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2065412432371789921L;
	protected static Color colorBgUp = new Color(80, 80, 80);
	protected static Color colorBgDown = new Color(40, 40, 40);
	protected static Color colorBgInner = new Color(200, 200, 200, 120);
	protected static Color colorFg = new Color(40, 40, 40);

	private final SlickerProgressBar progress;
	private final JLabel title;
	private final JLabel label;
	private final ImageLozengeButton cancelButton;

	public ProgressOverlayDialog(MainView mainView, final UIPluginContext context, String aTitle) {
		this(mainView, aTitle, new CancellationListener() {

			public void cancel() {
				context.getProgress().cancel();
			}

		});
		context.getProgressEventListeners().add(this);
	}

	public ProgressOverlayDialog(MainView mainView, String aTitle, final CancellationListener cancellationListener) {
		super(aTitle);

		setOpaque(false);
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		RoundedPanel innerPanel = new RoundedPanel(20, 0, 0);
		innerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		innerPanel.setBackground(colorBgInner);
		innerPanel.setMinimumSize(new Dimension(300, 120));
		innerPanel.setMaximumSize(new Dimension(600, 130));
		innerPanel.setPreferredSize(new Dimension(500, 130));
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
		innerPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

		progress = new SlickerProgressBar();
		progress.setOpaque(false);
		progress.setIndeterminate(true);

		title = new JLabel(aTitle);
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		title.setOpaque(false);
		title.setFont(title.getFont().deriveFont(16.0f));

		label = new JLabel("");
		label.setFont(label.getFont().deriveFont(12.0f));
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		label.setOpaque(false);

		cancelButton = new ImageLozengeButton(ImageLoader.load("cancel_white_30x30.png"), "Cancel",
				new Color(90, 0, 0), new Color(160, 0, 0), 4);
		cancelButton.setLabelColor(Color.white);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancellationListener.cancel();
				cancelButton.setEnabled(false);
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.setOpaque(false);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder());
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(progress);
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(cancelButton);
		buttonPanel.add(Box.createHorizontalGlue());

		innerPanel.add(Box.createVerticalGlue());
		innerPanel.add(alignLeft(title));
		innerPanel.add(Box.createVerticalStrut(10));
		innerPanel.add(buttonPanel);
		innerPanel.add(Box.createVerticalStrut(8));
		innerPanel.add(alignLeft(label));
		innerPanel.add(Box.createVerticalGlue());

		add(Box.createHorizontalGlue());
		add(innerPanel);
		add(Box.createHorizontalGlue());

	}

	protected JPanel alignLeft(JComponent component) {
		JPanel enclosure = new JPanel();
		enclosure.setOpaque(false);
		enclosure.setBorder(BorderFactory.createEmptyBorder());
		enclosure.setLayout(new BoxLayout(enclosure, BoxLayout.X_AXIS));
		enclosure.add(component);
		enclosure.add(Box.createHorizontalGlue());
		return enclosure;
	}

	protected JPanel alignRight(JComponent component) {
		JPanel enclosure = new JPanel();
		enclosure.setOpaque(false);
		enclosure.setBorder(BorderFactory.createEmptyBorder());
		enclosure.setLayout(new BoxLayout(enclosure, BoxLayout.X_AXIS));
		enclosure.add(Box.createHorizontalGlue());
		enclosure.add(component);
		return enclosure;
	}

	public void changeProgress(int p) {
		progress.setValue(p);
	}

	public void changeProgressBounds(int lowBo, int upBo) {
		progress.setMinimum(lowBo);
		progress.setMaximum(upBo);
		progress.setIndeterminate(upBo <= lowBo);
	}

	public void changeProgressCaption(String newCaption) {
		label.setText(newCaption);
		revalidate();
	}

	public void changeProgressIndeterminate(boolean indeterminate) {
		progress.setIndeterminate(indeterminate);
	}

	public int getMaximum() {
		return progress.getMaximum();
	}

	public void setIndeterminate(boolean b) {
		progress.setIndeterminate(b);

	}

}
