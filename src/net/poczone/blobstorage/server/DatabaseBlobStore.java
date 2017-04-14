package net.poczone.blobstorage.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.ServletContext;

import net.poczone.blobstorage.shared.Blob;
import net.poczone.blobstorage.shared.BlobStore;
import net.poczone.blobstorage.shared.Login;

public class DatabaseBlobStore implements BlobStore {
	private static final String CTX_DB_URL = "db.url";
	private static final String CTX_DB_USERNAME = "db.username";
	private static final String CTX_DB_PASSWORD = "db.password";

	private String jdbcUrl;
	private Login dbLogin;

	public DatabaseBlobStore(String jdbcUrl, Login databaseLogin) {
		this.jdbcUrl = jdbcUrl;
		this.dbLogin = databaseLogin;
		initDriver();
	}

	public DatabaseBlobStore(ServletContext context) {
		jdbcUrl = context.getInitParameter(CTX_DB_URL);
		String username = context.getInitParameter(CTX_DB_USERNAME);
		String password = context.getInitParameter(CTX_DB_PASSWORD);

		if (jdbcUrl == null || username == null || password == null) {
			throw new IllegalStateException("Database not configured. "
					+ "Please define the servlet context parameters "
					+ CTX_DB_URL + ", " + CTX_DB_USERNAME + " and "
					+ CTX_DB_PASSWORD + ".");
		}

		dbLogin = new Login(username, password);
		initDriver();
	}

	private void initDriver() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			throw new IllegalStateException(
					"Could not initialize MySQL Driver", e);
		}
	}

	@Override
	public void post(Blob blob) throws IOException {
		String postSql = "REPLACE blobstorage SET path=?, mimeType=?, data=?, lastMod=?";

		try (Connection connection = getConnection();
				PreparedStatement stmt = connection.prepareStatement(postSql)) {

			stmt.setString(1, blob.getPath());
			stmt.setString(2, blob.getMimeType());
			stmt.setBytes(3, blob.getData());
			stmt.setDate(4, new java.sql.Date(blob.getLastMod().getTime()));

			stmt.executeUpdate();

		} catch (Exception e) {
			throw new IOException("Failed to POST blob", e);
		}
	}

	@Override
	public Blob get(String path) throws IOException {
		String getSql = "SELECT mimeType, data, lastMod FROM blobstorage WHERE path=?";

		try (Connection connection = getConnection();
				PreparedStatement stmt = connection.prepareStatement(getSql)) {

			stmt.setString(1, path);

			ResultSet result = stmt.executeQuery();
			if (result.next()) {
				String mimeType = result.getString(1);
				byte[] data = result.getBytes(2);
				java.util.Date lastMod = new java.util.Date(result.getDate(3)
						.getTime());
				boolean exists = true;

				Blob blob = new Blob(path, mimeType, data, exists, lastMod);

				return blob;
			} else {
				boolean exists = false;

				Blob blob = new Blob(path, "text/plain", new byte[0], exists,
						new Date());

				return blob;
			}

		} catch (Exception e) {
			throw new IOException("Failed to GET blob", e);
		}
	}

	@Override
	public boolean delete(String path) throws IOException {
		String deleteSql = "DELETE FROM blobstorage WHERE path=?";

		try (Connection connection = getConnection();
				PreparedStatement stmt = connection.prepareStatement(deleteSql)) {

			stmt.setString(1, path);

			int affected = stmt.executeUpdate();

			return affected > 0;
		} catch (Exception e) {
			throw new IOException("Failed to DELETE blob", e);
		}
	}

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(jdbcUrl, dbLogin.getUsername(),
				dbLogin.getPassword());
	}
}
