/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.ols.router.util;

public class TimeHelper {

	public static String formatTime(double dblSeconds) {
		long seconds = Math.round(dblSeconds);
		long minutes = 0;
		long hours = 0;
		if(seconds > 60) {
			minutes = seconds / 60;
			seconds = seconds % 60;
		}
		if(minutes > 60) {
			hours = minutes / 60;
			minutes = minutes % 60;
		}
		StringBuilder sb = new StringBuilder();
		if(hours > 0) {
			sb.append(hours + " hour");
			if(hours > 1) {
				sb.append("s");
			}
			if(minutes > 0) {
				sb.append(" " + minutes + " minute");
				if(minutes > 1) {
					sb.append("s");
				}
			}
			return sb.toString();
		}
		if(minutes > 0) {
			sb.append(minutes + " minute");
			if(minutes > 1) {
				sb.append("s");
			}
			if(seconds > 0) {
				sb.append(" " + seconds + " second");
				if(seconds > 1) {
					sb.append("s");
				}
			}
			return sb.toString();
		}
		return seconds + " seconds";
	}

}
