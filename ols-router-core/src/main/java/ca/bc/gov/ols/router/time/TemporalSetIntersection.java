/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.time;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class TemporalSetIntersection implements TemporalSet {
	
	private List<TemporalSet> sets;
	
	public TemporalSetIntersection(TemporalSet ... setArray) {
		sets = Arrays.asList(setArray);
	}

	@SuppressWarnings("unchecked")
	public <T extends TemporalSet> TemporalSetIntersection(List<T> sets) {
		this.sets = (List<TemporalSet>) sets;
	}

	@Override
	public boolean contains(LocalDateTime dateTime) {
		for(TemporalSet set : sets) {
			if(!set.contains(dateTime)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isAlways() {
		for(TemporalSet set : sets) {
			if(!set.isAlways()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public LocalDateTime after(LocalDateTime dateTime) {
		if(isAlways()) return null;
		while(dateTime != null) {
			LocalDateTime[] possibles = new LocalDateTime[sets.size()];
			for(int i = 0; i < sets.size(); i++) {
				TemporalSet set = sets.get(i);
				if(!set.contains(dateTime)) {
					return dateTime;
				}
				possibles[i] = set.after(dateTime);
			}
			LocalDateTime min = null;
			for(LocalDateTime possible : possibles) {
				if(possible != null && (min == null || possible.isBefore(min))) {
					min = possible;
				}
			}
			dateTime = min;
		}
		return null;
	}

}
