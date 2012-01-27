		String stmt = String.format(
			"SELECT i.iid, i.name, ir.amount, u.uid, u.name " +
			"FROM Ingredient i, Recipe r, RecipeIngredients ir, Units u " +
			"WHERE ir.rid = r.rid " +
				"and ir.iid = i.iid " +
				"and ir.uid = u.uid " +
				"and r.rid = %d " +
			"ORDER BY ir.num ASC;", id);
