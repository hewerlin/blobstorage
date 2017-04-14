package net.poczone.blobstorage.shared;

import java.util.Date;

public class Blob {
	private String path;
	private String mimeType;
	private byte[] data;
	private boolean exists;
	private Date lastMod;

	public Blob(String path, String mimeType, byte[] data) {
		this.path = path;
		this.mimeType = mimeType;
		this.data = data;
		this.exists = true;
		this.lastMod = new Date();
	}

	public Blob(String path, String mimeType, byte[] data, boolean exists,
			Date lastMod) {
		this.path = path;
		this.mimeType = mimeType;
		this.data = data;
		this.exists = exists;
		this.lastMod = lastMod;
	}

	public String getPath() {
		return path;
	}

	public String getMimeType() {
		return mimeType;
	}

	public byte[] getData() {
		if (data == null) {
			data = new byte[0];
		}
		return data;
	}

	public Date getLastMod() {
		return lastMod;
	}

	public boolean exists() {
		return exists;
	}

	public int getLength() {
		return getData().length;
	}
}
