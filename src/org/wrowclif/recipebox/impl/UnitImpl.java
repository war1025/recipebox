package org.wrowclif.recipebox.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Unit;
import org.wrowclif.recipebox.Unit.Type;
import org.wrowclif.recipebox.db.RecipeBoxOpenHelper;

import java.util.List;
import java.util.ArrayList;

public class UnitImpl implements Unit {

	protected static final UnitFactory factory;

	static {
		factory = new UnitFactory();
	}

	private long id;
	private String name;
	private Type type;
	private double factor;
	private int minFraction;

	private UnitImpl(long id, String name, int type, double factor, int minFraction) {
		this.id = id;
		this.name = name;
		switch(type) {
			case 1 : this.type = Type.VOLUME; break;
			case 2 : this.type = Type.MASS; break;
			default : this.type = Type.ARBITRARY; break;
		}
		this.factor = factor;
		this.minFraction = minFraction;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public double getConversionFactorTo(Unit u) {
		if(u.getType() != type) {
			throw new IllegalArgumentException("Unit types do not match");
		} else if(type == Type.ARBITRARY) {
			throw new IllegalArgumentException("Arbitrary typed units cannot be converted");
		}

		UnitImpl ui = (UnitImpl) u;

		return factor / ui.factor;
	}

	public List<Unit> getCompatibleUnits() {
		if(type == Type.ARBITRARY) {
			return new ArrayList<Unit>();
		}

		String stmt =
			"SELECT u.uid, u.name, u.type, u.factor, u.minfraction " +
			"FROM Unit u " +
			"WHERE u.type == ? " +
				"and u.uid != ?; ";

		List<Unit> list = null;
		SQLiteDatabase db = factory.helper.getWritableDatabase();
		db.beginTransaction();
			Cursor c = db.rawQuery(stmt, new String[] {type.ordinal() + "", id + ""});
			list = factory.createListFromCursor(c);
			c.close();
		db.endTransaction();
		return list;
	}

	public String getStringForAmount(double amount) {
		int intAmount = (int) amount;
		if(minFraction == 0) {
			return intAmount + "";
		} if(minFraction < 0) {
			String fmt = String.format("%.%df", -minFraction);
			return String.format(fmt, amount);
		} else {
			// Some sort of binary search thing.
			return "";
		}
	}

	protected static class UnitFactory {

		protected RecipeBoxOpenHelper helper;

		private UnitFactory() {
			helper = AppData.getSingleton().getOpenHelper();
		}

		protected List<Unit> createListFromCursor(Cursor c) {
			List<Unit> list = new ArrayList<Unit>(c.getCount());
			while(c.moveToNext()) {
				// u.uid, u.name, u.type, u.factor, u.minfraction
				UnitImpl ui = new UnitImpl(c.getLong(1), c.getString(2), c.getInt(3), c.getDouble(4), c.getInt(5));
				list.add(ui);
			}
			return list;
		}

		protected Unit createFromData(ContentValues v) {
			return new UnitImpl(v.getAsLong("id"), v.getAsString("name"), v.getAsInteger("type"),
								v.getAsDouble("factor"), v.getAsInteger("minfraction"));
		}
	}
}
