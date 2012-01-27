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


