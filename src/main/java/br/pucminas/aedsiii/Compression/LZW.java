package main.java.br.pucminas.aedsiii.Compression;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import main.java.br.pucminas.aedsiii.App;

/**
 * Classe para compressao da base de dados utilizando o algoritmo LZW.
 * 
 * @since TP03
 * @version 1
 */
public class LZW {
	private static Dictionary initialDictionary;

	private LZW() { }
	
	/**
	 * Funcao que realiza a compressao da base de dados. Recebe a base de dados em formato de string,
	 * realiza o pre-processamento para obter o dicionario inicial, monta o dicionario e gera a compressao.
	 * Caso o dicionario ultrapasse o tamanho de 1 byte, é feito o congelamento.
	 * @param input - base de dados no formato de string
	 * @param size - tamanho da base em bytes, para comparativo
	 * @return Int - versao gerada
	 */
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
		int version = createCompressedFile(encoded, size);
		return version;
	}

	/**
	 * Realiza a descompactacao do backup selecionado. Obtem o dicionario inicial salvo invertendo os pares chave-valor,
	 * faz a descompactacao reconstruindo o dicionario e substitui os arquivos de dados e indices atuais.
	 * @param version - versao do backup
	 * @return Boolean - VERDADEIRO a versao do backup existir e for possivel descompactar. FALSO caso o backup nao exista.
	 */
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
		
		createDecompressedFile(extract, version);
		
		return true;
	}

	/**
	 * Salva os arquivos para backup. Obtem a versao do backup, criar a pasta e arquivo de dados comprimido.
	 * Calcula a porcentagem de compressao entre o backup e o arquivo atual. Salva o dicionario inicial e realiza copias
	 * dos arquivos de indices.
	 * @param encoded - Array de simbolos gerados pela compactacao da base de dados
	 * @param size - tamanho do arquivo original, em bytes, para comparacao.
	 * @return Int - versao gerada
	 */
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

	/**
	 * Gera o arquivo de dados apos a descompactacao. Separa os dados obtidos pela descompactacao e
	 * retorna para o formato do arquivo de dados. Realiza a substituicao dos arquivos atuais pelos arquivos
	 * restaurados.
	 * @param descompact - base de dados descompactada em formato de string
	 * @param version - versao descompactada
	 */
	private static void createDecompressedFile(String descompact, int version) {
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
		
		// restore data
		backup(App.resourcePath+"data.db", App.resourcePath+"tmp.db", false);
		// restore index
		backup(App.resourcePath+"indexes\\index.db", App.resourcePath+"\\backups\\BACKUP"+version+"\\index.db", true);
		// restore inverted lists
		backup(App.resourcePath+"indexes\\invertedLists\\artists.db", App.resourcePath+"\\backups\\BACKUP"+version+"\\artists.db", true);
		backup(App.resourcePath+"indexes\\invertedLists\\name.db", App.resourcePath+"\\backups\\BACKUP"+version+"\\name.db", true);
	}
	
	/**
	 * Salva o dicionario inicial na pasta de backup gerada
	 * @param path - caminho da pasta do backup gerado
	 */
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
	
	/**
	 * Obtem o dicionario inicial para a descompactacao. Gera um dicionario
	 * invertendo os pares chave-valor.
	 * @param path - caminho da pasta de backup
	 * @return HashMap<Integer, String> - Dicionario invertido
	 */
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
	
	/**
	 * Obtem a proxima versao do backup a gerar baseado nas versoes existem.
	 * @return Int - versao do backup
	 */
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

	/**
	 * Realiza a subtituicao dos arquivo do aquivo antigo pelo novo
	 * @param old - caminho do arquivo a substituir
	 * @param backup - caminho do novo arquivo
	 * @param copy - true: copiar / false: substituir
	 */
	private static void backup(String old, String backup, boolean copy) {
		File data = new File(old);
		data.delete();
		
		File backUp = new File(backup);
		if(copy) {
			try { Files.copy(backUp.toPath(), data.toPath(), StandardCopyOption.REPLACE_EXISTING); }
			catch (Exception e) { System.err.println("ERROR ON COPY BACKUP"); }
		} else {
			backUp.renameTo(data);
		}
	}

	/**
	 * Pre-processamento da atual base de dados. Gera o dicionario inicial para compactacao
	 * obtendo os simbolos unicos.
	 * @param input - base de dados em formato de string
	 * @return Dictionary - dicionario inicial
	 */
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
