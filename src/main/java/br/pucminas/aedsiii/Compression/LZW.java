package main.java.br.pucminas.aedsiii.Compression;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import main.java.br.pucminas.aedsiii.App;

public class LZW {
	private static Dictionary initialDictionary;

	private LZW() { }
	
	public static int compress(String input, long size) {
		Dictionary dictonary = preProcess(input);
		
		List<Integer> encoded = new ArrayList<Integer>();
		String prefix = "";
		
		for(char actual : input.toCharArray()) {
			String symbol = prefix + actual;
			if(dictonary.containsKey(symbol)) {
				prefix = symbol;
			} else {
				encoded.add(dictonary.get(prefix));
				
				if(dictonary.size() < 256) {
					dictonary.put(symbol, dictonary.size());
				}

				prefix = actual+"";
			}
		}
		if(!prefix.isEmpty()) {
			encoded.add(dictonary.get(prefix));
		}
		
		return createCompressedFile(encoded, size);
	}

	public static boolean extract(int version) {
		File dir = new File(App.resourcePath+"backups\\BACKUP"+version);
		if(!dir.exists()) { return false; }
		
		HashMap<Integer, String> dict = getInitialDict(dir.getAbsolutePath());
		List<Integer> encoded = new ArrayList<Integer>(); 
		
		try {
			RandomAccessFile db = new RandomAccessFile(dir.getAbsolutePath()+"\\dataCompressao"+version+".db", "rw");
			while(db.getFilePointer() < db.length()) {
				encoded.add(db.read());
			}
			db.close();
		} catch (Exception e) {
			System.err.println("Error on read backup file. Version: " + version);
		}
		
		String extract = dict.get(encoded.remove(0));
		String prefix = new String(extract);
		String actual = "";
		
		for(int key : encoded) {
			if(dict.containsKey(key)) {
				actual = dict.get(key);
			} else {
				actual = actual+prefix.charAt(0);
			}
			
			extract += actual;
			if(dict.size() < 256) {
				prefix += actual.charAt(0);
				dict.put(dict.size(), prefix);
				prefix = new String(actual);
			}
		}
		
		createDecompressedFile(extract);
		
		// restore data
		backup(App.resourcePath+"data.db", App.resourcePath+"tmp.db", false);
		// restore index
		backup(App.resourcePath+"indexes\\index.db", App.resourcePath+"\\backups\\BACKUP"+version+"\\index.db", true);
		// restore inverted lists
		backup(App.resourcePath+"indexes\\invertedLists\\artists.db", App.resourcePath+"\\backups\\BACKUP"+version+"\\artists.db", true);
		backup(App.resourcePath+"indexes\\invertedLists\\name.db", App.resourcePath+"\\backups\\BACKUP"+version+"\\name.db", true);
		
		return true;
	}
		
	private static int createCompressedFile(List<Integer> encoded, long size) {
		int version = getVersion();
		File dir = new File(App.resourcePath + "\\backups\\BACKUP"+version);
		if (!dir.exists()){
		    dir.mkdirs();
		}
		
		try {
			RandomAccessFile db = new RandomAccessFile(dir.getAbsolutePath()+ "\\dataCompressao"+version+".db", "rw");
			for(int symbol : encoded) {
				db.writeByte(symbol);
			}
			double percent = ((double) db.length() / size)*100 ;
			System.out.printf("Compactacao de %.2f%%\n", 100-percent);
			db.close();
			
		} catch (Exception e) {
			System.err.println("Error on write backup. Version: "+version);
			e.printStackTrace();
		}
		
		saveInitialDict(dir.getAbsolutePath());
		
		// backup index
		backup(dir.getAbsolutePath()+"\\index.db", App.resourcePath+"indexes\\index.db", true);
		// backup inverted lists
		backup(dir.getAbsolutePath()+"\\artists.db", App.resourcePath+"indexes\\invertedLists\\artists.db", true);
		backup(dir.getAbsolutePath()+"\\name.db", App.resourcePath+"indexes\\invertedLists\\name.db", true);
		
		return version;
	}

