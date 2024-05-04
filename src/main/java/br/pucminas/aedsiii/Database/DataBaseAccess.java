package main.java.br.pucminas.aedsiii.Database;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashSet;

import main.java.br.pucminas.aedsiii.MyIO;
import main.java.br.pucminas.aedsiii.Database.DTO.MusicDTO;
import main.java.br.pucminas.aedsiii.Entity.Music;
import main.java.br.pucminas.aedsiii.Indexes.Index;
import main.java.br.pucminas.aedsiii.Indexes.BTree.BStarTree;
import main.java.br.pucminas.aedsiii.Indexes.InvertedLists.InvertedList;

/**
 * Classe responsavel pelo gerenciamento do
 * arquivo de dados.
 * 
 * @since TP01
 * @version 2
 */
public class DataBaseAccess {
	private static char GRAVESTONE_SIGNAL = '*';
	private static String SPLIT_SIGNAL = " ";
	private static String ARTISTS_SPLIT_SIGNAL = ", ";
	
	private RandomAccessFile db;
	private BStarTree indexDB = new BStarTree();
	private InvertedList artistIndex = new InvertedList("artists.db", (byte)50);
	private InvertedList musicNameIndex = new InvertedList("name.db", (byte)50);

	/**
	 * Intancia a conexao com o arquivo de dados. <br>
	 * Se nao existir: cria e define o cabeçalho.
	 */
	public DataBaseAccess() {
		try {
			String path = System.getProperty("user.dir");
			db = new RandomAccessFile(path+"\\src\\main\\resources\\data.db", "rw");
			if(db.length() == 0) {
				db.writeInt(-1);
			}
		} catch(Exception e) {
			System.err.println("Error on open database.");
		}
	}
	
	/**
	 * Adiciona uma nova musica ao arquivo de dados.
	 * @param music - Nova musica
	 * @return boolean - Adicionou?
	 */
	public boolean createRecord(Music music) {
		try {
			music.setID(getID());
			long address = db.length();
			db.seek(address);
			
			byte[] musicByteArray = music.toByteArray();
			db.writeChar(' ');
			db.writeInt(musicByteArray.length);
			db.write(musicByteArray);

			indexing(music, address);

			return true;
		} catch (Exception e) {
			System.err.println("Error on create record to: "+music);
			return false;
		}
	}
	
	/**
	 * Realiza a indexação da musica nos arquivos de indices
	 * @param music - Musica salva
	 * @param address - Endereço da musica no arquivo de dados
	 */
	private void indexing(Music music, long address) {
		indexDB.insertIndex(new Index(music.getID(), address));
		
		musicNameIndex.createTerms(music.getName().split(SPLIT_SIGNAL), music.getID());
		
		artistIndex.createTerms(music.artistsConcat().split(ARTISTS_SPLIT_SIGNAL), music.getID());
	}
	
	/**
	 * Busca uma musica por ID no arquivo de dados.
	 * @param id - ID a procurar
	 * @return MusicDTO - Musica e seus endereços
	 */
	public MusicDTO readRecord(int id) {
		if(!recordCanExists(id)) { return null; }

		MusicDTO dto = search(id);
		return dto;
	}
	
