/*
 * Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)
 * 
 * The Cytoscape Consortium is: - Institute for Systems Biology - University of
 * California San Diego - Memorial Sloan-Kettering Cancer Center - Institut
 * Pasteur - Agilent Technologies
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and the Institute for Systems Biology and the
 * Whitehead Institute have no obligations to provide maintenance, support,
 * updates, enhancements or modifications. In no event shall the Institute for
 * Systems Biology and the Whitehead Institute be liable to any party for
 * direct, indirect, special, incidental or consequential damages, including
 * lost profits, arising out of the use of this software and its documentation,
 * even if the Institute for Systems Biology and the Whitehead Institute have
 * been advised of the possibility of such damage. See the GNU Lesser General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package csplugins.id.mapping.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

/**
 * ComboBox containing checkbox
 * 
 * @author gjj
 * 
 * Added configureable colors
 * 
 * @author Massimiliano de Leoni
 */
@SuppressWarnings("rawtypes")
public class CheckComboBox extends JComboBox/* <Object> */{
	// checkbox renderer for combobox
	class CheckBoxRenderer implements ListCellRenderer {
		private final List<ObjCheckBox> cbs;
		private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		private final javax.swing.JSeparator separator;

		//private final Set objs;

		public CheckBoxRenderer(final List<ObjCheckBox> cbs) {
			//setOpaque(true);
			this.cbs = cbs;
			//this.objs = objs;
			separator = new javax.swing.JSeparator(SwingConstants.HORIZONTAL);
		}

		@Override
		public Component getListCellRendererComponent(final JList list, final Object value, final int index,
				final boolean isSelected, final boolean cellHasFocus) {
			if (index > 0 && index <= cbs.size()) {
				final ObjCheckBox cb = cbs.get(index - 1);
				if (cb.getObj() == nullObject) {
					return separator;
				}

				cb.setBackground(isSelected ? backgroundSelected : backgroundNotSelected);
				cb.setForeground(isSelected ? foregroundSelected : foregroundNotSelected);

				return cb;
			}

			String str;
			final Collection objs = getSelectedItems();
			final Vector<String> strs = new Vector<String>();
			if (objs == null) {
				str = "Please select one or more ID types";
			} else {
				for (final Object obj : objs) {
					strs.add(obj.toString());
				}
				str = strs.toString();
			}

			Component comp=defaultRenderer.getListCellRendererComponent(list, str, index, isSelected, cellHasFocus);	
			return comp;
		}
	}

	class ObjCheckBox extends JCheckBox {
		/**
	 * 
	 */
		private static final long serialVersionUID = 1L;
		private final Object obj;

		public ObjCheckBox(final Object obj) {
			super(obj.toString());
			this.obj = obj;
		}

