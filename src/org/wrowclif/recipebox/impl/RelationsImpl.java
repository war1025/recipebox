package org.wrowclif.recipebox.impl;

import org.wrowclif.recipebox.Relations;
import org.wrowclif.recipebox.Recipe;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class RelationsImpl implements Relations {

	private Map<Recipe, Set<Recipe>> variations;
	private Map<Recipe, Set<Recipe>> suggestions;

	public RelationsImpl() {
		variations = new HashMap<Recipe, Set<Recipe>>();
		suggestions = new HashMap<Recipe, Set<Recipe>>();
	}

	public Set<Recipe> getVariants(Recipe r) {
		if(!variations.hasKey(r)) {
			variations.put(r, new HashSet<Recipe>());
		}
		return Collections.unmodifiableSet(variations.get(r));
	}

	public void addVariant(Recipe r1, Recipe r2) {
		if(!variations.hasKey(r1)) {
			variations.put(r1, new HashSet<Recipe>());
		}
		if(!variations.hasKey(r2)) {
			variations.put(r2, new HashSet<Recipe>());
		}

		variations.get(r1).add(r2);
		variations.get(r2).add(r1);
	}

	public void removeVariant(Recipe r1, Recipe r2) {
		if(!variations.hasKey(r1)) {
			variations.put(r1, new HashSet<Recipe>());
		}
		if(!variations.hasKey(r2)) {
			variations.put(r2, new HashSet<Recipe>());
		}

		variations.get(r1).remove(r2);
		variations.get(r2).remove(r1);
	}

	public Set<Recipe> getSuggestions(Recipe r) {
		if(!variations.hasKey(r)) {
			variations.put(r, new HashSet<Recipe>());
		}
		return Collections.unmodifiableSet(variations.get(r));
	}

	public void addSuggestion(Recipe r1, Recipe r2) {
		if(!variations.hasKey(r1)) {
			variations.put(r1, new HashSet<Recipe>());
		}
		if(!variations.hasKey(r2)) {
			variations.put(r2, new HashSet<Recipe>());
		}

		variations.get(r1).add(r2);
		variations.get(r2).add(r1);
	}

	public void removeSuggestion(Recipe r1, Recipe r2) {
		if(!variations.hasKey(r1)) {
			variations.put(r1, new HashSet<Recipe>());
		}
		if(!variations.hasKey(r2)) {
			variations.put(r2, new HashSet<Recipe>());
		}

		variations.get(r1).remove(r2);
		variations.get(r2).remove(r1);
	}

}




