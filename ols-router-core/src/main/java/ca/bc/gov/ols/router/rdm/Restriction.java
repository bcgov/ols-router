package ca.bc.gov.ols.router.rdm;

public class Restriction {
	public int restrictionId;
	public String restrictionType;
//	LaneType laneType;
//	LaneSubType laneSubType;
//	int laneNumber;
	public double permitableValue;
//	String publicComment;
	public int networkSegmentId;
	
	public String toString( ) {
		return restrictionId + ":" + networkSegmentId +":" + restrictionType + ":" + permitableValue;
	}
}
