/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.time;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class TemporalSetUnion implements TemporalSet {

	private List<TemporalSet> sets;
	
	public TemporalSetUnion(TemporalSet ... setArray) {
		sets = Arrays.asList(setArray);
	}

	@SuppressWarnings("unchecked")
	public <T extends TemporalSet> TemporalSetUnion(List<T> sets) {
		this.sets = (List<TemporalSet>) sets;
	}

	@Override
	public boolean contains(LocalDateTime dateTime) {
		for(TemporalSet set : sets) {
			if(set.contains(dateTime)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isAlways() {
		for(TemporalSet set : sets) {
			if(set.isAlways()) {
				return true;
			}
		}
		// TODO - need to consider sets that combine to make always 
		// eg. dayset(weekdays) + dayset(weekends)
		// or null -> somedate + somedate -> null
		return false;
	}

	@Override
	public LocalDateTime after(LocalDateTime dateTime) {
		if(isAlways()) return null;
		boolean changed = true;
		while(changed) {
			changed = false;
			for(TemporalSet set : sets) {
				if(set.contains(dateTime)) {
					LocalDateTime newDateTime = set.after(dateTime);
					if(newDateTime != null) {
						dateTime = newDateTime;
					}
					changed = true;
				}
			}
		}
		return dateTime;
	}

}
