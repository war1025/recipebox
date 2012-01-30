package org.wrowclif.recipebox;

import java.util.List;

public interface Unit {

	public enum Type {
		ARBITRARY,
		VOLUME,
		MASS
	}

	public long getId();

	public String getName();

	public String getAbbreviation();

	public Type getType();

	public double getConversionFactorTo(Unit u);

	public List<Unit> getCompatibleUnits();

	public String getStringForAmount(double amount);
}
