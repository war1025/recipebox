package org.wrowclif.recipebox.util;

import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Ingredient;
import org.wrowclif.recipebox.RecipeIngredient;
import org.wrowclif.recipebox.Instruction;
import org.wrowclif.recipebox.Category;
import org.wrowclif.recipebox.Utility;
import org.wrowclif.recipebox.impl.UtilityImpl;

import java.util.List;
import java.util.ArrayList;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {

	private static final String LOG_TAG = "Recipebox Json Util";

	public static String toJson(Recipe[] recipes) {
		return toJson(recipes, 0);
	}

	public static String toJson(Recipe[] recipes, int indent) {
		String ret = "";

		JSONArray recipeList = new JSONArray();
		for(Recipe r : recipes) {
			recipeList.put(toJsonObject(r));
		}

		try {
			if(indent < 1) {
				ret = recipeList.toString();
			} else {
				ret = recipeList.toString(indent);
			}
		} catch(JSONException e) {
			Log.e(LOG_TAG, "Exception converting recipe to json: " + e, e);
		}

		return ret;
	}

	public static String toJson(Recipe recipe) {
		return toJson(recipe, 0);
	}

	public static String toJson(Recipe recipe, int indent) {
		String ret = "";

		JSONObject recipeJson = toJsonObject(recipe);

		try {
			if(indent < 1) {
				ret = recipeJson.toString();
			} else {
				ret = recipeJson.toString(indent);
			}
		} catch(JSONException e) {
			Log.e(LOG_TAG, "Exception converting recipe to json: " + e, e);
		}

		return ret;
	}

	private static JSONObject toJsonObject(Recipe recipe) {
		JSONObject recipeJson = new JSONObject();

		try {
			recipeJson.put("name", recipe.getName());
			recipeJson.put("description", recipe.getDescription());
			recipeJson.put("prepTime", recipe.getPrepTime());
			recipeJson.put("cookTime", recipe.getCookTime());

			JSONArray ingredients = new JSONArray();

			for(RecipeIngredient ri : recipe.getIngredients()) {
				JSONObject ingredient = new JSONObject();
				ingredient.put("amount", ri.getAmount());
				ingredient.put("name", ri.getName());

				ingredients.put(ingredient);
			}

			recipeJson.put("ingredients", ingredients);

			JSONArray instructions = new JSONArray();

			for(Instruction in : recipe.getInstructions()) {
				instructions.put(in.getText());
			}

			recipeJson.put("instructions", instructions);

			JSONArray categories = new JSONArray();

			for(Category cat : recipe.getCategories()) {
				categories.put(cat.getName());
			}

			recipeJson.put("categories", categories);

		} catch(JSONException e) {
			Log.e(LOG_TAG, "Exception converting recipe to json: " + e, e);
		}

		return recipeJson;
	}

	public static Recipe[] fromJson(String json) {
		if(json == null || json.length() == 0) {
			return null;
		}
		JSONArray recipes = null;
		JSONObject jsonRecipe = null;
		Utility util = UtilityImpl.singleton;
		List<Recipe> ret = new ArrayList<Recipe>();

		try {
			if(json.charAt(0) == '{') {
				recipes = new JSONArray();
				recipes.put(new JSONObject(json));
			} else {
				recipes = new JSONArray(json);
			}

			for(int j = 0; j < recipes.length(); j++) {
				jsonRecipe = recipes.getJSONObject(j);
				String name = jsonRecipe.getString("name");
				String description = jsonRecipe.getString("description");
				int prepTime = jsonRecipe.getInt("prepTime");
				int cookTime = jsonRecipe.getInt("cookTime");
				JSONArray ingredients = jsonRecipe.getJSONArray("ingredients");
				JSONArray instruction = jsonRecipe.getJSONArray("instructions");
				JSONArray categories = jsonRecipe.optJSONArray("categories");

				List<String> amounts = new ArrayList<String>();
				List<String> inNames = new ArrayList<String>();

				for(int i = 0; i < ingredients.length(); i++) {
					JSONObject ing = ingredients.getJSONObject(i);

					amounts.add(ing.getString("amount"));
					inNames.add(ing.getString("name"));
				}

				List<String> steps = new ArrayList<String>();

				for(int i = 0; i < instruction.length(); i++) {
					steps.add(instruction.getString(i));
				}

				if(amounts.size() != inNames.size()) {
					return null;
				}

				Recipe recipe = util.newRecipe(name);
				ret.add(recipe);

				recipe.setDescription(description);
				recipe.setCookTime(cookTime);
				recipe.setPrepTime(prepTime);

				for(int i = 0; i < amounts.size(); i++) {
					RecipeIngredient ri = recipe.addIngredient(util.createOrRetrieveIngredient(inNames.get(i)));
					ri.setAmount(amounts.get(i));
				}

				for(String step : steps) {
					Instruction i = recipe.addInstruction();
					i.setText(step);
				}

				if(categories != null) {
					for(int i = 0; i < categories.length(); i++) {
						String category = categories.getString(i);
						Category c = util.createOrRetrieveCategory(category);
						c.addRecipe(recipe);
					}
				}
			}

		} catch(Exception e) {
			Log.e(LOG_TAG, "Error converting json text to recipe: " + e, e);
			for(Recipe r : ret) {
				r.delete();
			}
			ret = null;
		}

		return ret.toArray(new Recipe[0]);
	}

}
