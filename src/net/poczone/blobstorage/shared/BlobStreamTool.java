package net.poczone.blobstorage.shared;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BlobStreamTool {
	public static byte[] read(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		read(in, out);
		return out.toByteArray();
	}

	public static void read(InputStream in, ByteArrayOutputStream out)
			throws IOException {
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
	}
}
