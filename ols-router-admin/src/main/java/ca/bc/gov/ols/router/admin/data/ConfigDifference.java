package ca.bc.gov.ols.router.admin.data;

public class ConfigDifference<T> {
	private T db;
	private T file;
	
	public ConfigDifference(T db, T file) {
		this.db = db;
		this.file = file;
	}
	
	public T getDb() {
		return db;
	}
	
	public T getFile() {
		return file;
	}
}
