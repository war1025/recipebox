package org.wrowclif.recipebox.impl;

import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Suggestion;

public class SuggestionImpl implements Suggestion {

	public Recipe getOriginalRecipe();

	public Recipe getSuggestedRecipe();

	public String getComments();

	public void setComments(String text);

}

/*List<Suggestion> l1 = null;
		String stmt =
			"SELECT r.rid, r.name, r.description, r.preptime, r.cooktime, r.cost, r.vid " +
			"FROM Recipe r, SuggestedWith sw " +
			"WHERE sw.rid1 = %s " +
				"and sw.rid2 = %s; ";
		SQLiteDatabase db = factory.helper.getWriteableDatabase();
		db.beginTransaction();
			Cursor c1 = db.rawQuery(String.format(stmt, "r.id", id + ""), null);
			Cursor c2 = db.rawQuery(String.format(stmt, id + "", "r.id"), null);
			l1 = factory.createListFromCursor(c1);
			l1.addAll(factory.createListFromCursor(c2));
			c1.close();
			c2.close();
		db.endTransaction();
		return l1;
*/
