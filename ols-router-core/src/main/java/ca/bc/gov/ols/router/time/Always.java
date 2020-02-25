package ca.bc.gov.ols.router.time;

import java.time.LocalDateTime;

public class Always implements TemporalSet {

	@Override
	public boolean contains(LocalDateTime dateTime) {
		return true;
	}

	@Override
	public boolean isAlways() {
		return true;
	}

	@Override
	public LocalDateTime after(LocalDateTime dateTime) {
		return null;
	}

}
