package org.wrowclif.recipebox.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.AppData.Transaction;
import org.wrowclif.recipebox.Unit;
import org.wrowclif.recipebox.Unit.Type;

import java.util.List;
import java.util.ArrayList;

public class UnitImpl implements Unit {

	protected static final UnitFactory factory;

	static {
		factory = new UnitFactory();
	}

	private long id;
	private String name;
	private String abbreviation;
	private Type type;
	private double factor;
	private int minFraction;

	private UnitImpl(long id, String name, String abbreviation, int type, double factor, int minFraction) {
		this.id = id;
		this.name = name;
		this.abbreviation = abbreviation;
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

	public String getAbbreviation() {
		return abbreviation;
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

		final String stmt =
			"SELECT u.uid, u.name, u.abbreviation, u.type, u.factor, u.minfraction " +
			"FROM Unit u " +
			"WHERE u.type == ? " +
				"and u.uid != ?; ";

		return factory.data.sqlTransaction(new Transaction<List<Unit>>() {
			public List<Unit> exec(SQLiteDatabase db) {
				Cursor c = db.rawQuery(stmt, new String[] {type.ordinal() + "", id + ""});
				List<Unit> list = factory.createListFromCursor(c);
				c.close();
				return list;
			}
		});
	}

	public String getStringForAmount(double amount) {
		int intAmount = (int) amount;
		if(minFraction <= 0) {
			String fmt = "%." + -minFraction + "f";
			return String.format(fmt, amount);
		} else {
			double partialAmount = amount - intAmount;

			String frac = null;

			int numerator = (int) Math.round(partialAmount * minFraction);
			int denominator = minFraction;

			double roundedFrac = (numerator * 1.0)/minFraction;

			if(minFraction > 2) {
				if(Math.abs(partialAmount - 1/3.0) < Math.abs(partialAmount - roundedFrac)) {
					frac = "1/3";
				} else if(Math.abs(partialAmount - 2/3.0) < Math.abs(partialAmount - roundedFrac)) {
					frac = "2/3";
				}
			}
			if(numerator == 0) {
				frac = "";
			} else if(frac == null) {
				int a = numerator;
				int b = denominator;

				while(b > 0) {
					if(a > b) {
						a = a - b;
					} else {
						b = b - a;
					}
				}
				numerator = numerator / a;
				denominator = denominator / a;

				frac = numerator + "/" + denominator;

				if(denominator == 1 && numerator == 1) {
					if(numerator == 1) {
						intAmount++;
						frac = "";
					}
				}
			}
			return ((intAmount == 0) ? "" : intAmount + " ") + frac;
		}
	}

	protected static class UnitFactory {

		protected AppData data;

		private Object nullLock;
		private Unit nullUnit;

		private UnitFactory() {
			data = AppData.getSingleton();
			nullLock = new Object();
			nullUnit = null;
			ensureUnits();
		}

		protected List<Unit> createListFromCursor(Cursor c) {
			List<Unit> list = new ArrayList<Unit>(c.getCount());
			while(c.moveToNext()) {
				// u.uid, u.name, u.abbreviation, u.type, u.factor, u.minfraction
				UnitImpl ui = new UnitImpl(c.getLong(0), c.getString(1), c.getString(2),
												c.getInt(3), c.getDouble(4), c.getInt(5));
				list.add(ui);
			}
			return list;
		}

		protected Unit createFromData(ContentValues v) {
			return new UnitImpl(v.getAsLong("uid"), v.getAsString("name"), v.getAsString("abbreviation"),
								v.getAsInteger("type"), v.getAsDouble("factor"), v.getAsInteger("minfraction"));
		}

		protected Unit getNullUnit() {
			final String stmt =
				"SELECT u.uid, u.name, u.abbreviation, u.type, u.factor, u.minfraction " +
				"FROM Unit u " +
				"WHERE u.name = ?; ";

			synchronized(nullLock) {
				if(nullUnit == null) {
					data.sqlTransaction(new Transaction<Void>() {
						public Void exec(SQLiteDatabase db) {
							Cursor c = db.rawQuery(stmt, new String[] {"nullunit"});
							if(c.moveToNext()) {
								nullUnit = new UnitImpl(c.getLong(0), c.getString(1), c.getString(2),
															c.getInt(3), c.getDouble(4), c.getInt(5));
							} else {
								ContentValues values = new ContentValues();
								values.put("name", "nullunit");
								values.put("abbreviation", "");
								values.put("type", 0);
								values.put("factor", 1);
								values.put("minfraction", 0);
								long id = db.insert("Unit", null, values);
								nullUnit = createFromData(values);
							}
							return null;
						}
					});
				}
				return nullUnit;
			}
		}

		protected void ensureUnits() {
			final String stmt =
				"INSERT OR IGNORE INTO Unit(name, abbreviation, type, factor, minfraction) " +
					"VALUES('Cup', 'C', 1, 236.58824, 4); " +

				"INSERT OR IGNORE INTO Unit(name, abbreviation, type, factor, minfraction) " +
					"VALUES('Tablespoon', 'Tbs', 1, 14.786765, 3); " +

				"INSERT OR IGNORE INTO Unit(name, abbreviation, type, factor, minfraction) " +
					"VALUES('teaspoon', 'tsp', 1, 4.9289216, 8); " +

				"INSERT OR IGNORE INTO Unit(name, abbreviation, type, factor, minfraction) " +
					"VALUES('Pint', 'p', 1, 473.17647, 4); " +

				"INSERT OR IGNORE INTO Unit(name, abbreviation, type, factor, minfraction) " +
					"VALUES('Quart', 'qt', 1, 946.35295, 4); " +

				"INSERT OR IGNORE INTO Unit(name, abbreviation, type, factor, minfraction) " +
					"VALUES('Gallon', 'gal', 1, 3785.4118, 4); " +

				"INSERT OR IGNORE INTO Unit(name, abbreviation, type, factor, minfraction) " +
					"VALUES('Fluid Ounce', 'floz', 1, 29.57353, 4); " +

				"INSERT OR IGNORE INTO Unit(name, abbreviation, type, factor, minfraction) " +
					"VALUES('milliliter', 'mL', 1, 1, 0); " +

				"INSERT OR IGNORE INTO Unit(name, abbreviation, type, factor, minfraction) " +
					"VALUES('Liter', 'L', 1, 1000, -2); " +

				"INSERT OR IGNORE INTO Unit(name, abbreviation, type, factor, minfraction) " +
					"VALUES('Pound', 'lb', 2, 453.59237, 4); " +

				"INSERT OR IGNORE INTO Unit(name, abbreviation, type, factor, minfraction) " +
					"VALUES('Ounce', 'oz', 2, 28.349523, 4); " +

				"INSERT OR IGNORE INTO Unit(name, abbreviation, type, factor, minfraction) " +
					"VALUES('gram', 'g', 2, 1, 0); " +

				"INSERT OR IGNORE INTO Unit(name, abbreviation, type, factor, minfraction) " +
					"VALUES('kilogram', 'kg', 2, 1000, -2); " +

				"INSERT OR IGNORE INTO Unit(name, abbreviation, type, factor, minfraction) " +
					"VALUES('to taste', 'to taste', 0, 1, 0); " +

				"INSERT OR IGNORE INTO Unit(name, abbreviation, type, factor, minfraction) " +
					"VALUES('pinch', 'pinch', 0, 1, 0); " +

				"INSERT OR IGNORE INTO Unit(name, abbreviation, type, factor, minfraction) " +
					"VALUES('package', 'pkg', 0, 1, 0); " +

				"INSERT OR IGNORE INTO Unit(name, abbreviation, type, factor, minfraction) " +
					"VALUES('stick', 'stick', 0, 1, 0); " +

				"INSERT OR IGNORE INTO Unit(name, abbreviation, type, factor, minfraction) " +
					"VALUES('nullunit', '', 0, 1, 0);";

			data.sqlTransaction(new Transaction<Void>() {
				public Void exec(SQLiteDatabase db) {
					String[] stmts = stmt.split(";");

					for(String s : stmts) {
						db.execSQL(s);
					}
					return null;
				}
			});
		}
	}
}
