package org.wrowclif.recipebox.ui.components;

import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;

public class DynamicLoadAdapter<T> extends BaseAdapter implements Filterable {

	private static final int MAX_COUNT = 5;

	private List<T> data;
	private ListFilter filter;
	private Specifics<T> specifics;
	public final OnItemClickListener onClick;

	public DynamicLoadAdapter(Specifics<T> sp) {
		super();
		this.specifics = sp;
		this.filter = new ListFilter();
		this.data = new ArrayList<T>();
		this.data.add(null);

		onClick = new SpecificsClick();
	}

	public void setUpList(ListView lv) {
		lv.setAdapter(this);
		lv.setOnItemClickListener(this.onClick);
	}

	public View getView(int position, View v, ViewGroup vg) {

		if(data.get(position) == null) {
			filter.filter((data.size() - 1) + "");
		}
		return specifics.getView(position, data.get(position), v, vg);
	}

	public long getItemId(int position) {
		if(data.get(position) == null) {
			return -1;
		}
		return specifics.getItemId(data.get(position));
	}

	public T getItem(int position) {
		return data.get(position);
	}

	public int getCount() {
		return data.size();
	}

	public void clear() {
		data.clear();
		data.add(null);
		notifyDataSetChanged();
	}

	public void remove(int position) {
		data.remove(position);
		notifyDataSetChanged();
	}

	public void add(int position, T item) {
		data.add(position, item);
		notifyDataSetChanged();
	}

	public Filter getFilter() {
		return filter;
	}

	protected class ListFilter extends Filter {

		public String convertResultToString(Object result) {
			return specifics.convertResultToString((T) result);
		}

		public Filter.FilterResults performFiltering(CharSequence seq) {
			Filter.FilterResults results = null;
			if(seq != null) {
				int size = Integer.parseInt(seq.toString());
				results = new Filter.FilterResults();
				results.count = size;
				results.values = specifics.filter(size, MAX_COUNT);
			}
			return results;
		}

		public void publishResults(CharSequence seq, Filter.FilterResults results) {
			if(results == null || ((data.size() - 1) != results.count)) {
				return;
			}
			data.remove(data.size() - 1);
			data.addAll((List<T>) results.values);
			if(data.size() > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}

	protected class SpecificsClick implements OnItemClickListener {

		public void onItemClick(AdapterView av, View v, int position, long id) {
			specifics.onItemClick(av, v, position, id, data.get(position));
		}
	}

	public interface Specifics<E> {

		public View getView(int id, E item, View v, ViewGroup vg);

		public long getItemId(E item);

		public List<E> filter(int offset, int maxResults);

		public String convertResultToString(E result);

		public void onItemClick(AdapterView<?> av, View v, int position, long id, E item);
	}
}
