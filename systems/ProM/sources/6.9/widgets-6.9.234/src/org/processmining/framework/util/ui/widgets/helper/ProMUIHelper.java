package org.processmining.framework.util.ui.widgets.helper;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.contexts.uitopia.UIPluginContext;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

/**
 * Commonly used functionality for the ProM UI as static helper methods. All
 * methods throw {@link UserCancelledException} when the user presses the cancel
 * button.
 * 
 * @author F. Mannhardt
 * 
 */
public class ProMUIHelper {

	/******************* ERROR / WARNING / INFO MESSAGES *****************/

	/**
	 * Displays an error message in a 'modal' pop-up using the global UI as
	 * parent JFrame.
	 * 
	 * @param context
	 * @param errorMessage
	 * @param errorTitle
	 */
	public static void showErrorMessage(UIPluginContext context, String errorMessage, String errorTitle) {
		Object[] options = { "OK" };

		JOptionPane.showOptionDialog(context.getGlobalContext().getUI(), createMessageBody(errorMessage), errorTitle,
				JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
	}

	/**
	 * Displays an error message in a 'modal' pop-up using a default JFrame.
	 * 
	 * @param errorMessage
	 * @param errorTitle
	 */
	public static void showErrorMessage(String errorMessage, String errorTitle) {
		Object[] options = { "OK" };
		JOptionPane.showOptionDialog(null, createMessageBody(errorMessage), errorTitle, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.ERROR_MESSAGE, null, options, options[0]);
	}

	/**
	 * Displays an error message in a 'modal' pop-up using the specified
	 * Component as parent.
	 * 
	 * @param component
	 * @param errorMessage
	 * @param errorTitle
	 */
	public static void showErrorMessage(Component component, String errorMessage, String errorTitle) {
		Object[] options = { "OK" };
		JOptionPane.showOptionDialog(component, createMessageBody(errorMessage), errorTitle, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.ERROR_MESSAGE, null, options, options[0]);
	}

	public static void showErrorMessage(Component component, String errorMessage, String errorTitle, Throwable e) {
		Object[] options = { "OK", "Show Debug Information" };
		int result = JOptionPane.showOptionDialog(component, createMessageBody(errorMessage), errorTitle,
				JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
		if (result == 1) {
			String[] optionsStacktrace = new String[] { "OK" };
			JOptionPane.showOptionDialog(component, createMessageBody(Throwables.getStackTraceAsString(e)),
					"Debug Information: " + errorTitle, JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null,
					optionsStacktrace, optionsStacktrace[0]);
		}
	}

	/**
	 * Displays an warning message in a 'modal' pop-up using the global UI as
	 * parent JFrame.
	 * 
	 * @param context
	 * @param warnMessage
	 * @param warnTitle
	 */
	public static void showWarningMessage(UIPluginContext context, String warnMessage, String warnTitle) {
		Object[] options = { "OK" };
		JOptionPane.showOptionDialog(context.getGlobalContext().getUI(), createMessageBody(warnMessage), warnTitle,
				JOptionPane.PLAIN_MESSAGE, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
	}

	/**
	 * Displays an warning message in a 'modal' pop-up using a default JFrame.
	 * 
	 * @param warnMessage
	 * @param warnTitle
	 */
	public static void showWarningMessage(String warnMessage, String warnTitle) {
		Object[] options = { "OK" };
		JOptionPane.showOptionDialog(null, createMessageBody(warnMessage), warnTitle, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.WARNING_MESSAGE, null, options, options[0]);
	}

	/**
	 * Displays an warning message in a 'modal' pop-up using using the specified
	 * Component as parent.
	 * 
	 * @param component
	 * @param warnMessage
	 * @param warnTitle
	 */
	public static void showWarningMessage(Component component, String warnMessage, String warnTitle) {
		Object[] options = { "OK" };
		JOptionPane.showOptionDialog(component, createMessageBody(warnMessage), warnTitle, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.WARNING_MESSAGE, null, options, options[0]);
	}

	private static JComponent createMessageBody(String message) {
		JTextArea errorBody = new JTextArea(message);
		errorBody.setWrapStyleWord(true);
		errorBody.setLineWrap(true);
		return new JScrollPane(errorBody) {

			private static final long serialVersionUID = 1L;

			public Dimension getPreferredSize() {
				return new Dimension(480, 320);
			}

		};
	}

	/******************* QUERY DIALOGS *****************/

	/**
	 * Displays a configuration dialog asking the user to enter a String
	 * literal.
	 * 
	 * @param context
	 * @param queryCaption
	 * @return
	 * @throws UserCancelledException
	 */
	public static String queryForString(UIPluginContext context, String queryCaption) throws UserCancelledException {
		return queryForString(context, queryCaption, null, null);
	}
	
	public static String queryForString(Component view, String queryCaption) throws UserCancelledException {
		return queryForString(view, queryCaption, null, null);
	}

	/**
	 * Displays a configuration dialog asking the user to enter a String literal
	 * 
	 * @param context
	 * @param queryCaption
	 * @param queryText
	 * @return
	 * @throws UserCancelledException
	 */
	public static String queryForString(UIPluginContext context, String queryCaption, String queryText)
			throws UserCancelledException {
		return queryForString(context, queryCaption, queryText, null);
	}
	
	public static String queryForString(Component view, String queryCaption, String queryText)
			throws UserCancelledException {
		return queryForString(view, queryCaption, queryText, null);
	}

	/**
	 * Displays a configuration dialog asking the user to enter a String literal
	 * 
	 * @param context
	 * @param queryCaption
	 * @param queryText
	 * @param defaultValue
	 * @return
	 * @throws UserCancelledException
	 */
	public static String queryForString(UIPluginContext context, String queryCaption, String queryText,
			String defaultValue) throws UserCancelledException {
		StringQueryPanel queryPanel = new StringQueryPanel(queryText, defaultValue);
		InteractionResult choice = queryPanel.getUserChoice(context, queryCaption);
		if (choice == InteractionResult.FINISHED || choice == InteractionResult.CONTINUE) {
			return queryPanel.getResult();
		} else {
			throw new UserCancelledException();
		}
	}
	
	public static String queryForString(Component view, String queryCaption, String queryText,
			String defaultValue) throws UserCancelledException {
		StringQueryPanel queryPanel = new StringQueryPanel(queryText, defaultValue);
		InteractionResult choice = queryPanel.getUserChoice(view, queryCaption);
		if (choice == InteractionResult.FINISHED || choice == InteractionResult.CONTINUE) {
			return queryPanel.getResult();
		} else {
			throw new UserCancelledException();
		}
	}

	/**
	 * Displays a configuration dialog asking the user to enter a Integer, uses
	 * {@link Integer#parseInt(String)} to convert the user input.
	 * 
	 * @param context
	 * @param query
	 * @return
	 * @throws NumberFormatException
	 * @throws UserCancelledException
	 */
	public static int queryForInteger(UIPluginContext context, String query)
			throws NumberFormatException, UserCancelledException {
		return Integer.parseInt(queryForString(context, query));
	}
	
	public static int queryForInteger(Component view, String query)
			throws NumberFormatException, UserCancelledException {
		return Integer.parseInt(queryForString(view, query));
	}

	/**
	 * Displays a configuration dialog asking the user to enter a Float, uses
	 * {@link Float#parseFloat(String)} to convert the user input.
	 * 
	 * @param context
	 * @param query
	 * @return
	 * @throws NumberFormatException
	 * @throws UserCancelledException
	 */
	public static float queryForFloat(UIPluginContext context, String query)
			throws NumberFormatException, UserCancelledException {
		return Float.parseFloat(queryForString(context, query));
	}
	
	public static float queryForFloat(Component view, String query)
			throws NumberFormatException, UserCancelledException {
		return Float.parseFloat(queryForString(view, query));
	}

	/**
	 * Displays a configuration dialog asking the user to enter a Double, uses
	 * {@link Double#parseDouble(String)} to convert the user input.
	 * 
	 * @param context
	 * @param query
	 * @return
	 * @throws NumberFormatException
	 * @throws UserCancelledException
	 */
	public static double queryForDouble(UIPluginContext context, String query)
			throws NumberFormatException, UserCancelledException {
		return Double.parseDouble(queryForString(context, query));
	}
	
	public static double queryForDouble(Component view, String query)
			throws NumberFormatException, UserCancelledException {
		return Double.parseDouble(queryForString(view, query));
	}

	/**
	 * Displays a configuration dialog asking the user to select a double value
	 * between 0.0 and 1.0 (both inclusive). The default value is 1.0.
	 * 
	 * @param context
	 * @param query
	 * @return
	 * @throws NumberFormatException
	 * @throws UserCancelledException
	 */
	public static double queryForDoubleZeroOne(UIPluginContext context, String query) throws UserCancelledException {
		return queryForDouble(context, query, 0.0d, 1.0d, 1.0d);
	}
	
	public static double queryForDoubleZeroOne(Component view, String query) throws UserCancelledException {
		return queryForDouble(view, query, 0.0d, 1.0d, 1.0d);
	}

	/**
	 * Displays a configuration dialog asking the user to select a double value.
	 * 
	 * @param context
	 * @param query
	 * @return
	 * @throws UserCancelledException
	 */
	public static double queryForDouble(UIPluginContext context, String query, double min, double max,
			double defaultValue) throws UserCancelledException {
		SliderQueryPanel queryPanel = new SliderQueryPanel(query, min, max, defaultValue);
		InteractionResult choice = queryPanel.getUserChoice(context, query);
		if (choice == InteractionResult.FINISHED || choice == InteractionResult.CONTINUE) {
			return queryPanel.getResult();
		} else {
			throw new UserCancelledException();
		}
	}
	
	public static double queryForDouble(Component view, String query, double min, double max,
			double defaultValue) throws UserCancelledException {
		SliderQueryPanel queryPanel = new SliderQueryPanel(query, min, max, defaultValue);
		InteractionResult choice = queryPanel.getUserChoice(view, query);
		if (choice == InteractionResult.FINISHED || choice == InteractionResult.CONTINUE) {
			return queryPanel.getResult();
		} else {
			throw new UserCancelledException();
		}
	}

	/**
	 * Shows a dialog asking the user to enter an ordered list of integers
	 * separated by comma.
	 * 
	 * @param context
	 * @param query
	 * @return
	 * @throws NumberFormatException
	 * @throws UserCancelledException
	 */
	public static int[] queryForIntArray(UIPluginContext context, String query)
			throws NumberFormatException, UserCancelledException {
		String result = queryForString(context, query);
		if (result != null && result.length() > 0) {
			String[] traceIndexArray = result.split(",");
			int[] traceIndexSet = new int[traceIndexArray.length];
			for (int i = 0; i < traceIndexArray.length; i++) {
				String singleTraceIndex = traceIndexArray[i];
				traceIndexSet[i] = Integer.parseInt(singleTraceIndex);
			}
			return traceIndexSet;
		} else {
			throw new NumberFormatException("Invalid format, should be comma separated integers!");
		}
	}

	/**
	 * Shows a dialog with a combo box containing the choices, and returns the
	 * selected value.
	 * 
	 * @param context
	 * @param query
	 * @param choices
	 * @return
	 * @throws UserCancelledException
	 */
	public static <T> T queryForObject(UIPluginContext context, String query, T[] choices)
			throws UserCancelledException {
		return queryForObject(context, query, Arrays.asList(choices));
	}
	
	public static <T> T queryForObject(Component view, String query, T[] choices)
			throws UserCancelledException {
		return queryForObject(view, query, Arrays.asList(choices));
	}

	/**
	 * 
	 * Shows a dialog with a combo box containing the choices, and returns the
	 * selected value.
	 * 
	 * @param context
	 * @param query
	 * @param choices
	 * @return
	 * @throws UserCancelledException
	 */
	public static <T> T queryForObject(UIPluginContext context, String query, Iterable<T> choices)
			throws UserCancelledException {
		ChoiceQueryPanel<T> queryPanel = new ChoiceQueryPanel<>(choices);
		InteractionResult choice = queryPanel.getUserChoice(context, query);
		if (choice == InteractionResult.FINISHED || choice == InteractionResult.CONTINUE) {
			return queryPanel.getResult();
		} else {
			throw new UserCancelledException();
		}
	}
	
	public static <T> T queryForObject(Component view, String query, Iterable<T> choices)
			throws UserCancelledException {
		ChoiceQueryPanel<T> queryPanel = new ChoiceQueryPanel<>(choices);
		InteractionResult choice = queryPanel.getUserChoice(view, query);
		if (choice == InteractionResult.FINISHED || choice == InteractionResult.CONTINUE) {
			return queryPanel.getResult();
		} else {
			throw new UserCancelledException();
		}
	}

	/**
	 * Shows a dialog with a combo box containing the choices, and returns the
	 * selected value.
	 * 
	 * @param context
	 * @param query
	 * @param choices
	 * @return
	 * @throws UserCancelledException
	 */
	public static String queryForString(UIPluginContext context, String query, String[] choices)
			throws UserCancelledException {
		return queryForString(context, query, Arrays.asList(choices));
	}
	
	public static String queryForString(Component view, String query, String[] choices)
			throws UserCancelledException {
		return queryForString(view, query, Arrays.asList(choices));
	}

	/**
	 * Shows a dialog with a combo box containing the choices, and returns the
	 * selected value.
	 * 
	 * @param context
	 * @param query
	 * @param choices
	 * @return
	 * @throws UserCancelledException
	 */
	public static String queryForString(UIPluginContext context, String query, Iterable<String> choices)
			throws UserCancelledException {
		ChoiceQueryPanel<String> queryPanel = new ChoiceQueryPanel<>(choices);
		InteractionResult choice = queryPanel.getUserChoice(context, query);
		if (choice == InteractionResult.FINISHED || choice == InteractionResult.CONTINUE) {
			return queryPanel.getResult();
		} else {
			throw new UserCancelledException();
		}
	}
	
	public static String queryForString(Component view, String query, Iterable<String> choices)
			throws UserCancelledException {
		ChoiceQueryPanel<String> queryPanel = new ChoiceQueryPanel<>(choices);
		InteractionResult choice = queryPanel.getUserChoice(view, query);
		if (choice == InteractionResult.FINISHED || choice == InteractionResult.CONTINUE) {
			return queryPanel.getResult();
		} else {
			throw new UserCancelledException();
		}
	}

	/**
	 * Shows a dialog with a multi-line text area, and returns the entered
	 * value.
	 * 
	 * @param context
	 * @param queryCaption
	 * @return
	 * @throws UserCancelledException
	 */
	public static String queryForMultilineString(UIPluginContext context, String queryCaption)
			throws UserCancelledException {
		MultilineStringQueryPanel queryPanel = new MultilineStringQueryPanel();
		InteractionResult choice = queryPanel.getUserChoice(context, queryCaption);
		if (choice == InteractionResult.FINISHED || choice == InteractionResult.CONTINUE) {
			return queryPanel.getResult();
		} else {
			throw new UserCancelledException();
		}
	}

	/**
	 * Shows a dialog with a multi-line text area, and returns the entered
	 * value.
	 * 
	 * @param context
	 * @param queryCaption
	 *            that is shown on top of the dialog
	 * @param defaultText
	 *            that is shown in the text area
	 * @return
	 * @throws UserCancelledException
	 */
	public static String queryForMultilineString(UIPluginContext context, String queryCaption, String defaultText)
			throws UserCancelledException {
		MultilineStringQueryPanel queryPanel = new MultilineStringQueryPanel(defaultText);
		InteractionResult choice = queryPanel.getUserChoice(context, queryCaption);
		if (choice == InteractionResult.FINISHED || choice == InteractionResult.CONTINUE) {
			return queryPanel.getResult();
		} else {
			throw new UserCancelledException();
		}
	}

	/**
	 * Shows a dialog with a selection list that allow multi-selection.
	 * 
	 * @param context
	 * @param query
	 * @param choices
	 * @return
	 * @throws UserCancelledException
	 */
	public static <T> List<T> queryForObjects(UIPluginContext context, String query, T[] choices)
			throws UserCancelledException {
		return queryForObjects(context, query, Arrays.asList(choices));
	}
	
	public static <T> List<T> queryForObjects(Component view, String query, T[] choices)
			throws UserCancelledException {
		return queryForObjects(view, query, Arrays.asList(choices));
	}

	/**
	 * Shows a dialog with a selection list that allow multi-selection.
	 * 
	 * @param context
	 * @param query
	 * @param choices
	 * @return
	 * @throws UserCancelledException
	 */
	public static <T> List<T> queryForObjects(UIPluginContext context, String query, Iterable<T> choices)
			throws UserCancelledException {
		MultipleChoiceQueryPanel<T> queryPanel = new MultipleChoiceQueryPanel<>(query, choices);
		InteractionResult choice = queryPanel.getUserChoice(context, query);
		if (choice == InteractionResult.FINISHED || choice == InteractionResult.CONTINUE) {
			return queryPanel.getResult();
		} else {
			throw new UserCancelledException();
		}
	}

	public static <T> List<T> queryForObjects(Component view, String query, Iterable<T> choices)
			throws UserCancelledException {
		MultipleChoiceQueryPanel<T> queryPanel = new MultipleChoiceQueryPanel<>(query, choices);
		InteractionResult choice = queryPanel.getUserChoice(view, query);
		if (choice == InteractionResult.FINISHED || choice == InteractionResult.CONTINUE) {
			return queryPanel.getResult();
		} else {
			throw new UserCancelledException();
		}
	}

	/**
	 * Shows a dialog with a selection list that allow multi-selection.
	 * 
	 * @param context
	 * @param query
	 * @param choices
	 * @return
	 * @throws UserCancelledException
	 */
	public static List<String> queryForStrings(UIPluginContext context, String query, Iterable<String> choices)
			throws UserCancelledException {
		MultipleChoiceQueryPanel<String> queryPanel = new MultipleChoiceQueryPanel<>(query, choices);
		InteractionResult choice = queryPanel.getUserChoice(context, query);
		if (choice == InteractionResult.FINISHED || choice == InteractionResult.CONTINUE) {
			return queryPanel.getResult();
		} else {
			throw new UserCancelledException();
		}
	}

	public static List<String> queryForStrings(Component view, String query, Iterable<String> choices)
			throws UserCancelledException {
		MultipleChoiceQueryPanel<String> queryPanel = new MultipleChoiceQueryPanel<>(query, choices);
		InteractionResult choice = queryPanel.getUserChoice(view, query);
		if (choice == InteractionResult.FINISHED || choice == InteractionResult.CONTINUE) {
			return queryPanel.getResult();
		} else {
			throw new UserCancelledException();
		}
	}

	/**
	 * Shows a dialog with a selection list that allow multi-selection.
	 * 
	 * @param context
	 * @param query
	 * @param choices
	 * @return
	 * @throws UserCancelledException
	 */
	public static List<String> queryForStrings(UIPluginContext context, String query, String[] choices)
			throws UserCancelledException {
		return queryForStrings(context, query, Arrays.asList(choices));
	}

	/**
	 * Shows a dialog to the user with a two-column table. The first column
	 * contains the supplied keys. In the second column the user can enter the
	 * desired values. The mapping is returned as a Map.
	 * 
	 * @param context
	 * @param query
	 * @param keys
	 * @return
	 * @throws UserCancelledException
	 */
	public static <T> Map<T, String> queryMapToString(UIPluginContext context, String query, Iterable<T> keys)
			throws UserCancelledException {
		FreeMappingQueryPanel<T> queryPanel = new FreeMappingQueryPanel<>(query, keys, ImmutableMap.<T, String>of());
		InteractionResult choice = queryPanel.getUserChoice(context, query);
		if (choice == InteractionResult.FINISHED || choice == InteractionResult.CONTINUE) {
			return queryPanel.getResult();
		} else {
			throw new UserCancelledException();
		}
	}

	/**
	 * Shows a dialog to the user with a two-column table. The first column
	 * contains the supplied keys. In the second column the user can enter the
	 * desired values. The mapping is returned as a Map.
	 * 
	 * @param context
	 * @param query
	 * @param keys
	 * @return
	 * @throws UserCancelledException
	 */
	public static <T> Map<T, String> queryMapToString(UIPluginContext context, String query, T[] keys)
			throws UserCancelledException {
		return queryMapToString(context, query, Arrays.asList(keys));
	}

	/**
	 * Shows a dialog to the user with a two-column table. The first column
	 * contains the supplied keys. In the second column the user can enter the
	 * desired values. The mapping is returned as a Map.
	 * 
	 * @param context
	 * @param query
	 * @param keys
	 * @param defaultValues
	 * @return
	 * @throws UserCancelledException
	 */
	public static <T> Map<T, String> queryMapToString(UIPluginContext context, String query, Iterable<T> keys,
			Map<T, String> defaultValues) throws UserCancelledException {
		FreeMappingQueryPanel<T> queryPanel = new FreeMappingQueryPanel<>(query, keys, defaultValues);
		InteractionResult choice = queryPanel.getUserChoice(context, query);
		if (choice == InteractionResult.FINISHED || choice == InteractionResult.CONTINUE) {
			return queryPanel.getResult();
		} else {
			throw new UserCancelledException();
		}
	}

	/**
	 * Shows a dialog to the user with a two-column table. The first column
	 * contains the supplied keys. In the second column the user can enter the
	 * desired values. The mapping is returned as a Map.
	 * 
	 * @param context
	 * @param query
	 * @param keys
	 * @param defaultValues
	 * @return
	 * @throws UserCancelledException
	 */
	public static <T> Map<T, String> queryMapToString(UIPluginContext context, String query, T[] keys,
			Map<T, String> defaultValues) throws UserCancelledException {
		return queryMapToString(context, query, Arrays.asList(keys), defaultValues);
	}

	/**
	 * Shows a dialog to the user with a two-column table. The first column
	 * contains the supplied keys. In the second column the user can choose from
	 * a list of values. The mapping is returned as a Map.
	 * 
	 * @param context
	 * @param query
	 * @param keys
	 * @param values
	 * @return
	 * @throws UserCancelledException
	 */
	public static <S, T> Map<S, T> queryMapToObject(UIPluginContext context, String query, Iterable<S> keys,
			Iterable<T> values) throws UserCancelledException {
		return queryMapToObject(context, query, keys, values, ImmutableMap.<S, T>of());
	}

	/**
	 * Shows a dialog to the user with a two-column table. The first column
	 * contains the supplied keys. In the second column the user can choose from
	 * a list of values. The mapping is returned as a Map.
	 * 
	 * @param context
	 * @param query
	 * @param keys
	 * @param values
	 * @param defaultMap
	 * @return
	 * @throws UserCancelledException
	 */
	public static <S, T> Map<S, T> queryMapToObject(UIPluginContext context, String query, Iterable<S> keys,
			Iterable<T> values, Map<S, T> defaultMap) throws UserCancelledException {
		FixedMappingQueryPanel<S, T> queryPanel = new FixedMappingQueryPanel<S, T>(query, keys, values, defaultMap);
		InteractionResult choice = queryPanel.getUserChoice(context, query);
		if (choice == InteractionResult.FINISHED || choice == InteractionResult.CONTINUE) {
			return queryPanel.getResult();
		} else {
			throw new UserCancelledException();
		}
	}

}