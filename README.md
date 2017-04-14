# blobstorage

[POCZone.net](https://poczone.net/) blobstorage is a simple Java API for byte-array storage.

It is used as a storage engine for various [POCZone.net](https://poczone.net/) applications.

The blobstorage service offers a straight-forward HTTP API with POST/GET/DELETE functionality. The blobs are stored in a SQL database. Clients can access the blobs from anywhere via the HTTP API or the Java BlobStorageClient accessor class.

## Client Usage

Initialize a BlobStorageClient like this
```java
String base = BlobStorageClient.BASE_POCZONE; // "https://poczone.net/blobstorage/";
Login login = new Login("username", "password");

BlobStorageClient client = new BlobStorageClient(base, login);
```

Store a Blob like this
```java
String path = "a/b/demo.txt";
String mimeType = "text/plain";
byte[] data = "I was here.".getBytes();

Blob blob = new Blob(path, mimeType, data);
client.post(blob);
```

Retrieve a Blob like this
```
Blob blob2 = client.get(path);
if(blob2.exists()) {
	byte[] data2 = blob2.getData();

	// ...
}
```

Delete a Blob like this
```java
boolean deleted = client.delete(path);
// ...
```
