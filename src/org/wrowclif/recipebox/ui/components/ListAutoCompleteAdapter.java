package org.wrowclif.recipebox.ui.components;

import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;

public class ListAutoCompleteAdapter<T> extends BaseAdapter implements Filterable {

	private List<T> data;
	private ListFilter filter;
	private Specifics<T> specifics;
	public final OnItemClickListener onClick;

	public ListAutoCompleteAdapter(Specifics<T> sp) {
		super();
		this.specifics = sp;
		this.filter = new ListFilter();

		onClick = new SpecificsClick();
	}

	public void setUpView(AutoCompleteTextView av) {
		av.setAdapter(this);
		av.setOnItemClickListener(this.onClick);
	}

	public View getView(int id, View v, ViewGroup vg) {

		return specifics.getView(id, data.get(id), v, vg);
	}

	public long getItemId(int position) {
		return specifics.getItemId(data.get(position));
	}

	public T getItem(int position) {
		return data.get(position);
	}

	public int getCount() {
		if(data == null) {
			return 0;
		}
		return data.size();
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
				results = new Filter.FilterResults();
				results.count = 1;
				results.values = specifics.filter(seq);
			}
			return results;
		}

		public void publishResults(CharSequence seq, Filter.FilterResults results) {
			if(results == null) {
				return;
			}
			data = (List<T>) results.values;
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

		public List<E> filter(CharSequence seq);

		public String convertResultToString(E result);

		public void onItemClick(AdapterView<?> av, View v, int position, long id, E item);
	}
}
