package main.java.br.pucminas.aedsiii.Database;

import java.io.IOException;
import java.io.RandomAccessFile;

import main.java.br.pucminas.aedsiii.Database.DTO.MusicDTO;
import main.java.br.pucminas.aedsiii.Entity.Music;
import main.java.br.pucminas.aedsiii.Indexes.Index;
import main.java.br.pucminas.aedsiii.Indexes.BTree.BTree;

public class DataBaseAccess {
	private static char GRAVESTONE_SIGNAL = '*';
	private RandomAccessFile db;
	private BTree indexDB = new BTree();
	
	public DataBaseAccess() {
		try {
			String path = System.getProperty("user.dir");
			db = new RandomAccessFile(path+"\\src\\main\\resources\\data.db", "rw");
		} catch(Exception e) {
			System.err.println("Error on open database.");
		}
	}
	
	public boolean createRecord(Music music) {
		try {
			music.setID(getID());
			long address = db.length();
			db.seek(address);
			
			byte[] musicByteArray = music.toByteArray();
			db.writeChar(' ');
			db.writeInt(musicByteArray.length);
			db.write(musicByteArray);
			indexDB.insertIndex(new Index(music.getID(), address));
			return true;
		} catch (Exception e) {
			System.err.println("Error on create record to: "+music);
			return false;
		}
	}
	
	public MusicDTO readRecord(int id) {
		if(!recordCanExists(id)) { return null; }

		MusicDTO dto = search(id);
		return dto != null ? dto : null;
	}
	
	public boolean deleteRecord(int id) {
		MusicDTO dto = readRecord(id);
		if(dto == null) { return false; }
		
		try {
			db.seek(dto.getGravestonePointer());
			db.writeChar(GRAVESTONE_SIGNAL);
		} catch(IOException e) {
			System.err.println("Error on delete record: "+id);
			return false;
		}
		
		return true;
	}
	
	public boolean updateRecord(Music music, MusicDTO dto) {
		try {
			byte[] newMusic = music.toByteArray();
			byte[] oldMusic = dto.getMusic().toByteArray();
			
			if(newMusic.length <= oldMusic.length) {
				db.seek(dto.getRecordPointer());
				db.write(newMusic);
			} else {
				db.seek(dto.getGravestonePointer());
				db.writeChar(GRAVESTONE_SIGNAL);
				long newAddress = db.length();
				db.seek(newAddress);
				db.writeChar(' ');
				db.writeInt(newMusic.length);
				db.write(newMusic);
				indexDB.updateIndex(music.getID(), newAddress);
			}
		} catch(IOException e) {
			System.err.println("Error on update record: "+music);
			return false;
		}
		
		return true;
	}
	
	public void close() {
		try {
			db.close();
			indexDB.close();
		} catch(Exception e) {
			System.err.println("Error on closing database.");
		}
	}
	
	// MARK: - Private Functions

	private MusicDTO search(int id) {
		int size;
		long recordPointer, gravestonePointer;
		byte[] recording;
		Music music = new Music();
		long address = indexDB.findIndex(id);
		
		try {
			db.seek(address);
			gravestonePointer = db.getFilePointer();
			char gravestone = db.readChar();
			
			size = db.readInt();
			
			recordPointer = db.getFilePointer();
			recording = new byte[size];

			db.read(recording);
			if(gravestone != GRAVESTONE_SIGNAL) {
				music.fromByteArray(recording);
				return new MusicDTO(music, gravestonePointer, recordPointer);
			}
		} catch (Exception e) { 
			System.err.println("Erro ao procurar id: "+address+" - "+ id);
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
			System.err.println("Error on validate ID.");
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
