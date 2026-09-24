package ca.bc.gov.ols.router.status;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A status message about the routing service or one of its datasets.")
public interface StatusMessage {

	public enum Type {RDM}
	
	public Type getType();
	
	public String getMessage();
	
}
