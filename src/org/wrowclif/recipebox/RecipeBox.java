package org.wrowclif.recipebox;

import org.wrowclif.recipebox.impl.UtilityImpl;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class RecipeBox extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        AppData.initialSingleton(this);

		List<Ingredient> ilist = new ArrayList<Ingredient>();

		String[] inNames = {"spice", "ice", "icecream", "sliced pizza", "mice"};


        Utility util = UtilityImpl.singleton;

		for(String i : inNames) {
			util.createOrRetrieveIngredient(i);
		}

		List<Ingredient> inList = util.searchIngredients("ice", 6);

		for(Ingredient i : inList) {
			Log.d("Recipebox", "Ingredient: " + i.getName());
		}

        List<Recipe> list = util.searchRecipes("Amy", 10);

		Log.d("Recipebox", "Found the following recipes: ");
        for(Recipe rr : list) {
			info(rr);
			rr.delete();
		}

		Recipe r = util.newRecipe("Amy's first recipe");

		for(int k = 0; k < 3; k++) {
			r = r.branch("Amy's new Recipe: " + k);
			r.addIngredient(util.createOrRetrieveIngredient(inNames[k]));
			Log.d("Recipebox", "Created Recipe: " + r.getId() + " " + r.getName());

			Instruction instruction = r.addInstruction();

			instruction.setText(r.getId() + "Most Finally, Love amy lots.");

			info(r);
		}

		AppData.getSingleton().close();

    }

	public static void info(Recipe r) {
		Log.d("Recipebox", "Recipe: " + r.getId() + " " + r.getName());
        for(RecipeIngredient ii : r.getIngredients()) {
			Log.d("Recipebox", "Ingredient: " + ii.getName());
		}

		List<Instruction> i = r.getInstructions();
        for(int j = 0; j < i.size(); j++) {
			Instruction ii = i.get(j);
			Log.d("Recipebox", String.format("%2d: " + ii.getText(), j));
			Log.d("Recipebox", "This step uses: ");
			for(Ingredient ingredient : ii.getIngredientsUsed()) {
				Log.d("Recipebox", ingredient.getName());
			}
		}
	}
}
