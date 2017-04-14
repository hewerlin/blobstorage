package net.poczone.blobstorage.shared;

import java.io.IOException;

public interface AccessChecker {
	boolean hasAccess(Login login, String path, AccessType type) throws IOException;

	public enum AccessType {
		READ, WRITE
	}
}
