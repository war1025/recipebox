package org.wrowclif.recipebox;

public interface Ingredient {

	public long getId();

	public String getName();

	public double getAmount();

	public void setAmount(double amount);

	public String getUnits();

	public void setUnits(String units);

	public List<Ingredient> getSimilarIngredients();

	public void addSimilarIngredient(Ingredient other);

	public void removeSimilarIngredient(Ingredient other);

	public IngredientFactory getFactory();

	public interface IngredientFactory {

		public Ingredient createNew(String name);

		public Ingredient getExisting(String name);

		public void delete(String name);

	}
}
