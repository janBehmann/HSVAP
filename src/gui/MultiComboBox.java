package gui;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 * Creates multiple ComboBoxes, which are accessible through {@link #getComboBox(int)}. All ComboBoxes share 
 * the same list, but each item on the list, can be selected at max by one combo box at a time. 
 * @author Micha Strauch
 *
 * @param <T> Type of the values in the combo boxes
 */
public class MultiComboBox<T> {
	/** Number of combo boxes */
	private final int count;
	/** Array of all combo boxes, once created */
	private final JComboBox<T>[] combos;
	/** Data for each combo box, containing selection and listener */
	private final ComboData[] comboData;
	/** Elements for the combo boxes */
	private final List<T> data;
	/** Optional private Renderer for the elements of the list */
	private ListCellRenderer renderer;
	
	/** Creates an empty MultiComboBox with <i>count</i> boxes */
	public MultiComboBox(int count){
		this(count, Collections.<T>emptyList());
	}
	
	/** Creates a filled MultiComboBox with <i>count</i> boxes */
	public MultiComboBox(int count, Collection<T> data) {
		this(count, data, false);
	}
	
	/** Creates a filled MultiComboBox with <i>count</i> boxes. Optional adding support for selecting nothing */
	@SuppressWarnings("unchecked")
	public MultiComboBox(int count, Collection<T> data, boolean none){
		this.count = count;
		this.combos = new JComboBox[count];
		if(none){
			this.data = new ArrayList<T>(data.size() + 1);
			this.data.add(null);
			this.data.addAll(data);
		} else
			this.data = new ArrayList<T>(data);
		this.comboData = new ComboData[count];
		for(int i = 0; i < count; i++)
			comboData[i] = new ComboData();
	}
	
	/** Renderer to be used to create components for the combo boxes */
	public void setRenderer(ListCellRenderer renderer){
		this.renderer = renderer;
	}
	
	/** Sets data and optional the selected items */
	public void initialize(Collection<T> data, T... selected){
		initialize(data, false, selected);
	}
	
	/** Sets data and optional the selected items. <i>none</i> specifies if it's possible to select nothing */
	public void initialize(Collection<T> data, boolean none, T... selected){
		this.data.clear();
		if(none)
			this.data.add(null);
		this.data.addAll(data);
		
		for(int i = 0; i < count && i < selected.length; i++){
			int index = ComboData.NONE;
			if(selected[i] != null)
				index = this.data.indexOf(selected[i]);
			comboData[i].selection = index;
		}
		
		fireEvent();
	}
	
	/** Returns the number of combo boxes */
	public int getCount() {
		return count;
	}
	
	/** Returns the combo box for given id<br>0 &le; id < count*/
	@SuppressWarnings("unchecked")
	public JComboBox<T> getComboBox(final int id){
		if(combos[id] == null){
			combos[id] = new JComboBox<T>(
				new ComboBoxModel<T>() {
					@Override
					public void addListDataListener(ListDataListener l) {
						MultiComboBox.this.addListDataListener(l, id);
					}
					@Override
					public T getElementAt(int index) {
						return MultiComboBox.this.getElementAt(index, id);
					}
					@Override
					public Object getSelectedItem() {
						return MultiComboBox.this.getSelectedItem(id);
					}
					@Override
					public int getSize() {
						return MultiComboBox.this.getSize(id);
					}
					@Override
					public void removeListDataListener(ListDataListener l) {
						MultiComboBox.this.removeListDataListener(l, id);
					}
					@Override
					public void setSelectedItem(Object anItem) {
						MultiComboBox.this.setSelectedItem(anItem, id);
					}
				}
			);
			combos[id].setRenderer(new BasicComboBoxRenderer(){
				private static final long serialVersionUID = -7887927197907429531L;

				@Override
				public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					if(value == null)
						value = "- NONE -";
					
					Component comp;
					if(renderer != null)
						comp = renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					else
						comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
						
					if(!canSelect(index, id)){
						comp.setBackground(Color.LIGHT_GRAY);
						comp.setForeground(Color.DARK_GRAY);
					}
					return comp;
				}
			});
		}
		return combos[id];
	}
	
	/** Checks if given id can select item at given index */
	private boolean canSelect(int index, int id){
		for(int i = 0; i < count; i++)
			if(i != id && comboData[i].selection == index)
				return false;
		return true;
	}
	
	/** Adds a listener to the given id */
	private void addListDataListener(ListDataListener l, int id){
		comboData[id].listener.add(l);
	}
	
	/** Returns the element at given index for given id */
	private T getElementAt(int index, int id){
		return data.get(index);
	}
	
	/** Returns the currently selected item for the given id */ 
	public T getSelectedItem(int id) {
		if(comboData[id].selection == ComboData.NONE)
			return null;
		return data.get(comboData[id].selection);
	}
	
	/** Returns the list size for the given id */
	private int getSize(int id){
		return data.size();
	}
	
	/** Removes the given listener from the given id */
	private void removeListDataListener(ListDataListener l, int id){
		comboData[id].listener.remove(l);
	}
	
	/** Sets the selected item for the given id, if possible<br>null = select nothing */
	private void setSelectedItem(Object anItem, int id){
		if(anItem == null)
			comboData[id].selection = ComboData.NONE;
		else {
			int index = data.indexOf(anItem);
			if(canSelect(index, id))
				comboData[id].selection = index;
		}
		
		// Update others
		fireEvent();
	}
	
	/** Updates all listeners */
	private void fireEvent(){
		for(int i = 0; i < count; i++){
			if(combos[i] == null)
				continue;
			// Could be done more efficiently...
			ListDataEvent event = new ListDataEvent(combos[i], ListDataEvent.CONTENTS_CHANGED, 0, getSize(i));
			for(ListDataListener listener : comboData[i].listener)
				listener.contentsChanged(event);
		}
	}
	
	/**
	 * Data for each combo box
	 * @author Micha Strauch
	 */
	private static class ComboData {
		public static final int NONE = -1;
		/** Listener for this combo box */
		public final Set<ListDataListener> listener = new HashSet<ListDataListener>();
		/** Currently selected item */
		public int selection = NONE;
	}

}
