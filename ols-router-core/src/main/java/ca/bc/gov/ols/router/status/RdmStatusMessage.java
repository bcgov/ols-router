package ca.bc.gov.ols.router.status;

public class RdmStatusMessage extends BasicStatusMessage {
	private final int restrictionId;
	
	public RdmStatusMessage(int restrictionId, String message) {
		super(Type.RDM, message);
		this.restrictionId = restrictionId;
	}
		
	public int getRestrictionId() {
		return restrictionId;
	}
	
	
}
