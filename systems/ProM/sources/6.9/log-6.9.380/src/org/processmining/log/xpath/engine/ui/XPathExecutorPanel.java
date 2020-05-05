package org.processmining.log.xpath.engine.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.deckfour.xes.extension.std.XIdentityExtension;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.framework.util.ui.widgets.ProMHeaderPanel;
import org.processmining.framework.util.ui.widgets.ProMSplitPane;
import org.processmining.framework.util.ui.widgets.ProMTable;
import org.processmining.framework.util.ui.widgets.ProMTextArea;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.log.xes.extensions.id.IdentitiesMissingException;
import org.processmining.log.xpath.engine.XPathEngine;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import net.sf.saxon.s9api.SaxonApiException;

public class XPathExecutorPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8961837431111952562L;

	private final XLog _log;
	private final PluginContext _context;

	private XPathEngine _engine;

	private ProMSplitPane splitter;
	private ProMTextArea errorText;
	private JPanel topPanel;
	private JPanel queryPanel;
	private ProMTextField queryText;
	private ProMTextField relQueryText;
	private JPanel buttonPanel;
	private JButton queryButton;
	private JButton clearButton;
	private JLabel resultCounter;

	private JPanel queryHistory;
	private JButton prevQuery;
	private JButton nextQuery;

	private ProMTable table;

	private Stack<Pair<String, String>> queryPast = new Stack<Pair<String, String>>();
	private Stack<Pair<String, String>> queryFuture = new Stack<Pair<String, String>>();

	private JPanel bottomPanel;
	private ProMSplitPane bottomSplitter;
	private JPanel bottomRight;

	private Logger logging;

	public XPathExecutorPanel(final PluginContext context, final XLog log)
			throws IdentitiesMissingException, IOException {
		_log = log;
		_context = context;

		_engine = new XPathEngine(_context, _log);

		logging = Logger.getLogger("XPathExecutor");
		logging.setLevel(Level.FINEST);

		initializeUI();
	}

	public XLog getLog() {
		return _log;
	}

	public PluginContext getContext() {
		return _context;
	}

	protected XPathEngine getEngine() {
		return _engine;
	}

	private void initializeUI() {
		this.setLayout(new BorderLayout());

		setTopPanel();
		setBottomPanel();
		setSplitter();
	}

	private void setTopPanel() {
		topPanel = new JPanel(new BorderLayout());

		setQueryPanel();
		topPanel.add(queryPanel, BorderLayout.CENTER);

		buttonPanel = new JPanel();
		queryButton = SlickerFactory.instance().createButton("Query");
		queryButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				executeQuery(queryText.getText(), relQueryText.getText());
			}
		});

		clearButton = SlickerFactory.instance().createButton("Clear");
		clearButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				executeClear();
			}
		});

		resultCounter = SlickerFactory.instance().createLabel("");

		buttonPanel.add(queryButton);
		buttonPanel.add(clearButton);
		buttonPanel.add(resultCounter);
		topPanel.add(buttonPanel, BorderLayout.SOUTH);
	}

	private void setBottomPanel() {

		bottomPanel = new JPanel(new BorderLayout());

		bottomRight = new JPanel(new BorderLayout());
		errorText = new ProMTextArea(false);

		bottomSplitter = new ProMSplitPane(ProMSplitPane.HORIZONTAL_SPLIT);
		bottomSplitter.setLeftComponent(errorText);
		bottomSplitter.setRightComponent(bottomRight);

		bottomPanel.add(bottomSplitter);
	}

	private void setSplitter() {

		splitter = new ProMSplitPane(ProMSplitPane.VERTICAL_SPLIT);
		splitter.setTopComponent(topPanel);
		splitter.setBottomComponent(bottomPanel);
		add(splitter, BorderLayout.CENTER);
	}

	private void setQueryPanel() {
		queryPanel = new JPanel(new BorderLayout());

		JPanel qr = new JPanel(new BorderLayout());

		queryText = new ProMTextField();
		queryText.setText("//trace");
		relQueryText = new ProMTextField();

		InputMethodListener l = new InputMethodListener() {

			public void inputMethodTextChanged(InputMethodEvent event) {
				if (event.getCommittedCharacterCount() > 0) {
					resetQueryFuture();
				}
			}

			public void caretPositionChanged(InputMethodEvent event) {
			}
		};

		queryText.addInputMethodListener(l);
		relQueryText.addInputMethodListener(l);

		qr.add(queryText, BorderLayout.NORTH);
		qr.add(relQueryText, BorderLayout.SOUTH);

		queryPanel.add(qr, BorderLayout.CENTER);

		queryHistory = new JPanel(new BorderLayout());

		prevQuery = SlickerFactory.instance().createButton("<");
		prevQuery.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setPrevQuery();
			}
		});

		nextQuery = SlickerFactory.instance().createButton(">");
		nextQuery.setEnabled(false);
		nextQuery.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setNextQuery();
			}
		});

		queryHistory.add(prevQuery, BorderLayout.NORTH);
		queryHistory.add(nextQuery, BorderLayout.SOUTH);

		queryPanel.add(queryHistory, BorderLayout.EAST);
	}

	private boolean queryChanged = false;

	public boolean isQueryChanged() {
		return queryChanged;
	}

	private void setPrevQuery() {

		if (!queryPast.isEmpty()) {
			queryFuture.push(new Pair<String, String>(queryText.getText(), relQueryText.getText()));

			Pair<String, String> pop = queryPast.pop();
			queryText.setText(pop.getFirst());
			relQueryText.setText(pop.getSecond());
		}

		prevQuery.setEnabled(!(queryPast.isEmpty()));
		nextQuery.setEnabled(true);

		queryChanged = false;
	}

	private void setNextQuery() {
		if (!queryFuture.isEmpty()) {
			queryPast.push(new Pair<String, String>(queryText.getText(), relQueryText.getText()));

			Pair<String, String> pop = queryFuture.pop();
			queryText.setText(pop.getFirst());
			relQueryText.setText(pop.getSecond());
		}

		nextQuery.setEnabled(!(queryFuture.isEmpty()));
		prevQuery.setEnabled(true);

		queryChanged = false;
	}

	private void resetQueryFuture() {
		queryFuture.clear();
		nextQuery.setEnabled(false);
	}

	private void executeClear() {
		queryText.setText("");
		relQueryText.setText("");
		errorText.setText("");
		resultCounter.setText("");
		bottomSplitter.setLeftComponent(errorText);
		bottomSplitter.setRightComponent(bottomRight);

		repaint();
	}

	private void executeQuery(String query, String relQuery) {
		queryPast.add(new Pair<String, String>(query, relQuery));

		long start = Calendar.getInstance().getTimeInMillis();
		logging.log(Level.INFO, "start query '" + query + "'");

		//get solution
		SolutionTable solTable = null;

		String error = "";

		if (relQuery.equals("")) {
			List<XAttributable> solution = null;
			try {
				solution = getEngine().query(query, true);
			} catch (SaxonApiException e) {
				error = e.getMessage();
			}

			if (solution != null) {

				solTable = new SolutionTable(false);
				solTable.setSingleSolution(solution);

			}

		} else {

			List<Pair<XAttributable, XAttributable>> solution = null;
			try {
				solution = getEngine().query(query, relQuery, true);
			} catch (SaxonApiException e) {
				error = e.getMessage();
			}

			if (solution != null) {
				solTable = new SolutionTable(true);
				solTable.setPairSolution(solution);
			}
		}

		long end = Calendar.getInstance().getTimeInMillis();
		logging.log(Level.INFO, "end query. Duration: '" + (end - start) + " ms");

		if (solTable == null) {

			errorText.setText(error);
			resultCounter.setText("Errors found");
			bottomSplitter.setLeftComponent(errorText);
			bottomSplitter.setRightComponent(bottomRight);

		} else {
			//populate result table
			table = new ProMTable(solTable);
			table.getTable().setDefaultRenderer(XAttributable.class, new XAttributableRenderer());

			table.getTable().addMouseListener(new MouseListener() {

				public void mouseReleased(MouseEvent e) {
				}

				public void mousePressed(MouseEvent e) {
				}

				public void mouseExited(MouseEvent e) {
				}

				public void mouseEntered(MouseEvent e) {
				}

				public void mouseClicked(MouseEvent e) {
					int row = table.getTable().rowAtPoint(e.getPoint());
					int col = table.getTable().columnAtPoint(e.getPoint());

					if ((row >= 0 && row <= table.getTable().getRowCount())
							&& (col >= 0 && col <= table.getTable().getColumnCount())) {
						setPropertiesTable(table.getValueAt(row, col));
					}
				}
			});

			bottomSplitter.setLeftComponent(table);
			bottomSplitter.setRightComponent(bottomRight);
			resultCounter.setText("Solutions found: " + solTable.getRowCount());
		}

		repaint();
	}

	private void setPropertiesTable(Object value) {
		if (value instanceof XAttributable) {
			XAttributable parent = (XAttributable) value;

			ProMHeaderPanel php;
			if (value instanceof XAttribute) {
				XAttribute attr = (XAttribute) parent;
				php = new ProMHeaderPanel("Attribute: " + attr.getKey() + " (value: " + attr.toString() + ")");
			} else {
				php = new ProMHeaderPanel(value.getClass().getSimpleName());
			}

			ProMTable propTable = new ProMTable(new PropertiesTable(parent));
			propTable.getTable().setDefaultRenderer(XAttribute.class, new XAttributeRender());
			php.add(propTable);
			bottomSplitter.setRightComponent(php);
		}
	}

	private class PropertiesTable implements TableModel {

		private XAttributeMap attrMap;
		private List<String> keys;

		public PropertiesTable(XAttributable parent) {

			attrMap = parent.getAttributes();
			keys = new ArrayList<String>(attrMap.keySet());

		}

		public int getRowCount() {
			return keys.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
				case 0 :
					return "Attribute";
				case 1 :
					return "Value";
				default :
					return "ERROR FIELD";
			}
		}

		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
				case 0 :
					return String.class;
				case 1 :
					return XAttribute.class;
				default :
					return null;
			}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 0 :
					return keys.get(rowIndex);
				case 1 :
					return attrMap.get(keys.get(rowIndex));
			}
			return null;
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		}

		public void addTableModelListener(TableModelListener l) {
		}

		public void removeTableModelListener(TableModelListener l) {
		}

	}

	private class XAttributeRender extends DefaultTableCellRenderer {

		private static final long serialVersionUID = -4820042181333426306L;

		public void setValue(Object value) {
			if (value == null) {
				super.setValue("Not set");
			} else {
				if (value instanceof XAttribute) {
					super.setValue(value);
				} else {
					super.setValue(value);
				}
			}
		}

	}

	private class XAttributableRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = -4649231822102709523L;

		public void setValue(Object value) {
			if (value instanceof XAttributable) {
				super.setValue(XIdentityExtension.instance().extractID((XAttributable) value));
			} else {
				super.setValue(value);
			}
		}
	}

	private class SolutionTable implements TableModel {

		private List<Pair<XAttributable, XAttributable>> pairSolution;

		private boolean pairs;

		List<XAttributable> sol;

		public SolutionTable(boolean pairs) {
			this.pairs = pairs;
		}

		public void setPairSolution(List<Pair<XAttributable, XAttributable>> solution) {
			pairSolution = solution;
		}

		public void setSingleSolution(List<XAttributable> solution) {
			sol = solution;
		}

		public int getRowCount() {
			if (pairs)
				return pairSolution.size();
			else
				return sol.size();
		}

		public int getColumnCount() {
			if (pairs) {
				return 2;
			} else {
				return 1;
			}
		}

		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
				case 0 :
					return "First";
				case 1 :
					return "Second";
				default :
					return "Wrong";
			}
		}

		public Class<?> getColumnClass(int columnIndex) {
			return XAttributable.class;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			XAttributable attr = null;

			if (pairs) {
				Pair<XAttributable, XAttributable> item = pairSolution.get(rowIndex);

				if (item == null) {
					return null;
				}

				switch (columnIndex) {
					case 0 :
						attr = item.getFirst();
						break;
					case 1 :
						attr = item.getSecond();
						break;
				}

			} else {
				attr = sol.get(rowIndex);
			}
			return attr;

			//if (attr == null)
			//	return null;
			//else
			//	return XIdentityExtension.instance().extractID(attr);
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		}

		public void addTableModelListener(TableModelListener l) {
		}

		public void removeTableModelListener(TableModelListener l) {
		}

	}

}
