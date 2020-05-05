package org.processmining.contexts.uitopia.model;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CancellationException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.deckfour.uitopia.api.model.Resource;
import org.deckfour.uitopia.api.model.View;
import org.deckfour.uitopia.api.model.ViewType;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.hub.ProMViewManager;
import org.processmining.contexts.uitopia.hub.overlay.ProgressOverlayDialog;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.util.Pair;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.google.common.base.Throwables;

public class ProMView implements View {

	private final class ProMViewRunnable implements Runnable {

		private JComponent content;
		private ProgressOverlayDialog dialog;

		private String message;
		private String stacktrace;
		private PluginDescriptor descriptor;

		public ProMViewRunnable(JComponent content, ProgressOverlayDialog dialog, String message, String stacktrace,
				PluginDescriptor descriptor) {
			this.content = content;
			this.dialog = dialog;
			this.message = message;
			this.stacktrace = stacktrace;
			this.descriptor = descriptor;
		}

		public void run() {
			component.removeAll();
			if (content != null) {
				try {
					content.repaint();
					component.add(content, BorderLayout.CENTER);
				} catch (Exception e) {
					e.printStackTrace();
					//ignore
					message = e.getMessage();
					stacktrace = Throwables.getStackTraceAsString(e);
				}
			}
			if (component.getComponents().length == 0) {
				component.add(buildErrorComponent(message, stacktrace, descriptor), BorderLayout.CENTER);
			}
			dialog.changeProgress(dialog.getMaximum());
		}

