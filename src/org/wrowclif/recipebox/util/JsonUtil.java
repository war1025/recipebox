package org.wrowclif.recipebox.util;

import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Ingredient;
import org.wrowclif.recipebox.RecipeIngredient;
import org.wrowclif.recipebox.Instruction;
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

	public static String toJson(Recipe recipe) {
		return toJson(recipe, 0);
	}

	public static String toJson(Recipe recipe, int indent) {
		JSONObject recipeJson = new JSONObject();
		String ret = "";

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

	public static Recipe fromJson(String json) {
		JSONObject jsonRecipe = null;
		Utility util = UtilityImpl.singleton;
		Recipe ret = null;

		try {
			jsonRecipe = new JSONObject(json);

			String name = jsonRecipe.getString("name");
			String description = jsonRecipe.getString("description");
			int prepTime = jsonRecipe.getInt("prepTime");
			int cookTime = jsonRecipe.getInt("cookTime");
			JSONArray ingredients = jsonRecipe.getJSONArray("ingredients");
			JSONArray instruction = jsonRecipe.getJSONArray("instructions");

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

			ret = util.newRecipe(name);

			ret.setDescription(description);
			ret.setCookTime(cookTime);
			ret.setPrepTime(prepTime);

			for(int i = 0; i < amounts.size(); i++) {
				RecipeIngredient ri = ret.addIngredient(util.createOrRetrieveIngredient(inNames.get(i)));
				ri.setAmount(amounts.get(i));
			}

			for(String step : steps) {
				Instruction i = ret.addInstruction();
				i.setText(step);
			}

		} catch(Exception e) {
			Log.e(LOG_TAG, "Error converting json text to recipe: " + e, e);
			if(ret != null) {
				ret.delete();
			}
			ret = null;
		}

		return ret;
	}

}
