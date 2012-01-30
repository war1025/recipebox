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

        AppData.initialSingleton(this).getOpenHelper().getWritableDatabase();

		List<Ingredient> ilist = new ArrayList<Ingredient>();

        Utility util = UtilityImpl.singleton;

        List<Recipe> list = util.searchRecipes("Amy", 10);

		Log.d("Recipebox", "Found the following recipes: ");
        for(Recipe rr : list) {
			info(rr);
			rr.delete();
		}

		Recipe r = util.newRecipe("Amy's first recipe");

		for(int k = 0; k < 3; k++) {
			r = r.branch("Amy's new Recipe: " + k);
			Log.d("Recipebox", "Created Recipe: " + r.getId() + " " + r.getName());

			Instruction instruction = r.addStep();

			instruction.setText(r.getId() + "Most Finally, Love amy lots.");

			info(r);
		}

		AppData.getSingleton().close();
    }

	public static void info(Recipe r) {
		Log.d("Recipebox", "Recipe: " + r.getId() + " " + r.getName());
        for(RecipeIngredient ii : r.getIngredients()) {
			Log.d("Recipebox", "Ingredient: " + ii.getIngredient().getId() + " " + ii.getName());
		}

        for(Instruction ii : r.getSteps()) {
			Log.d("Recipebox", "Instruction: " + ii.getId() + " " + ii.getText());
			Log.d("Recipebox", "This step uses: ");
			for(Ingredient ingredient : ii.getIngredientsUsed()) {
				Log.d("Recipebox", ingredient.getName() + " " + ingredient.getId());
			}
		}
	}

}
