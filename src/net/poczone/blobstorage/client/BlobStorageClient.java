package net.poczone.blobstorage.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

import javax.servlet.ServletContext;

import net.poczone.blobstorage.shared.Blob;
import net.poczone.blobstorage.shared.BlobStore;
import net.poczone.blobstorage.shared.BlobStreamTool;
import net.poczone.blobstorage.shared.Login;

public class BlobStorageClient implements BlobStore {
	public static final String BASE_LOCALHOST = "http://localhost:8080/blobstorage/";
	public static final String BASE_POCZONE = "https://poczone.net/blobstorage/";

	private static final String DEFAULT_BASE = BASE_POCZONE;

	private static final String CTX_BLOBSTORAGE_BASE = "blobstorage.base";
	private static final String CTX_BLOBSTORAGE_USERNAME = "blobstorage.username";
	private static final String CTX_BLOBSTORAGE_PASSWORD = "blobstorage.password";

	private String base;
	private Login login;

	public BlobStorageClient() {
		setDefaults();
	}

	public BlobStorageClient(ServletContext context) {
		base = context.getInitParameter(CTX_BLOBSTORAGE_BASE);

		String username = context.getInitParameter(CTX_BLOBSTORAGE_USERNAME);
		String password = context.getInitParameter(CTX_BLOBSTORAGE_PASSWORD);
		login = new Login(username, password);

		setDefaults();
	}

	public BlobStorageClient(String base, Login login) {
		this.base = base;
		this.login = login;
		setDefaults();
	}

	private void setDefaults() {
		if (base == null) {
			base = DEFAULT_BASE;
		}
		if (login == null) {
			login = new Login(null, null);
		}
	}

	@Override
	public void post(Blob blob) throws IOException {
		String path = blob.getPath();
		try {
			HttpURLConnection connection = openConnection(path, "POST");

			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", blob.getMimeType());
			connection.setRequestProperty("Content-Length",
					String.valueOf(blob.getLength()));
			connection.setRequestProperty("Last-Modified",
					new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",
							Locale.US).format(blob.getLastMod()));

			OutputStream out = connection.getOutputStream();
			out.write(blob.getData());
			out.close();

			int responseCode = connection.getResponseCode();

			if (responseCode != 204 && responseCode != 200) {
				throw new IOException("Unexpected response code "
						+ responseCode);
			}
		} catch (Exception e) {
			throw new IOException("Failed to POST blob", e);
		}
	}

	@Override
	public Blob get(String path) throws IOException {
		try {
			HttpURLConnection connection = openConnection(path, "GET");
			connection.setDoInput(true);

			int responseCode = connection.getResponseCode();
			if (responseCode == 200) {
				String mimeType = String.valueOf(connection
						.getHeaderField("Content-Type"));
				Date lastMod = new Date(connection.getHeaderFieldLong(
						"Last-Modified", new Date().getTime()));
				byte[] data = BlobStreamTool.read(connection.getInputStream());
				boolean exists = true;

				Blob blob = new Blob(path, mimeType, data, exists, lastMod);
				return blob;
			} else if (responseCode == 404) {
				boolean exists = false;
				return new Blob(path, "text/plain", new byte[0], exists,
						new Date());
			} else {
				throw new IOException("Unexpected response code "
						+ responseCode);
			}
		} catch (Exception e) {
			throw new IOException("Failed to GET blob", e);
		}
	}

	@Override
	public boolean delete(String path) throws IOException {
		try {
			HttpURLConnection connection = openConnection(path, "DELETE");

			int responseCode = connection.getResponseCode();
			if (responseCode == 200 || responseCode == 204) {
				return true;
			} else if (responseCode == 404) {
				return false;
			} else {
				throw new IOException("Unexpected response code "
						+ responseCode);
			}
		} catch (Exception e) {
			throw new IOException("Failed to DELETE blob", e);
		}
	}

	private HttpURLConnection openConnection(String path, String method)
			throws IOException {
		String url = base
				+ URLEncoder.encode(path, "UTF-8").replace("%2F", "/");
		HttpURLConnection connection = (HttpURLConnection) new URL(url)
				.openConnection();

		connection.setRequestMethod(method);

		byte[] loginBytes = (login.getUsername() + ":" + login.getPassword())
				.getBytes("UTF-8");
		String authBase64 = Base64.getEncoder().encodeToString(loginBytes);
		connection.setRequestProperty("Authorization", "Basic " + authBase64);

		return connection;
	}
}