		private JComponent buildErrorComponent(final String message, final String stacktrace,
				final PluginDescriptor plugin) {
			final JPanel errorPanel = new JPanel();
			errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
			String userfriendlyMessage = String.format(
					"<html><h1>Unable to produce the requested visualization</h1><h2>Error Message</h2><h3>%s</b></h3></html>",
					message);
			final JEditorPane messagePanel = new JEditorPane("text/html", userfriendlyMessage);
			messagePanel.setEditable(false);
			final JButton debugButton = SlickerFactory.instance().createButton("Show Debug Information");
			debugButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
			debugButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String debugMessage = String.format(
							"<html><body><h1>Unable to produce the requested visualization</h1>"
									+ "<h2>Error Message</h2><h3>%s</b></h3>"
									+ "<h2>Debug Information for Reporting</h2>" + "<p><b>Visualizer</b>: %s</p>"
									+ "<p><b>Stack trace</b>: %s</p>" + "</body></html>",
							message, plugin.getName(),
							stacktrace.replace(System.getProperty("line.separator"), "<p>\n"));
					messagePanel.setText(debugMessage);
					messagePanel.setCaretPosition(0);
					debugButton.removeActionListener(this);
					errorPanel.remove(debugButton);
					errorPanel.validate();
				}
			});
			errorPanel.add(new JScrollPane(messagePanel));
			errorPanel.add(debugButton);
			return errorPanel;
		}
	}

	private static final class ProMCancellerImpl implements ProMCanceller {

		private boolean isCancelled = false;

		public boolean isCancelled() {
			return isCancelled;
		}

		public void cancel() {
			isCancelled = true;
		}

	}

	private final JPanel component;
	private final ProMViewManager manager;
	private String name;
	private final ProMResource<?> resource;
	protected static GraphicsConfiguration gc;
	private final ProMViewType type;
	private BufferedImage original;
	private BufferedImage scaledImage;
	private final Pair<Integer, PluginParameterBinding> binding;
	private boolean working = true;
	private final ProMCancellerImpl proMCanceller;

	public ProMView(ProMViewManager manager, ProMViewType type, ProMResource<?> resource, String name,
			Pair<Integer, PluginParameterBinding> binding) {
		this.manager = manager;
		this.type = type;
		this.resource = resource;
		this.binding = binding;
		resource.setView(this);
		this.name = name;
		component = new JPanel(new BorderLayout());
		component.setBorder(BorderFactory.createEmptyBorder());
		component.setOpaque(false);
		original = toBufferedImage(resource.getType().getTypeIcon());
		proMCanceller = new ProMCancellerImpl();
		refresh(0);
	}

	public void destroy() {
		component.removeAll();
		proMCanceller.cancel();
	}

	public String getCustomName() {
		return name;
	}

	public Image getPreview(int maxWidth, int maxHeight) {
		synchronized (original) {

			int originalWidth = original.getWidth();
			int originalHeight = original.getHeight();
			double scaleFactor = (double) maxWidth / (double) originalWidth;
			double scaleY = (double) maxHeight / (double) originalHeight;
			if (scaleY < scaleFactor) {
				scaleFactor = scaleY;
			}

			int scaledWidth = Math.max(1, (int) (originalWidth * scaleFactor));
			int scaledHeight = Math.max(1, (int) (originalHeight * scaleFactor));

			scaledImage = createCompatibleImage(scaledWidth, scaledHeight);
			Graphics2D g2ds = scaledImage.createGraphics();
			g2ds = scaledImage.createGraphics();
			g2ds.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2ds.drawImage(original, 0, 0, scaledWidth, scaledHeight, null);
			g2ds.dispose();
			return scaledImage;
		}

	}

	public Resource getResource() {
		return resource;
	}

	public JComponent getViewComponent() {
		return component;
	}

	public void setCustomName(String name) {
		this.name = name;
	}

	private BufferedImage createCompatibleImage(int width, int height) {
		if (gc == null) {
			gc = component.getGraphicsConfiguration();
			if (gc == null) {
				gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
						.getDefaultConfiguration();
			}
		}
		return gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
	}

	public ViewType getType() {
		return type;
	}

	public void captureNow() {
		// Record a screen-capture of the currenly visible frame
		synchronized (original) {
			Dimension size = component.getSize();
			if (size.width > 0 && size.height > 0) {
				original = createCompatibleImage(size.width, size.height);
				Graphics2D g2d = original.createGraphics();
				component.paint(g2d);
				g2d.dispose();
			}
		}

	}

	// This method returns a buffered image with the contents of an image
	private BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();

		// Create a buffered image with a format that's compatible with the screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;

			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
		} catch (HeadlessException e) {
			// The system does not have a screen
		}

		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
		}

		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bimage;
	}

	public void refresh() {
		refresh(0);
	}

	public void refresh(final int millisToPopup) {

		final UIPluginContext context = manager.getContext().getMainPluginContext()
				.createChildContext("Visualizing: " + resource.getName());
		context.getParentContext().getPluginLifeCycleEventListeners().firePluginCreated(context);

		final ProgressOverlayDialog dialog = new ProgressOverlayDialog(
				manager.getContext().getController().getMainView(), context,
				"Please wait while updating visualization...");
		dialog.setIndeterminate(false);

		Thread thread = new Thread(new Runnable() {

			public void run() {
				manager.getContext().getController().getMainView().showOverlay(dialog);
				synchronized (ProMView.this) {
					working = true;
				}

				PluginExecutionResult result = getVisualizationResult(context, proMCanceller);

				String message = "no message";
				String stacktrace = "unavailable";
				JComponent content = null;
				try {
					context.log("Starting visualization of " + resource);
					result.synchronize();
					content = result.getResult(binding.getFirst());

					if (content == null) {
						throw new Exception("The visualiser for " + resource.toString()
								+ " returned null. Please select another visualiser.");
					}
				} catch (CancellationException e) {
					proMCanceller.cancel();
					message = "The visualiser is cancelled.";
					stacktrace = "not available";
				} catch (Exception e) {
					message = e.getMessage();
					stacktrace = Throwables.getStackTraceAsString(e);
				} finally {
					context.getParentContext().deleteChild(context);
					SwingUtilities.invokeLater(
							new ProMViewRunnable(content, dialog, message, stacktrace, result.getPlugin()));
					synchronized (ProMView.this) {
						working = false;
						ProMView.this.notifyAll();
					}
					manager.getContext().getController().getMainView().hideOverlay();
				}

			}

			private PluginExecutionResult getVisualizationResult(final UIPluginContext context,
					ProMCanceller proMCanceller) {
				PluginParameterBinding parameterBinding = binding.getSecond();
				List<Class<?>> parameterTypes = parameterBinding.getPlugin()
						.getParameterTypes(parameterBinding.getMethodIndex());
				if (parameterTypes.size() == 2 && parameterTypes.get(1) == ProMCanceller.class) {
					return parameterBinding.invoke(context, resource.getInstance(), proMCanceller);
				} else {
					return parameterBinding.invoke(context, resource.getInstance());
				}
			}

		});
		thread.start();

	}

	public synchronized boolean isReady() {
		return !working;
	}

}
