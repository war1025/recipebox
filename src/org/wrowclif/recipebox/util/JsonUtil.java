package org.wrowclif.recipebox.util;

import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Ingredient;
import org.wrowclif.recipebox.RecipeIngredient;
import org.wrowclif.recipebox.Instruction;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {

	private static final String LOG_TAG = "Recipebox Json Util";

	public static String toJson(Recipe recipe) {
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

			ret = recipeJson.toString(3);
		} catch(JSONException e) {
			Log.e(LOG_TAG, "Exception converting recipe to json: " + e, e);
		}

		return ret;
	}

}
