package org.processmining.plugins.log.logfilters.ui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.collection.AlphanumComparator;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.plugins.log.logfilters.impl.DefaultLogFilter;
import org.processmining.plugins.log.logfilters.impl.EventLogFilter;
import org.processmining.plugins.log.logfilters.impl.FinalEventLogFilter;
import org.processmining.plugins.log.logfilters.impl.StartEventLogFilter;
import org.processmining.plugins.log.logfilters.ui.SlickerEventTypeConfiguration.EventTypeAction;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.components.RoundedPanel;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.fluxicon.slickerbox.ui.SlickerScrollBarUI;

/**
 * Simple log filter. Allows the user to use the following filters in the given
 * order: 1. Filter events using the lifecycle transition classification. 2.
 * Filter traces on start events using the standard classification (concept name
 * and lifecycle transition). 3. Filter traces on end events using the standard
 * classification (concept name and lifecycle transition). 4. Filter events
 * using the standard classification (concept name and lifecycle transition).
 * During steps 2, 3, and 4, the user can use a slider to indicate how much
 * coverage s/he wants. The most occurring events/traces will then be selected
 * until the coverage has been reached.
 * 
 * @author hverbeek
 * 
 */
public class LogFilterUI {

	private final UIPluginContext context;
	private int nofSteps;
	private int eventTypeStep;
	private int startEventStep;
	private int endEventStep;
	private int eventFilterStep;
	private int currentStep;
	private myStep[] mySteps;
	private String name;

	/**
	 * Whether to use all events, only the start events, or only the end events.
	 */
	enum Mode {
		ALLEVENTS, STARTEVENTS, ENDEVENTS
	}

	public LogFilterUI(UIPluginContext context) {
		this.context = context;
	}

	public String getName() {
		return name;
	}
	
	/**
	 * Runs the simple log filter on the given log.
	 * 
	 * @param log
	 *            The given log.
	 * @return The filtered log (depends on the settings chosen by the user).
	 */
	public XLog filter(XLog log) {
		InteractionResult result;

		name = XConceptExtension.instance().extractName(log) + " (filtered on simple heuristics)";
		/*
		 * Create all filter steps.
		 */
		nofSteps = 0;
		eventTypeStep = nofSteps++;
		startEventStep = nofSteps++;
		endEventStep = nofSteps++;
		eventFilterStep = nofSteps++;

		mySteps = new myStep[nofSteps];

		mySteps[eventTypeStep] = new EventTypeStep();
		mySteps[startEventStep] = new StartEventStep();
		mySteps[endEventStep] = new EndEventStep();
		mySteps[eventFilterStep] = new EventFilterStep();

		/*
		 * Initialize the first step.
		 */
		currentStep = eventTypeStep;
		mySteps[currentStep].initComponents(log);

		/*
		 * The wizard loop.
		 */
		while (true) {
			/*
			 * Show the current step.
			 */
			result = context.showWizard("Log Filter", currentStep == 0, currentStep == nofSteps - 1,
					mySteps[currentStep]);
			switch (result) {
				case NEXT :
					/*
					 * Show the next step. First get the log of the previous
					 * step, then move the next step, and initialize it with the
					 * filtered log of the previous step.
					 */
					//					long time = -System.currentTimeMillis();
					XLog filteredLog = mySteps[currentStep].getLog();
					//					time += System.currentTimeMillis();
					//					System.err.println("[LogFilterUI] log time = " + time);
					go(1);
					//					time = -System.currentTimeMillis();
					mySteps[currentStep].initComponents(filteredLog);
					mySteps[currentStep].repaint();
					//					time += System.currentTimeMillis();
					//					System.err.println("[LogFilterUI] UI time = " + time);
					break;
				case PREV :
					/*
					 * Move back. The previous step should still be valid.
					 */
					go(-1);
					break;
				case FINISHED :
					/*
					 * Return the filtered log of the final step.
					 */
					return mySteps[currentStep].getLog();
				default :
					/*
					 * Should not occur.
					 */
					return null;
			}
		}
	}

