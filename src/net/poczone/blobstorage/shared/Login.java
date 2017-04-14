package net.poczone.blobstorage.shared;

public class Login {
	private static final String DEFAULT_USERNAME = "guest";
	private static final String DEFAULT_PASSWORD = "guest";

	private String username;
	private String password;

	public Login(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username != null ? username : DEFAULT_USERNAME;
	}

	public String getPassword() {
		return password != null ? password : DEFAULT_PASSWORD;
	}
}