		public Object getObj() {
			return obj;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<ObjCheckBox> cbs;

	private final List<CheckComboBoxSelectionChangedListener> changedListeners = new Vector<CheckComboBoxSelectionChangedListener>();

	private Map<Object, Boolean> mapObjSelected;

	private final Object nullObject = new Object();
	
	private Color backgroundSelected=Color.BLUE;
	private Color backgroundNotSelected=Color.WHITE;
	private Color foregroundSelected=Color.WHITE;
	private Color foregroundNotSelected=Color.BLACK;

	/**
	 * @param objs
	 */
	public CheckComboBox(final Collection<?> objs) {
		this(objs, false);
	}

	/**
	 * @param objs
	 * @param selected
	 */
	public CheckComboBox(final Collection<?> objs, final boolean selected) {
		resetObjs(objs, selected);
	}

	/**
	 * @param objs
	 * @param selected
	 */
	public CheckComboBox(final Collection<?> objs, final Collection selected) {
		mapObjSelected = new LinkedHashMap<Object, Boolean>();
		for (Object obj : objs) {
			if (obj == null) {
				obj = nullObject;
			}
			mapObjSelected.put(obj, selected.contains(obj));
		}

		reset();
	}

	/**
	 * @param mapObjSelected
	 */
	public CheckComboBox(final Map<Object, Boolean> mapObjSelected) {
		this.mapObjSelected = mapObjSelected;
		reset();
	}

	/**
	 * @param objs
	 */
	public CheckComboBox(final Object[] objs) {
		this(objs, false);
	}

	/**
	 * @param objs
	 * @param selected
	 */
	public CheckComboBox(final Object[] objs, final boolean selected) {
		resetObjs(objs, selected);
	}

	/**
	 * @see javax.swing.JComboBox#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		final int sel = getSelectedIndex();

		if (sel == 0) {
			getUI().setPopupVisible(this, false);
		} else if (sel > 0) {
			checkBoxSelectionChanged(sel - 1);
			for (final CheckComboBoxSelectionChangedListener l : changedListeners) {
				l.selectionChanged(sel - 1);
			}
		}

		setSelectedIndex(-1); // clear selection
	}

	/**
	 * @param c
	 */
	public void addSelectedItems(final Collection c) {
		if (c == null) {
			return;
		}

		for (final Object obj : c) {
			if (mapObjSelected.containsKey(obj)) {
				mapObjSelected.put(obj, true);
			}
		}

		reset();
		repaint();
	}

	/**
	 * @param objs
	 */
	public void addSelectedItems(final Object[] objs) {
		if (objs == null) {
			return;
		}

		for (final Object obj : objs) {
			if (mapObjSelected.containsKey(obj)) {
				mapObjSelected.put(obj, true);
			}
		}

		reset();
		repaint();
	}

	/**
	 * @param l
	 */
	public void addSelectionChangedListener(final CheckComboBoxSelectionChangedListener l) {
		if (l == null) {
			return;
		}
		changedListeners.add(l);
	}

	/**
	 * 
	 */
	@SuppressWarnings({ "unchecked" })
	public void clearSelection() {
		for (final Object key : new ArrayList(mapObjSelected.keySet())) {
			mapObjSelected.put(key, false);
		}

		reset();
		repaint();
	}

	/**
	 * @return
	 */
	public Collection getSelectedItems() {
		final Set<Object> ret = new TreeSet<Object>(); // alphabetically
		for (final Map.Entry<Object, Boolean> entry : mapObjSelected.entrySet()) {
			final Object obj = entry.getKey();
			final Boolean selected = entry.getValue();

			if (selected) {
				ret.add(obj);
			}
		}

		if (ret.isEmpty()) {
			return null;
		}

		return ret;
	}

	/**
	 * @param l
	 */
	public void removeSelectionChangedListener(final CheckComboBoxSelectionChangedListener l) {
		changedListeners.remove(l);
	}

	/**
	 * @param objs
	 * @param selected
	 */
	public void resetObjs(final Collection<?> objs, final boolean selected) {
		mapObjSelected = new LinkedHashMap<Object, Boolean>();
		for (final Object obj : objs) {
			mapObjSelected.put(obj, selected);
		}

		reset();
	}

	/**
	 * @param objs
	 * @param selected
	 */
	public void resetObjs(final Object[] objs, final boolean selected) {
		mapObjSelected = new LinkedHashMap<Object, Boolean>();
		for (final Object obj : objs) {
			mapObjSelected.put(obj, selected);
		}

		reset();
	}

	/**
	 * @see javax.swing.JComboBox#setPopupVisible(boolean)
	 */
	@Override
	public void setPopupVisible(final boolean flag) {
		//TODO this not work, fix it
		// Not code here prevents the populist from closing
	}

	private void checkBoxSelectionChanged(final int index) {
		final int n = cbs.size();
		if (index < 0 || index >= n) {
			return;
		}

		//Set selectedObj = getSelected();
		if (index < n - 2) {
			final ObjCheckBox cb = cbs.get(index);
			if (cb.getObj() == nullObject) {
				return;
			}

			if (cb.isSelected()) {
				cb.setSelected(false);
				mapObjSelected.put(cb.getObj(), false);

				cbs.get(n - 2).setSelected(false); //Select all
				cbs.get(n - 1).setSelected(getSelectedItems() == null); // select none
			} else {
				cb.setSelected(true);
				mapObjSelected.put(cb.getObj(), true);

				final Collection sobjs = getSelectedItems();
				cbs.get(n - 2).setSelected(sobjs != null && sobjs.size() == n - 2); // Select all
				cbs.get(n - 1).setSelected(false); // select none
			}
		} else if (index == n - 2) {
			for (final Object obj : mapObjSelected.keySet()) {
				if (obj != nullObject) {
					mapObjSelected.put(obj, true);
				}
			}

			for (int i = 0; i < n - 1; i++) {
				if (cbs.get(i) != nullObject) {
					cbs.get(i).setSelected(true);
				}
			}
			cbs.get(n - 1).setSelected(false);
		} else { // if (index==n-1)
			for (final Object obj : mapObjSelected.keySet()) {
				mapObjSelected.put(obj, false);
			}

			for (int i = 0; i < n - 1; i++) {
				cbs.get(i).setSelected(false);
			}
			cbs.get(n - 1).setSelected(true);
		}

	}

	private void initCBs() {
		cbs = new Vector<ObjCheckBox>();

		boolean selectedAll = true;
		boolean selectedNone = true;

		ObjCheckBox cb;
		for (final Map.Entry<Object, Boolean> entry : mapObjSelected.entrySet()) {
			final Object obj = entry.getKey();
			final Boolean selected = entry.getValue();

			if (selected) {
				selectedNone = false;
			} else {
				selectedAll = false;
			}

			cb = new ObjCheckBox(obj);
			cb.setSelected(selected);
			cbs.add(cb);
		}

		cb = new ObjCheckBox("Select all");
		cb.setSelected(selectedAll);
		cbs.add(cb);

		cb = new ObjCheckBox("Select none");
		cb.setSelected(selectedNone);
		cbs.add(cb);
	}

	@SuppressWarnings("unchecked")
	private void reset() {
		removeAllItems();

		initCBs();

		addItem(new String());
		for (final JCheckBox cb : cbs) {
			addItem(cb);
		}

		setRenderer(new CheckBoxRenderer(cbs));
		addActionListener(this);
	}

	public Color getBackgroundSelected() {
		return backgroundSelected;
	}

	public Color getBackgroundNotSelected() {
		return backgroundNotSelected;
	}

	public Color getForegroundSelected() {
		return foregroundSelected;
	}

	public Color getForegroundNotSelected() {
		return foregroundNotSelected;
	}

	public void setBackgroundSelected(Color backgroundSelected) {
		this.backgroundSelected = backgroundSelected;
	}

	public void setBackgroundNotSelected(Color backgroundNotSelected) {
		this.backgroundNotSelected = backgroundNotSelected;
	}

	public void setForegroundSelected(Color foregroundSelected) {
		this.foregroundSelected = foregroundSelected;
	}

	public void setForegroundNotSelected(Color foregroundNotSelected) {
		this.foregroundNotSelected = foregroundNotSelected;
	}

}

interface CheckComboBoxSelectionChangedListener extends java.util.EventListener {
	public void selectionChanged(int idx);
}