	/**
	 * Move the wizard in either direction.
	 * 
	 * @param direction
	 *            The direction, use 1 for forward and -1 for backward.
	 * @return The next step.
	 */
	private int go(int direction) {
		currentStep += direction;
		if ((currentStep >= 0) && (currentStep < nofSteps)) {
			if (mySteps[currentStep].precondition()) {
				return currentStep;
			} else {
				return go(direction);
			}
		}
		return currentStep;
	}

	/*
	 * Now follows a section containing some graphical improvements. Not
	 * important for the logic.
	 */
	protected Color colorBg = new Color(140, 140, 140);
	protected Color colorOuterBg = new Color(100, 100, 100);
	protected Color colorListBg = new Color(60, 60, 60);
	protected Color colorListBgSelected = new Color(10, 90, 10);
	protected Color colorListFg = new Color(200, 200, 200, 160);
	protected Color colorListFgSelected = new Color(230, 230, 230, 200);
	protected Color colorListEnclosureBg = new Color(150, 150, 150);
	protected Color colorListHeader = new Color(10, 10, 10);
	protected Color colorListDescription = new Color(60, 60, 60);

	protected JComponent configureList(JList<Object> list, String title, String description) {
		list.setFont(list.getFont().deriveFont(13f));
		list.setBackground(colorListBg);
		list.setForeground(colorListFg);
		list.setSelectionBackground(colorListBgSelected);
		list.setSelectionForeground(colorListFgSelected);
		list.setFont(list.getFont().deriveFont(12f));
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setSelectionInterval(0, list.getModel().getSize() - 1);
		return configureAnyScrollable(list, title, description);
	}

