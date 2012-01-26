package org.wrowclif.recipebox;

import java.util.List;

public interface Unit {

	public long getId();

	public String getName();

	public List<Unit> getConvertibleUnits();

	public double convertToGivenUnit(double amount, Unit given);

	public UnitFactory getFactory();

	public interface UnitFactory {

		public Unit createNew(String name);

		public Unit getExisting(String name);

		public void setConversionFactor(Unit a, Unit b, double factor);

		public void removeConversionFactor(Unit a, Unit b);

	}
}
