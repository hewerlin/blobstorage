package net.poczone.blobstorage.shared;

import java.io.IOException;

public interface BlobStore {
	void post(Blob blob) throws IOException;

	Blob get(String path) throws IOException;

	boolean delete(String path) throws IOException;
}
