package org.wrowclif.recipebox;

public interface RecipeIngredient {

	public String getName();

	public double getAmount();

	public void setAmount(double amount);

	public String getUnitName();

	public Ingredient getIngredient();

	public Unit getUnits();

	public void setUnits(Unit unit);
}
