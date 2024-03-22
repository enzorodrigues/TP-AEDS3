package main.java.br.pucminas.aedsiii;

import main.java.br.pucminas.aedsiii.FileUtil.*;

import java.util.ArrayList;
import java.util.Date;

import main.java.br.pucminas.aedsiii.Entity.*;

public class App {

	private static String csvFilePath = "songs.csv";
	
	public static void main(String[] args) throws Exception {

	}
	
	@SuppressWarnings("deprecation")
	private static void uploadData() {
		TextFileReader base = new TextFileReader(csvFilePath);
		base.readLine();
		
		String line = base.readLine();
		while(line != null) {
			String [] musicData = line.split(";");
			String date = musicData[3]+"/"+musicData[4]+"/"+musicData[5];
			
			Music music = new Music(musicData[0], musicData[1].split(","), Byte.parseByte(musicData[2]),
									new Date(date), Integer.parseInt(musicData[6]), 
									Short.parseShort(musicData[7]), Long.parseLong(musicData[8]));
			
			addRecord(music);
			
			line = base.readLine();
		}
		
		base.close();
		
		System.out.println("Success import csv to database!");
	}
	
	private static void addRecord(Music music) {
		DataBaseAccess db = new DataBaseAccess();
		boolean success = db.createRecord(music);
		db.close();
		
		System.out.println((success ? "Success to add " : "Failed to add ")+music);
	}
	
	private static Music readRecord(int id){
		DataBaseAccess db = new DataBaseAccess();
		Music music = db.readRecord(id);
		db.close();
		
		return music;
	}
	
	private static boolean updateRecord(Music music) {
		DataBaseAccess db = new DataBaseAccess();
		boolean success = db.updateRecord(music);
		db.close();
		
		return success;
	}
	
	private static boolean deleteRecord(int id) {
		DataBaseAccess db = new DataBaseAccess();
		boolean success = db.deleteRecord(id);
		db.close();
		
		return success;
	}

}
