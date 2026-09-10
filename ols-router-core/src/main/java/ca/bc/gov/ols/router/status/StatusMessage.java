package ca.bc.gov.ols.router.status;

public interface StatusMessage {

	public enum Type {RDM, CLOSURE}
	
	public Type getType();
	
	public String getMessage();
	
}
