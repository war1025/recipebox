package org.wrowclif.recipebox.ui.components;

public class CategoryListWidget {

	private Recipe recipe;
	private ViewGroup categories;
	private int categoryItemId;
	private int deleteButtonId;
	private Button addButton;
	private CategoryAddData addData;
	private boolean editing;

	public CategoryListWidget(Recipe recipe, CategoryItemData itemData, CategoryAddData addData) {
		this.recipe = recipe;
		this.categories = itemData.categories;
		this.categoryItemId = itemData.categoryItemId;
		this.deleteButtonId = itemData.deleteButtonId;
		this.addButton = addData.addButton;
	}

	public void setEditing(boolean editing) {
		this.editing = editing;
		for(int i = 0; i < categories.getChildCount(); i++) {
			View child = categories.getChildAt(i);
			View deleteButton = child.findViewById(deleteButtonId);
			deleteButton.setVisiblity((editing) ? View.VISIBLE : View.GONE);
		}
	}

	public void setupAddButton() {
		addButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				addData.onAddClick();
			}
		});
	}

	public Dialog createAddDialog() {
		return null;
	}

	protected static class CategoryItemData {

		protected ViewGroup categories;
		protected int categoryItemId;
		protected int deleteButtonId;

		public CategoryItemData(ViewGroup categories, int categoryItemId, int deleteButtonId) {
			this.categories = categories;
			this.categoryItemId = categoryItemId;
			this.deleteButtonId = deleteButtonId;
		}
	}

	protected static class CategoryAddData {

		protected Button addButton;

		public CategoryAddData(Button addButton) {
			this.addButton = addButton;
		}

		public onAddClick() {

		}
	}
