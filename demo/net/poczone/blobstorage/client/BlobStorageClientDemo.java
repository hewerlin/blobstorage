package net.poczone.blobstorage.client;

import java.io.IOException;

import net.poczone.blobstorage.shared.Blob;
import net.poczone.blobstorage.shared.Login;

public class BlobStorageClientDemo {
	public static void main(String[] args) throws IOException {
		String base = BlobStorageClient.BASE_POCZONE;
		Login login = new Login("user", "pass");

		BlobStorageClient client = new BlobStorageClient(base, login);

		String path = "a/b/demo.txt";
		String mimeType = "text/plain";
		byte[] data = "I was here.".getBytes();

		System.out.println("POST blob to " + path + "...");
		Blob blob = new Blob(path, mimeType, data);
		client.post(blob);
		System.out.println("=> Done.");

		System.out.println("GET blob from " + path + "...");
		Blob blob2 = client.get(path);
		if (blob2.exists()) {
			byte[] data2 = blob2.getData();

			System.out.println("=> Text: " + new String(data2));
		}

		System.out.println("DELETE blob from " + path);
		boolean deleted = client.delete(path);
		System.out.println("=> Deleted: " + deleted);
	}
}