	protected JComponent configureAnyScrollable(JComponent scrollable, String title, String description) {
		RoundedPanel enclosure = new RoundedPanel(10, 5, 5);
		enclosure.setBackground(colorListEnclosureBg);
		enclosure.setLayout(new BoxLayout(enclosure, BoxLayout.Y_AXIS));
		JLabel headerLabel = new JLabel(title);
		headerLabel.setOpaque(false);
		headerLabel.setForeground(colorListHeader);
		headerLabel.setFont(headerLabel.getFont().deriveFont(14f));
		JLabel descriptionLabel = new JLabel("<html>" + description + "</html>");
		descriptionLabel.setOpaque(false);
		descriptionLabel.setForeground(colorListDescription);
		descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(11f));
		JScrollPane listScrollPane = new JScrollPane(scrollable);
		listScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		listScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		listScrollPane.setViewportBorder(BorderFactory.createLineBorder(new Color(40, 40, 40)));
		listScrollPane.setBorder(BorderFactory.createEmptyBorder());
		JScrollBar vBar = listScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, colorListEnclosureBg, new Color(30, 30, 30), new Color(80, 80, 80), 4,
				12));
		enclosure.add(packLeftAligned(headerLabel));
		enclosure.add(Box.createVerticalStrut(3));
		enclosure.add(packLeftAligned(descriptionLabel));
		enclosure.add(Box.createVerticalStrut(5));
		enclosure.add(listScrollPane);
		return enclosure;
	}

	protected JComponent packLeftAligned(JComponent component) {
		JPanel packed = new JPanel();
		packed.setOpaque(false);
		packed.setBorder(BorderFactory.createEmptyBorder());
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));
		packed.add(component);
		packed.add(Box.createHorizontalGlue());
		return packed;
	}

	/**
	 * Basic step class. Every step has a log, a way to check whether it should
	 * be displayed, a way to get the filtered log, and a way to initialize it
	 * with some log.
	 * 
	 * @author hverbeek
	 * 
	 */
	private abstract class myStep extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6125158693462475923L;
		/**
		 * The log to filter using this step.
		 */
		protected XLog log;

		public void setLog(XLog log) {
			this.log = log;
		}

		/**
		 * Returns whether this step should be displayed. If not, it should be
		 * skipped.
		 * 
		 * @return Whether to display this step.
		 */
		public abstract boolean precondition();

		/**
		 * Returns the filtered log.
		 * 
		 * @return The filtered log.
		 */
		public abstract XLog getLog();

		/**
		 * Initializes the step given the log to filter.
		 * 
		 * @param log
		 *            The log to filter.
		 */
		public abstract void initComponents(XLog log);
	}

	private class EventClassComparator implements Comparator<XEventClass> {

		public int compare(XEventClass o1, XEventClass o2) {
			// TODO Auto-generated method stub
			return (new AlphanumComparator().compare(o1.toString(), o2.toString()));
		}
	}

	/**
	 * Simple step class. All steps belong to this class, but in the future
	 * additional (non-simple) steps may be added.
	 * 
	 * @author hverbeek
	 * 
	 */
	private abstract class SimpleStep extends myStep {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2288113155552125657L;
		/**
		 * The classifier to use for the filtering.
		 */
		protected XEventClassifier classifier;
		/**
		 * The log info obtained for this classifier.
		 */
		private XLogInfo logInfo;
		/**
		 * The advanced selection list used in step 1.
		 */
		protected SlickerEventTypeConfiguration cfg;
		/**
		 * The simple selection list used in steps 2,3 , and 4.
		 */
		protected JComponent comp;
		protected JList<Object> list;
		/**
		 * Whether to use the advanced (true) or simple (false) selection list.
		 */
		private final boolean useCfg;
		/**
		 * The heading to display for the simple selection list.
		 */
		private final String heading;
		/**
		 * The text to display for the simple selection list.
		 */
		private final String text;
		/**
		 * Whether to use all events, only start events, or only end events.
		 */
		private final Mode mode;
		/**
		 * The event classes found.
		 */
		private XEventClasses eventClasses;
		private List<XEventClass> sortedEventClasses;
		/**
		 * The slider used for the simple selection list.
		 */
		private NiceSlider slider;

		/**
		 * Creates the simple step.
		 * 
		 * @param classifier
		 *            The classifier to use in this step.
		 * @param mode
		 *            The mode to use (all events, start events, or end events).
		 * @param useCfg
		 *            Whether to use the advanced list or the simple list.
		 * @param heading
		 *            The heading to use, if simple list (null otherwise).
		 * @param text
		 *            The text to use, if simple list (null otherwise).
		 */
		public SimpleStep(XEventClassifier classifier, Mode mode, boolean useCfg, String heading, String text) {
			this.classifier = classifier;
			this.mode = mode;
			this.useCfg = useCfg;
			this.heading = heading;
			this.text = text;
			cfg = null;
			comp = null;
			slider = null;
		}

		/**
		 * All steps may always be displayed.
		 */
		public boolean precondition() {
			// TODO Auto-generated method stub
			return true;
		}

		/**
		 * Initializes the component, given the log to filter.
		 */
		public void initComponents(XLog log) {
			setLog(log);
			double size[][] = { { 80, TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, 30, 30 } };
			setLayout(new TableLayout(size));
			add(new JLabel("Log name:"), "0, 2");
			final ProMTextField textField = new ProMTextField(name);
			textField.setPreferredSize(new Dimension(100, 25));
			this.add(textField, "1, 2");
			textField.addKeyListener(new KeyListener() {

				public void keyTyped(KeyEvent e) {
					name = textField.getText();
				}

				public void keyPressed(KeyEvent e) {
					name = textField.getText();
				}

				public void keyReleased(KeyEvent e) {
					name = textField.getText();
				}
				
			});

			/**
			 * Initialize the event classes.
			 */
			eventClasses = null;
			switch (mode) {
				case ALLEVENTS :
					logInfo = XLogInfoImpl.create(log, classifier);
					eventClasses = logInfo.getEventClasses(classifier);
					break;
				case STARTEVENTS :
					eventClasses = new XEventClasses(classifier);
					for (XTrace trace : log) {
						if (!trace.isEmpty()) {
							eventClasses.register(trace.get(0));
						}
					}
					break;
				case ENDEVENTS :
					eventClasses = new XEventClasses(classifier);
					for (XTrace trace : log) {
						if (!trace.isEmpty()) {
							eventClasses.register(trace.get(trace.size() - 1));
						}
					}
					break;
				default :
			}
			/**
			 * Initialize the selection list.
			 */
			if (eventClasses != null) {
				if (useCfg) {
					if (cfg != null) {
						this.remove(cfg);
					}
					cfg = new SlickerEventTypeConfiguration(eventClasses.getClasses().toArray());
					this.add(cfg, "0, 0, 1, 0");
				} else {
					if (comp != null) {
						this.remove(comp);
					}
					sortedEventClasses = new ArrayList<XEventClass>(eventClasses.getClasses());
					Collections.sort(sortedEventClasses, new EventClassComparator());
					list = new JList<Object>(sortedEventClasses.toArray());
					comp = configureList(list, heading, text);
					this.add(comp, "0, 0, 1, 0");
				}
			}
			/**
			 * Initialize the slider, if necessary.
			 */
			if (!useCfg) {
				if (slider != null) {
					this.remove(slider);
				}
				slider = SlickerFactory.instance().createNiceIntegerSlider("Select top percentage", 0, 100, 80,
						Orientation.HORIZONTAL);
				ChangeListener listener = new ChangeListener() {

					public void stateChanged(ChangeEvent e) {
						// TODO Auto-generated method stub
						int percentage = slider.getSlider().getValue();
						int size = 0;
						TreeSet<Integer> eventSizes = new TreeSet<Integer>();
						for (XEventClass event : sortedEventClasses) {
							size += event.size();
							eventSizes.add(event.size());
						}
						int treshold = size * percentage;
						int value = 0;
						list.clearSelection();
						while (100 * value < treshold) {
							int eventSize = eventSizes.last();
							eventSizes.remove(eventSize);
							int index = 0;
							for (XEventClass event : sortedEventClasses) {
								if (event.size() == eventSize) {
									value += eventSize;
									list.addSelectionInterval(index, index);
								}
								index++;
							}
						}
					}

				};
				slider.addChangeListener(listener);
				listener.stateChanged(null);
				this.add(slider, "0, 1, 1, 1");
			}
		}
	}

	private class EventTypeStep extends SimpleStep {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1266880064535493470L;

		public EventTypeStep() {
			super(XLogInfoImpl.LIFECYCLE_TRANSITION_CLASSIFIER, Mode.ALLEVENTS, true, null, null);
		}

		public XLog getLog() {
			String[] toRemove = cfg.getFilteredEventTypes(EventTypeAction.REMOVE);
			String[] toSkip = cfg.getFilteredEventTypes(EventTypeAction.SKIP_INSTANCE);
			//			PluginContext filterContext = context.createChildContext("Default Log Filter");
			DefaultLogFilter filter = new DefaultLogFilter();
			return filter.filter(null, log, toRemove, toSkip);

			/*
			 * try { return
			 * context.tryToFindOrConstructFirstNamedObject(XLog.class,
			 * "Default Log Filter", Connection.class, "", log, toRemove,
			 * toSkip); } catch (ConnectionCannotBeObtained e) { // TODO
			 * Auto-generated catch block e.printStackTrace(); }
			 */
			/*
			 * Set<Pair<Integer, PluginParameterBinding>> plugins =
			 * context.getPluginManager().find(Plugin.class, XLog.class,
			 * context.getPluginContextType(), true, true, true, XLog.class,
			 * String[].class, String[].class); for (Pair<Integer,
			 * PluginParameterBinding> plugin: plugins) { if
			 * (plugin.getSecond().
			 * getPlugin().getName().equals("Default Log Filter")) {
			 * UIPluginContext subContext =
			 * context.createChildContext("Default Log Filter");
			 * PluginExecutionResult result =
			 * plugin.getSecond().invoke(subContext, log, toRemove, toSkip); try
			 * { result.synchronize(); } catch (CancellationException e) { //
			 * TODO Auto-generated catch block e.printStackTrace(); } catch
			 * (ExecutionException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); } catch (InterruptedException e) { // TODO
			 * Auto-generated catch block e.printStackTrace(); } return
			 * result.getResult(plugin.getFirst()); } } return log;
			 */
		}
	}

	private class StartEventStep extends SimpleStep {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8547494758416001029L;

		public StartEventStep() {
			super(XLogInfoImpl.STANDARD_CLASSIFIER, Mode.STARTEVENTS, false, "Start events",
					"Only instances starting with a green event will be used.");
		}

		public XLog getLog() {
			// TODO Auto-generated method stub
			List<Object> selectedObjects = list.getSelectedValuesList();
			String[] startIds = new String[selectedObjects.size()];
			for (int i = 0; i < selectedObjects.size(); i++) {
				startIds[i] = selectedObjects.get(i).toString();
			}
			//			PluginContext filterContext = context.createChildContext("Start Event Log Filter");
			StartEventLogFilter filter = new StartEventLogFilter();
			return filter.filterWithClassifier(null, log, classifier, startIds);

			/*
			 * try { return
			 * context.tryToFindOrConstructFirstNamedObject(XLog.class,
			 * "Start Event Log Filter", Connection.class, "", log, classifier,
			 * startIds); } catch (ConnectionCannotBeObtained e) { // TODO
			 * Auto-generated catch block e.printStackTrace(); } return log;
			 */
		}
	}

	private class EndEventStep extends SimpleStep {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4750780067360884545L;

		public EndEventStep() {
			super(XLogInfoImpl.STANDARD_CLASSIFIER, Mode.ENDEVENTS, false, "End events",
					"Only instances ending with a green event will be used.");
		}

		public XLog getLog() {
			// TODO Auto-generated method stub
			List<Object> selectedObjects = list.getSelectedValuesList();
			String[] endIds = new String[selectedObjects.size()];
			for (int i = 0; i < selectedObjects.size(); i++) {
				endIds[i] = selectedObjects.get(i).toString();
			}
			//			PluginContext filterContext = context.createChildContext("Final Event Log Filter");
			FinalEventLogFilter filter = new FinalEventLogFilter();
			return filter.filterWithClassifier(null, log, classifier, endIds);

			/*
			 * try { return
			 * context.tryToFindOrConstructFirstNamedObject(XLog.class,
			 * "Final Event Log Filter", Connection.class, "", log, classifier,
			 * endIds); } catch (ConnectionCannotBeObtained e) { // TODO
			 * Auto-generated catch block e.printStackTrace(); } return log;
			 */
		}
	}

	private class EventFilterStep extends SimpleStep {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2295002325162718535L;

		public EventFilterStep() {
			super(XLogInfoImpl.STANDARD_CLASSIFIER, Mode.ALLEVENTS, false, "Event filter",
					"Only green events will be used.");
		}

		public XLog getLog() {
			// TODO Auto-generated method stub
			List<Object> selectedObjects = list.getSelectedValuesList();
			String[] selectedIds = new String[selectedObjects.size()];
			for (int i = 0; i < selectedObjects.size(); i++) {
				selectedIds[i] = selectedObjects.get(i).toString();
			}
			//			PluginContext filterContext = context.createChildContext("Event Log Filter");
			EventLogFilter filter = new EventLogFilter();
			return filter.filterWithClassifier(null, log, classifier, selectedIds);

			/*
			 * try { return
			 * context.tryToFindOrConstructFirstNamedObject(XLog.class,
			 * "Event Log Filter", Connection.class, "", log, eventClasses,
			 * selectedClasses); } catch (ConnectionCannotBeObtained e) { //
			 * TODO Auto-generated catch block e.printStackTrace(); } return
			 * log;
			 */
		}

	}
}
