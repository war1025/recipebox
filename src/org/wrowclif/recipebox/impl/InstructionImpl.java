package org.wrowclif.recipebox.impl;

import org.wrowclif.recipebox.Instruction;
import org.wrowclif.recipebox.Ingredient;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class InstructionImpl implements Instruction {

	private String text;
	private Set<Ingredient> ingredients;

	public InstructionImpl() {
		this.text = null;
		this.ingredients = new HashSet<Ingredient>();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Set<Ingredient> getIngredientsUsed() {
		return Collections.unmodifiableSet(ingredients);
	}

	public void addIngredient(Ingredient toAdd) {
		ingredients.add(toAdd);
	}

	public void removeIngredient(Ingredient toRemove) {
		ingredients.remove(toRemove);
	}

}
