package com.wahlberger.velocity.database;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;

public class BinaryDatabase implements ServerProtectionDatabase {
	private Set<String> protectedServers = new HashSet<>();
	private final String filePath;
	private final Logger logger;

	public BinaryDatabase(String filePath, Logger logger) {
		this.filePath = filePath;
		this.logger = logger;

		this.readFromFile();
	}

	@Override
	public boolean isServerAuthRequired(String serverName) {
		return this.protectedServers.contains(serverName);
	}

	@Override
	public void deleteServerAuth(String serverName) {
		if (this.protectedServers.contains(serverName)) {
			this.protectedServers.remove(serverName);
			this.writeToFile();
		}
	}

	@Override
	public void addServerAuth(String serverName) {
		if (!this.protectedServers.contains(serverName)) {
			this.protectedServers.add(serverName);
			this.writeToFile();
		}
	}

	private synchronized void writeToFile() {
		try (DataOutputStream dos = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(this.filePath, false)))) {
			dos.writeInt(this.protectedServers.size());
			for (String server : this.protectedServers)
				dos.writeUTF(server);
			dos.close();
		} catch (IOException e) {
			this.logger.error(e.toString());
		}
	}

	private synchronized void readFromFile() {
		try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(this.filePath)))) {
			int count = dis.readInt();
			this.protectedServers.clear();
			while (this.protectedServers.size() < count) {
				this.protectedServers.add(dis.readUTF());
			}
		} catch (IOException e) {
			this.logger.error(e.toString());
		}
	}
}
