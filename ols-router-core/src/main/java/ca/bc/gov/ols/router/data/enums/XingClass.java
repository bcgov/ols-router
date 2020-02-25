package ca.bc.gov.ols.router.data.enums;

public enum XingClass {
	SMALLER, SAME, LARGER;

	public static XingClass convert(String s) {
		try {
			return valueOf(s);
		} catch(IllegalArgumentException iae) {
			return SAME;
		}
	}
	
	public double applyMultiplier(double xingCost, double mult) {
		switch(this) {
			case SMALLER: return xingCost * mult;
			case LARGER: return xingCost / mult;
			case SAME:
			default: return xingCost;
		}
	}
}
