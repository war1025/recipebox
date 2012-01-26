package org.wrowclif.recipebox.impl;

import org.wrowclif.recipebox.Ingredient;

public class IngredientImpl implements Ingredient {

	private String name;
	private double amount;
	private String units;

	public IngredientImpl(String name, double amount, String units) {
		this.name = name;
		this.amount = amount;
		this.units = units;
	}

	public String getName() {
		return this.name;
	}

	public double getAmount() {
		return this.amount;
	}

	public String getUnits() {
		return this.units;
	}
}


		String stmt = String.format(
			"SELECT i.iid, i.name, ir.amount, u.uid, u.name " +
			"FROM Ingredient i, Recipe r, RecipeIngredients ir, Units u " +
			"WHERE ir.rid = r.rid " +
				"and ir.iid = i.iid " +
				"and ir.uid = u.uid " +
				"and r.rid = %d " +
			"ORDER BY ir.num ASC;", id);
