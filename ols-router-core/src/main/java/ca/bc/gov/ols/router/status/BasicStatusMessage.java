package ca.bc.gov.ols.router.status;

public class BasicStatusMessage implements StatusMessage {

	protected final String message;
	protected final Type type;
	
	public BasicStatusMessage(Type type, String message) {
		this.type = type;
		this.message = message;
	}

	public Type getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}
	
	@Override
	public String toString() {
		return getMessage();
	}
}
