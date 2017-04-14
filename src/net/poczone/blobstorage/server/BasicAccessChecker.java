package net.poczone.blobstorage.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.poczone.blobstorage.shared.AccessChecker;
import net.poczone.blobstorage.shared.Blob;
import net.poczone.blobstorage.shared.BlobStore;
import net.poczone.blobstorage.shared.Login;

public class BasicAccessChecker implements AccessChecker {
	public static final String ACCESS_FILE = ".access.txt";

	private BlobStore accessStore;

	public BasicAccessChecker(BlobStore accessStore) {
		this.accessStore = accessStore;
	}

	@Override
	public boolean hasAccess(Login login, String path, AccessType type)
			throws IOException {
		try {
			List<String> roots = getRoots(path);
			for (String root : roots) {
				Blob accessBlob = accessStore.get(root + ACCESS_FILE);
				if (accessBlob.exists()) {
					String subPath = path.substring(root.length());
					return checkAccess(login, subPath, type,
							accessBlob.getData());
				}
			}
			return true;
		} catch (Exception e) {
			throw new IOException("Failed to check access", e);
		}
	}

	private boolean checkAccess(Login login, String path, AccessType type,
			byte[] accessData) {
		String[] accessLines = new String(accessData).split("(\r?\n)+");
		for (String line : accessLines) {
			String[] parts = line.split("~");
			if (parts.length < 3) {
				continue;
			}

			String permissions = parts[0];
			String pathPattern = parts[1];
			String loginPattern = parts[2];

			String loginStr = login.getUsername() + ":" + login.getPassword();

			boolean permissionsOk = permissions.toUpperCase().contains(
					type.name().substring(0, 1));
			boolean pathOk = path.matches(pathPattern);
			boolean loginOk = loginStr.matches(loginPattern);

			if (permissionsOk && pathOk && loginOk) {
				return true;
			}
		}

		return false;
	}

	private List<String> getRoots(String path) {
		List<String> roots = new ArrayList<String>();

		while (path.contains("/")) {
			int lastSlash = path.lastIndexOf('/');

			path = path.substring(0, lastSlash);
			roots.add(path + "/");
		}

		roots.add("");

		return roots;
	}
}
