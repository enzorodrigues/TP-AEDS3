package main.java.br.pucminas.aedsiii.FileUtil;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import main.java.br.pucminas.aedsiii.Entity.Music;
import main.java.br.pucminas.aedsiii.FileUtil.DTO.MusicDTO;

public class DataBaseAccess {
	private static char GRAVESTONE_SIGNAL = '*';
	private RandomAccessFile db;
	
	public DataBaseAccess() {
		try {
			db = new RandomAccessFile("data.db", "rw");
		} catch(Exception e) {
			System.out.println("Error on open database.");
		}
	}
	
	public boolean createRecord(Music music) {
		try {
			music.setID(getID());
			db.seek(db.length());
			
			byte[] musicByteArray = music.toByteArray();
			db.writeChar(' ');
			db.writeInt(musicByteArray.length);
			db.write(musicByteArray);
			return true;
		} catch (Exception e) {
			System.out.println("Error on create record to: "+music);
			return false;
		}
	}
	
	public Music readRecord(int id) {
		if(!recordCanExists(id)) { return null; }

		MusicDTO dto = search(id);
		return dto != null ? dto.getMusic() : null;
	}
	
	public boolean deleteRecord(int id) {
		if(!recordCanExists(id)) { return false; }
		
		MusicDTO dto = search(id);
		if(dto == null) { return false; }
		
		try {
			db.seek(dto.getGravestonePointer());
			db.writeChar(GRAVESTONE_SIGNAL);
		} catch(IOException e) {
			System.out.println("Error on delete record: "+id);
			return false;
		}
		
		return true;
	}
	
	public boolean updateRecord(Music music) {
		if(!recordCanExists(music.getID())) { return false; }
		
		MusicDTO dto = search(music.getID());
		if(dto == null) { return false; }
		
		try {
			byte[] newMusic = music.toByteArray();
			byte[] oldMusic = dto.getMusic().toByteArray();
			
			if(newMusic.length <= oldMusic.length) {
				db.seek(dto.getRecordPointer());
			} else {
				db.seek(dto.getGravestonePointer());
				db.writeChar(GRAVESTONE_SIGNAL);
				db.seek(db.length());
			}
			
			db.write(newMusic);
		} catch(IOException e) {
			System.out.println("Error on update record: "+music);
			return false;
		}
		
		return true;
	}
	
	public void close() {
		try {
			db.close();
		} catch(Exception e) {
			System.out.println("Error on closing database.");
		}
		
	}
	
	
	// MARK: - Private Functions

	private MusicDTO search(int id) {
		int size;
		long recordPointer, gravestonePointer;
		byte [] recording;
		boolean EOF = false;
		Music music = new Music();

		while(!EOF) {
			try {
				gravestonePointer = db.getFilePointer();
				char gravestone = db.readChar();
				
				size = db.readInt();
				
				recordPointer = db.getFilePointer();
				recording = new byte[size];

				db.read(recording);
				if(gravestone != GRAVESTONE_SIGNAL) {
					music.fromByteArray(recording);
					if(music.getID() == id) { 
						return new MusicDTO(music, gravestonePointer, recordPointer);
					}
				}
			} catch (EOFException e) { 
				EOF = true; 
			} catch(IOException e) {
				System.out.println("Error on searching music: "+ e);
				return null;
			}
		}
		
		return null;
	}
	
	private boolean recordCanExists(int id) {
		try {
			db.seek(0);
			int lastId = !isEmpty() ? db.readInt() : 0;
			if(id > lastId) {
				System.out.println("ID cannot exists. ID greater than the last ID.");
				return false;
			}
		} catch(IOException e) {
			System.out.println("Error on validate ID.");
			return false;
		}
		
		return true;
	}

	private int getID() throws IOException {
		db.seek(0);
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
