package org.wrowclif.recipebox.ui.components;

import org.wrowclif.recipebox.R;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;

public class ReorderableItemDecorator<T> {

	private int moveableItem;
	private boolean editing;
	private DynamicLoadAdapter<T> adapter;
	private ItemSwap swapper;

	public ReorderableItemDecorator(DynamicLoadAdapter<T> adapter, ItemSwap swapper) {
		this.moveableItem = -1;
		this.editing = false;
		this.adapter = adapter;
		this.swapper = swapper;
	}

	public void setEditing(boolean editing) {
		this.editing = editing;
		this.moveableItem = -1;
	}

	public void decorateItem(View view, int position) {
		if(editing) {
			decorateForEditing(view, position);
		} else {
			decorateForViewing(view, position);
		}
	}

	private void decorateForEditing(View item, final int position) {
		item.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				if(position == moveableItem) {
					moveableItem = -1;
				} else {
					moveableItem = position;
				}
				adapter.notifyDataSetChanged();
				return true;
			}
		});

		Button mu = (Button) item.findViewById(R.id.up_button);
		Button md = (Button) item.findViewById(R.id.down_button);

		if(moveableItem == position) {
			if(position > 0) {
				mu.setVisibility(View.VISIBLE);
				mu.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						swapper.swapItems(position, position - 1);
						moveableItem = position - 1;
						T toMove = adapter.getItem(position);
						adapter.remove(position);
						adapter.add(position - 1, toMove);
					}
				});
			} else {
				mu.setVisibility(View.INVISIBLE);
			}

			if(position < adapter.getCount() - 1) {
				md.setVisibility(View.VISIBLE);
				md.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						swapper.swapItems(position, position + 1);
						T toMove = adapter.getItem(position + 1);
						moveableItem = position + 1;
						adapter.remove(position + 1);
						adapter.add(position, toMove);
					}
				});
			} else {
				md.setVisibility(View.INVISIBLE);
			}
		} else {
			mu.setVisibility(View.GONE);
			md.setVisibility(View.GONE);
		}
	}

	private void decorateForViewing(View item, int position) {
		item.setOnLongClickListener(null);

		Button mu = (Button) item.findViewById(R.id.up_button);
		Button md = (Button) item.findViewById(R.id.down_button);

		mu.setVisibility(View.GONE);
		md.setVisibility(View.GONE);
	}

	public interface ItemSwap {

		public void swapItems(int a, int b);

	}
}

