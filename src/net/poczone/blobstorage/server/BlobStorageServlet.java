package net.poczone.blobstorage.server;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.poczone.blobstorage.shared.AccessChecker;
import net.poczone.blobstorage.shared.AccessChecker.AccessType;
import net.poczone.blobstorage.shared.Blob;
import net.poczone.blobstorage.shared.BlobStore;
import net.poczone.blobstorage.shared.BlobStreamTool;
import net.poczone.blobstorage.shared.Login;

public class BlobStorageServlet extends HttpServlet {
	private static final String AUTH_REALM = "BlobStorage";

	private static final long serialVersionUID = 509390444479091051L;

	private AccessChecker accessChecker;
	private BlobStore store;

	@Override
	public void init() throws ServletException {
		store = new DatabaseBlobStore(getServletContext());
		accessChecker = new BasicAccessChecker(store);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Login login = getLogin(req);
		String path = getPath(req);

		if (login == null) {
			requestAuth(resp);
			return;
		} else if (!accessChecker.hasAccess(login, path, AccessType.READ)) {
			sendForbidden(resp);
			return;
		}

		Blob blob = store.get(path);
		if (blob.exists()) {
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentType(blob.getMimeType());
			resp.setContentLength(blob.getLength());
			resp.setDateHeader("Last-Modified", blob.getLastMod().getTime());
			resp.getOutputStream().write(blob.getData());
		} else {
			sendNotFound(resp);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Login login = getLogin(req);
		String path = getPath(req);

		if (login == null) {
			requestAuth(resp);
			return;
		} else if (!accessChecker.hasAccess(login, path, AccessType.WRITE)) {
			sendForbidden(resp);
			return;
		}

		byte[] data = BlobStreamTool.read(req.getInputStream());
		String mimeType = String.valueOf(req.getHeader("Content-Type"));
		long lastModTime = req.getDateHeader("Last-Modified");
		Date lastMod = lastModTime != 0 ? new Date(lastModTime) : new Date();
		boolean exists = true;

		Blob blob = new Blob(path, mimeType, data, exists, lastMod);

		store.post(blob);
		sendOK(resp);
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Login login = getLogin(req);
		String path = getPath(req);

		if (login == null) {
			requestAuth(resp);
			return;
		} else if (!accessChecker.hasAccess(login, path, AccessType.WRITE)) {
			sendForbidden(resp);
			return;
		}

		if (store.delete(path)) {
			sendOK(resp);
		} else {
			sendNotFound(resp);
		}
	}

	private Login getLogin(HttpServletRequest req) {
		try {
			String auth = req.getHeader("Authorization");
			if (auth == null) {
				return null;
			}

			String[] authParts = auth.split(" ");
			if (authParts.length != 2
					|| !authParts[0].equalsIgnoreCase("Basic")) {
				return null;
			}

			String decoded = new String(Base64.getDecoder()
					.decode(authParts[1]), "UTF-8");

			String[] userPass = decoded.split(":", 2);
			if (authParts.length != 2) {
				return null;
			}

			return new Login(userPass[0], userPass[1]);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private String getPath(HttpServletRequest req) throws IOException {
		String uri = req.getRequestURI();
		String contextPath = getServletContext().getContextPath();
		if (uri.startsWith(contextPath)) {
			uri = uri.substring(contextPath.length());
		}
		if (uri.startsWith("/")) {
			uri = uri.substring(1);
		}
		uri = URLDecoder.decode(uri, "UTF-8");
		return uri;
	}

	private void requestAuth(HttpServletResponse resp) {
		resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		resp.setHeader("WWW-Authenticate", "Basic realm=\"" + AUTH_REALM + "\"");
	}

	private void sendOK(HttpServletResponse resp) throws IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getOutputStream().print("OK :-)");
	}

	private void sendForbidden(HttpServletResponse resp) throws IOException {
		resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
	}

	private void sendNotFound(HttpServletResponse resp) throws IOException {
		resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Blob not found");
	}
}
