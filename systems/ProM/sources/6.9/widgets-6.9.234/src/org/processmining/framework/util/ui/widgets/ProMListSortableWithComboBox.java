package org.processmining.framework.util.ui.widgets;
import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.TransferHandler;

import com.fluxicon.slickerbox.components.SlickerButton;


/**
 * JList with ProM look & feel with a {@link ProMComboBox} on top of it that
 * allows to add elements to the list.
 */
public class ProMListSortableWithComboBox<T> extends JPanel {

	private static class DragListenerImpl<T> implements DragSourceListener, DragGestureListener {

		private final JList<T> list;
		private final DragSource ds = new DragSource();

		public DragListenerImpl(JList<T> list) {
			this.list = list;
			ds.createDefaultDragGestureRecognizer(list, DnDConstants.ACTION_MOVE, this);
		}

		public void dragGestureRecognized(DragGestureEvent dge) {
			StringSelection transferable = new StringSelection(Integer.toString(list.getSelectedIndex()));
			ds.startDrag(dge, DragSource.DefaultCopyDrop, transferable, this);
		}

		public void dragEnter(DragSourceDragEvent dsde) {
		}

		public void dragExit(DragSourceEvent dse) {
		}

		public void dragOver(DragSourceDragEvent dsde) {
		}

		public void dragDropEnd(DragSourceDropEvent dsde) {
		}

		public void dropActionChanged(DragSourceDragEvent dsde) {
		}
	}

	private static class DropHandlerImpl<T> extends TransferHandler {

		private static final long serialVersionUID = -3468373344687124791L;

		private final DefaultListModel<T> listModel;

		public DropHandlerImpl(DefaultListModel<T> listModel) {
			this.listModel = listModel;
		}

		public boolean canImport(TransferHandler.TransferSupport support) {
			if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				return false;
			}
			JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
			if (dl.getIndex() == -1) {
				return false;
			} else {
				return true;
			}
		}

		public boolean importData(TransferHandler.TransferSupport support) {
			if (!canImport(support)) {
				return false;
			}

			Transferable transferable = support.getTransferable();
			String indexString;
			try {
				indexString = (String) transferable.getTransferData(DataFlavor.stringFlavor);
			} catch (Exception e) {
				return false;
			}

			int sourceIndex = Integer.parseInt(indexString);
			JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
			int dropTargetIndex = dl.getIndex();
			T element = listModel.remove(sourceIndex);
			if (sourceIndex < dropTargetIndex) {
				listModel.insertElementAt(element, dropTargetIndex - 1);
			} else {
				listModel.insertElementAt(element, dropTargetIndex);
			}
			return true;
		}
	}

	private static final long serialVersionUID = -3989998064589278170L;

	private final JLabel selectedItemsText;
	private final JList<T> list;
	private final ProMComboBox<T> comboBox;
	private final DefaultListModel<T> listModel;
	private boolean isMultiSelection = false;
	
	public ProMListSortableWithComboBox(ComboBoxModel<T> comboBoxModel) {
		super();
		setOpaque(false);
		JPanel controlPanel = new JPanel();
		controlPanel.setOpaque(false);
		controlPanel.setLayout(new BorderLayout());
		comboBox = new ProMComboBox<>(comboBoxModel, true);
		comboBox.addActionListener(new ActionListener() {
			
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				if (!isMultiSelection) {
					listModel.clear();
					listModel.addElement((T) comboBox.getSelectedItem());
				}
			}
		});
		controlPanel.add(comboBox, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setOpaque(false);
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		
		JButton addButton = new SlickerButton("+");
		addButton.setAlignmentY(CENTER_ALIGNMENT);
		addButton.addActionListener(new ActionListener() {
			
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				isMultiSelection = true;
				listModel.addElement((T) comboBox.getSelectedItem());
			}
		});
		buttonPanel.add(addButton);
		JButton removeButton = new SlickerButton("-");
		removeButton.setAlignmentY(CENTER_ALIGNMENT);
		removeButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				isMultiSelection = true;
				if (list.getSelectedIndex() == -1) {
					// Just remove the last one
					if (list.getModel().getSize() >= 1) {
						listModel.remove(list.getModel().getSize()-1);
					}
				} else {
					listModel.removeElement(list.getSelectedValue());	
				}				
			}
		});
		buttonPanel.add(removeButton);
		controlPanel.add(buttonPanel, BorderLayout.EAST);

		listModel = new DefaultListModel<>();
		list = new JList<>(listModel);
		list.addKeyListener(new KeyListener() {

			public void keyTyped(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
					for (int i : list.getSelectedIndices()) {
						listModel.remove(i);
					}
				}
			}

			public void keyPressed(KeyEvent e) {
			}

		});
		list.setDragEnabled(true);
		list.setDropMode(DropMode.INSERT);
		list.setTransferHandler(new DropHandlerImpl<>(listModel));
		list.setBackground(WidgetColors.COLOR_LIST_BG);
		list.setForeground(WidgetColors.COLOR_LIST_FG);
		list.setSelectionBackground(WidgetColors.COLOR_LIST_SELECTION_BG);
		list.setSelectionForeground(WidgetColors.COLOR_LIST_SELECTION_FG);

		final ProMScrollPane scroller = new ProMScrollPane(list);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		new DragListenerImpl<>(list);
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setOpaque(false);
		selectedItemsText = new JLabel("Selected items");
		getSelectedItemsText().setAlignmentX(CENTER_ALIGNMENT);
		topPanel.add(controlPanel, BorderLayout.CENTER);
		topPanel.add(getSelectedItemsText(), BorderLayout.SOUTH);
		
		setLayout(new BorderLayout());
		add(topPanel, BorderLayout.NORTH);
		add(scroller, BorderLayout.CENTER);
	}

	public JList<T> getList() {
		return list;
	}

	public ProMComboBox<T> getComboBox() {
		return comboBox;
	}

	/**
	 * @return the ordered elements currently in the {@link JList}
	 */
	public List<T> getElements() {
		ArrayList<T> elements = new ArrayList<>();
		for (int i = 0; i < listModel.getSize(); i++) {
			elements.add(listModel.get(i));
		}
		return elements;
	}

	public void addElement(T element) {
		listModel.addElement(element);
	}

	public ListModel<T> getListModel() {
		return listModel;
	}

	public JLabel getSelectedItemsText() {
		return selectedItemsText;
	}

}