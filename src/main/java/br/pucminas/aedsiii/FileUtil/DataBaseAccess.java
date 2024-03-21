package main.java.br.pucminas.aedsiii.FileUtil;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import main.java.br.pucminas.aedsiii.Entity.Music;

public class DataBaseAccess {
	private RandomAccessFile db;
	
	public DataBaseAccess() throws Exception {
		db = new RandomAccessFile("data.db", "rw");
	}
	
	public void createRecord(Music music) throws IOException {
		db.seek(0);
		music.setID(getID());
		db.seek(db.length());
		
		byte[] musicByteArray = music.toByteArray();
		db.writeChar(' ');
		db.writeInt(musicByteArray.length);
		db.write(musicByteArray);
	}
	
	
	
	public Music readRecord(int id) throws IOException {
		db.seek(0);
		
		int lastId = !isEmpty() ? db.readInt() : 0;
		if(id > lastId) {
			System.out.println("ID not exists");
			return null;
		}
		
		int size;
		byte [] recording;
		boolean EOF = false;
		Music music = new Music();

		while(!EOF) {
			try {
				char gravestone = db.readChar();
				size = db.readInt();
				recording = new byte[size];
				db.read(recording);
				if(gravestone != '*') {
					music.fromByteArray(recording);
					if(music.getID() == id) { return music; }
				}
			} catch (EOFException e) { EOF = true; }
		}
		return null;
	}
	
	public void close() throws IOException {
		db.close();
	}
	
	
	// MARK: - Private Functions

	private int getID() throws IOException {
		if(isEmpty()) {
			db.writeInt(0);
			return 0;
		} else {
			int id = db.readInt()+1;
			db.seek(0);
			db.writeInt(id);
			return id;
		}
	}
	
	private boolean isEmpty() throws IOException {
		return db.length() == 0;
	}
}
