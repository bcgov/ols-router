package ca.bc.gov.ols.router.data;

import ca.bc.gov.ols.router.data.enums.TurnDirection;

public class TurnClass {

	private final int[] ids;
	private final TurnDirection turnDirection;
	
	public TurnClass(final int fromSegId, final int intId, final int toSegId, final TurnDirection turnDirection) {
		ids = new int[3];
		ids[0] = fromSegId;
		ids[1] = intId;
		ids[2] = toSegId;
		this.turnDirection = turnDirection;
	}

	public TurnClass(String idSeq, TurnDirection turnDirection) {
		String[] stringIds = idSeq.split("\\|");
		ids = new int[stringIds.length];
		for(int i = 0; i < stringIds.length; i++) {
			ids[i] = Integer.parseInt(stringIds[i]);
		}
		this.turnDirection = turnDirection; 
	}

	public String getIdSeqString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append(ids[0] + "|" + ids[1] + "|" + ids[2]);
		for(int i = 3; i < ids.length; i++) {
			sb.append("|" + ids[i]);
		}
		return sb.toString();
	}

	public int[] getIdSeq() {
		return ids;
	}

	public TurnDirection getTurnDirection() {
		return turnDirection;
	}
	
}
