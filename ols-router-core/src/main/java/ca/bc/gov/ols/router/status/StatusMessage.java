package ca.bc.gov.ols.router.status;

public interface StatusMessage {

	public enum Type {RDM}
	
	public Type getType();
	
	public String getMessage();
	
}
