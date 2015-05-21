package com.amp.tsp.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class Downloader implements Runnable {
	private boolean stop = false;
	private boolean finished = false;
	private long fileSize;
	private long total;

	private final URL url;
	private final File file;

	public Downloader(URL url, File file) {
		this.url = url;
		this.file = file;
	}

	@Override
	public void run() {
		try {
			download();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void download() throws IOException {
		System.out.println("Downloading");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		fileSize = connection.getContentLengthLong();
		connection.disconnect();

		ReadableByteChannel rbc = Channels.newChannel(url.openStream());
		FileOutputStream fos = new FileOutputStream(file);
		long trans = 0;
		total = 0;
		do {
			if (stop) {
				rbc.close();
				fos.close();
				file.delete();
				finished = true;
				return;
			}
			trans = fos.getChannel().transferFrom(rbc, total, 1024);
			total += trans;
			System.out.println("Total: " + total);
		} while (trans == 1024);
		rbc.close();
		fos.close();
		finished = true;
	}

	public void stop() {
		stop = true;
	}

	public int progress() {
		return (int) ((((double) total / (double) fileSize)) * 100);
	}
	
	public boolean isFinished(){
		return finished;
	}

}
