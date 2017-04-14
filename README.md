# blobstorage

POCZone.net blobstorage is a simple Java API for byte-array storage.

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