	/**
	 * Deleta uma musica no arquivo de dados pelo ID.<br>
	 * Remove também suas referencias nos arquivos de indice
	 * @param id - ID a deletar
	 * @return boolean - Removido? 
	 */
	public boolean deleteRecord(int id) {
		MusicDTO dto = readRecord(id);
		if(dto == null) { return false; }
		
		try {
			db.seek(dto.getGravestonePointer());
			db.writeChar(GRAVESTONE_SIGNAL);
			artistIndex.deleteIdFromTerms(dto.getMusic().artistsConcat().split(SPLIT_SIGNAL), id);
			musicNameIndex.deleteIdFromTerms(dto.getMusic().getName().split(ARTISTS_SPLIT_SIGNAL), id);
		} catch(IOException e) {
			System.err.println("Error on delete record: "+id);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Realiza a atualização de uma musica no arquivo de dados.<br>
	 * Caso necessário, atualiza também suas referencias nos arquivos de indices
	 * @param music - Musica atualizada
	 * @param dto - Objeto com informações primárias da musica
	 * @return boolean - Atualizado nos arquivos? 
	 */
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
			
			if(!music.getName().equalsIgnoreCase(dto.getMusic().getName())) {
				updateIndexes(musicNameIndex, dto.getMusic().getName().split(SPLIT_SIGNAL), 
							  music.getName().split(SPLIT_SIGNAL), music.getID());
			}
			
			if(!music.artistsConcat().equalsIgnoreCase(dto.getMusic().artistsConcat())) {
				updateIndexes(artistIndex, dto.getMusic().artistsConcat().split(ARTISTS_SPLIT_SIGNAL), 
						  	  music.artistsConcat().split(ARTISTS_SPLIT_SIGNAL), music.getID());
			}
			
		} catch(IOException e) {
			System.err.println("Error on update record: "+music);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Encerra a conexão com os arquivos de dados e indices
	 */
	public void close() {
		try {
			db.close();
			indexDB.close();
			musicNameIndex.close();
			artistIndex.close();
		} catch(Exception e) {
			System.err.println("Error on closing database.");
		}
	}
	
	/**
	 * Busca e imprime as musica em que seu nome contenha todos 
	 * os termos procurados e também os nomes dos 
	 * artistas participantes obedeçam a todos termos procurados
	 * @param nameTerms - Termos da busca para o nome da musica
	 * @param artistTerms - Termos da busca para os nomes dos artistas
	 */
	public void searchByMusicNameAndArtists(String[] nameTerms, String[] artistTerms) {
		Integer[] nameIDs = musicNameIndex.searchTerm(nameTerms);
		Integer[] artistIDs = artistIndex.searchTerm(artistTerms);
		
		HashSet<Integer> equals = new HashSet<Integer>(Arrays.asList(nameIDs));
		equals.retainAll(Arrays.asList(artistIDs));
		
		MusicDTO dto;
		for(int id: equals) {
			dto = search(id);
			if(dto != null) {
				System.out.println(dto.getMusic().toString()); 
			}
		}
	}
	
	/**
	 * Busca e imprime as musica em que seu nome contenha todos 
	 * os termos procurados
	 * @param terms - Termos da busca
	 */
	public void searchByMusicName(String[] terms) {
		MusicDTO dto;
		Integer[] ids = musicNameIndex.searchTerm(terms);
		for(int id: ids) {
			dto = search(id);
			if(dto != null) {
				System.out.println(dto.getMusic().toString()); 
			}
		}
	}
	
	/**
	 * Busca e imprime as musica em que os nomes dos 
	 * artistas participantes obedeçam a todos termos procurados
	 * @param terms - Termos da busca
	 */
	public void searchByArtistName(String[] terms) {
		MusicDTO dto;
		Integer[] ids = artistIndex.searchTerm(terms);
		for(int id: ids) {
			dto = search(id);
			if(dto != null) {
				System.out.println(dto.getMusic().toString()); 
			}
		}
	}
	
	/**
	 * Atualiza no arquivo de lista invertida os termos existentes na musica. <br>
	 * Define quais termos sao novos e quais nao aparecem mais. Adiciona o ID aos termos novos
	 * e exclui o ID dos termos nao utilizados.
	 * @param index - Lista invertida a ser atualizada
	 * @param oldTerms - Termos da musica antes da atualização
	 * @param newTerms - Termos da musica apos atualização
	 * @param id - ID atualizado
	 */
	private void updateIndexes(InvertedList index, String[] oldTerms, String[] newTerms, int id) {
		HashSet<String> oldUniques = new HashSet<String>(Arrays.asList(oldTerms));
		HashSet<String> newUniques = new HashSet<String>(Arrays.asList(newTerms));
		HashSet<String> equals = new HashSet<String>();

		equals.addAll(Arrays.asList(oldTerms));
		equals.retainAll(Arrays.asList(newTerms));

		oldUniques.removeAll(equals);
		newUniques.removeAll(equals);
		equals.clear();
		equals = null;

		oldTerms = new String[oldUniques.size()];
		oldUniques.toArray(oldTerms);
		oldUniques.clear();
		oldUniques = null;

		newTerms = new String[newUniques.size()];
		newUniques.toArray(newTerms);
		newUniques.clear();
		newUniques = null;
		
		index.updateTerms(oldTerms, newTerms, id);
	}
	
	/**
	 * Busca a partir do arquivo de indices uma musica pelo ID
	 * @param id - ID procurado
	 * @return MusicDTO - Musica procurada e endereços uteis.
	 */
	private MusicDTO search(int id) {
		int size;
		long recordPointer, gravestonePointer;
		byte[] recording;
		Music music;
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
				music = Music.fromByteArray(recording);
				return new MusicDTO(music, gravestonePointer, recordPointer);
			}
		} catch (Exception e) { 
			System.err.println("Erro ao procurar id: "+ id +" - endereco: "+address);
		}

		return null;
	}

	/**
	 * Valida a existencia de um ID no arquivo de dados,
	 * evitando o acesso desnecessário aos arquivos.
	 * @param id - ID procurado
	 * @return boolean - Pode existir?
	 */
	private boolean recordCanExists(int id) {
		try {
			db.seek(0);
			int lastId = db.readInt();
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

	/**
	 * Retorna o proximo ID a ser utilizado. <br>
	 * Le o ultimo ID utilizado, define o proximo e atualiza no cabeçalho do arquivo.
	 * @return int - Novo ID
	 * @throws IOException
	 */
	private int getID() throws IOException {
		db.seek(0);
		int id = db.readInt()+1;
		db.seek(0);
		db.writeInt(id);
		return id;
	}
}