	private static void createDecompressedFile(String descompact) {
		String[] data = descompact.split(App.DIVIDER);
		
		try {
			RandomAccessFile db = new RandomAccessFile(App.resourcePath+"tmp.db", "rw");
			db.writeInt(Integer.parseInt(data[0]));
			int i=1;
			while(i<data.length) {
				db.writeChar(data[i++].charAt(0)); 				// Lapide
				db.writeInt(Integer.parseInt(data[i++])); 		// Tamanho do registro
				db.writeInt(Integer.parseInt(data[i++])); 		// ID
				db.writeShort(Short.parseShort(data[i++])); 	// Tamanho do nome
				db.writeUTF(data[i++]);							// Nome
				byte artistCount = Byte.parseByte(data[i++]);	
				db.writeByte(artistCount);						// Contagem de artistas
				for(byte j=0; j<artistCount;j++) {
					db.writeShort(Short.parseShort(data[i++]));	// Tamanho do nome do artista
					db.writeUTF(data[i++]);						// Nome do artista
				}
				db.writeLong(Long.parseLong(data[i++]));		// Data de lancamento
				db.writeInt(Integer.parseInt(data[i++])); 		// Playlists
				db.writeShort(Short.parseShort(data[i++]));		// Ranking
				db.writeLong(Long.parseLong(data[i++]));		// Streams
			}
			db.close();
		} catch (Exception e) {
			System.err.println("Error on read backup!");
		}
	}
	
	private static void saveInitialDict(String path) {
		try {
			RandomAccessFile db = new RandomAccessFile(path+ "\\dict.db", "rw");
			for(String key : initialDictionary.keySet()) {
				db.writeUTF(key);
				db.write(initialDictionary.get(key));
			}
			db.close();
		} catch (Exception e) {
			System.err.println("Erron on save initial dict");
		}
		
	}
	
	private static HashMap<Integer, String> getInitialDict(String path) {
		HashMap<Integer, String> initialDict = new HashMap<Integer, String>();

		try {
			RandomAccessFile db = new RandomAccessFile(path+"\\dict.db", "rw");
			int key;
			String value;
			while(db.getFilePointer() < db.length()) {
				value = db.readUTF();
				key = db.read();
				initialDict.put(key, value);
			}
			db.close();
		} catch (Exception e) {
			System.err.println("Erron on read initial dict!");
		}
		return initialDict;
	}
	
	private static int getVersion() {
		File backupDir = new File(App.resourcePath+ "\\backups");
		File[] dirs = backupDir.listFiles(File::isDirectory);
		int greather = -1;
		if(dirs != null)
			for(File dir: dirs) {
				int version = Integer.parseInt(dir.getName().substring(6));
				if(version > greather) {
					greather = version;
				}
			}
		return greather+1;
	}
	
	private static void backup(String old, String backup, boolean copy) {
		File data = new File(old);
		data.delete();
		
		File backUp = new File(backup);
		if(copy) {
			try { Files.copy(backUp.toPath(), data.toPath(), StandardCopyOption.REPLACE_EXISTING); }
			catch (Exception e) { System.err.println("ERRO NA COPIA"); }
		} else {
			backUp.renameTo(data);
		}
	}
	
	private static Dictionary preProcess(String input) {
		initialDictionary = new Dictionary();
		String act;
		for(char actual : input.toCharArray()) {
			act = actual+"";
			if(!initialDictionary.containsKey(act)) {
				initialDictionary.put(act, initialDictionary.size());
			}
		}
		
		return initialDictionary.clone();
	}
}

class Dictionary extends HashMap<String, Integer> {

	private static final long serialVersionUID = 7122442633188928431L;

	public Dictionary clone() {
		Dictionary dict = new Dictionary();
		for(String key : this.keySet()) {
			dict.put(key, this.get(key));
		}
		return dict;
	}

}
