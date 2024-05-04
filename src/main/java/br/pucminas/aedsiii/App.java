package main.java.br.pucminas.aedsiii;

import java.util.Date;

import main.java.br.pucminas.aedsiii.Database.DataBaseAccess;
import main.java.br.pucminas.aedsiii.Database.DTO.MusicDTO;
import main.java.br.pucminas.aedsiii.Entity.Music;
import main.java.br.pucminas.aedsiii.FileUtil.TextFileReader;

/**
 * Classe raiz da aplicacao. Inicia todas as conexoes e realiza
 * a interacao com o usuario.
 * 
 * @since TP01
 * @version 2
 */
public class App {
	private static String csvFilePath = "\\src\\main\\resources\\popularSpotifySongs.csv";
	private static DataBaseAccess db;
	
	/**
	 * Realiza a leitura da base de dados e insere as musicas
	 * no arquivo de dados.
	 */
	@SuppressWarnings("deprecation")
	private static void uploadData() {
		String path = System.getProperty("user.dir");
		TextFileReader base = new TextFileReader(path+csvFilePath);
		base.readLine();
		
		MyIO.println("\nImportando base de dados...");
		String[] musicData;
		String date;
		Music music;
		int total=0;
		
		String line = base.readLine();
		while(line != null) {
			musicData = line.split(";");
			date = musicData[3]+"/"+musicData[4]+"/"+musicData[5];
			
			music = new Music(musicData[0], musicData[1].split(", "), Byte.parseByte(musicData[2]),
									new Date(date), Integer.parseInt(musicData[6]), 
									Short.parseShort(musicData[7]), Long.parseLong(musicData[8]));
			db.createRecord(music);
			total++;
			line = base.readLine();
		}
		base.close();
		MyIO.println("Sucesso na importacao do .csv para base de dados! Total: "+total+" registros.\n");
	}
	
	/**
	 * Função teste para listagem das musicas salvas no arquivo de dados.
	 */
	private static void list() {
		for(int i=0; i<951; i++) {
			MusicDTO dto = db.readRecord(i);
			if(dto != null) {
				MyIO.println("ID encontrado: " + i);
				MyIO.println(dto.getMusic().toString());
			} else {
				MyIO.println("ID nao existente na base de dados.");
			}
		}
	}

	/**
	 * Exibição do menu inicial e direcionamento para aos demais menus.
	 */
	private static void initalMenu() {
		int option;
		do {
			MyIO.println("TP02 - AEDS III (Spotify Musics): MENU INICIAL");
			MyIO.println("1 - Importar base de dados");
			MyIO.println("2 - Adicionar nova musica ");
			MyIO.println("3 - Buscar musica por ID");
			MyIO.println("4 - Buscar musica por nome");
			MyIO.println("5 - Buscar musica por artista");
			MyIO.println("6 - Buscar musica por nome e artista");
			MyIO.println("7 - Apagar musica por ID");
			MyIO.println("9 - SAIR");
			MyIO.print("Selecao: ");
			option = MyIO.readInt();
			
			switch(option) {
				case 1:
					uploadData();
					break;
				case 2:
					createMusicMenu();
					break;
				case 3:
					searchMusicByIdMenu();
					break;
				case 4:
					searchMusicByNameMenu();
					break;
				case 5:
					searchMusicByArtistsMenu();
					break;
				case 6:
					searchMusicByNameAndArtistsMenu();
					break;
				case 7:
					deleteMusicByIdMenu();
					break;
				case 9:
					break;
				default:
					MyIO.println("OPCAO INVALIDA");
			}
		} while(option != 9);
		
		MyIO.println("Ate logo...");
	}
	
	/**
	 * Menu para adicionar uma nova musica a base de dados.
	 */
	private static void createMusicMenu() {
		String name, artists, date;
		int playlists;
		short rank;
		long streams;

		MyIO.println("\n\nTP02 - AEDS III (Spotify Musics): ADICIONAR MUSICA");
		
		name = MyIO.readLine("Nome: ");
		artists =  MyIO.readLine("Artistas (separados por virgula): ");
		date =  MyIO.readLine("Data de lancamento(yyyy/MM/dd): ");
		playlists =  MyIO.readInt("Numero de playlist adicionadas: ");
		rank = MyIO.readShort("Ranking no Spotify Charts: ");
		streams = MyIO.readLong("Numero de streams: ");
		
		Music music = new Music(name, artists, date, playlists, rank, streams);
		boolean success = db.createRecord(music);
		MyIO.println("\n"+music.toString());
		MyIO.println((success? "Musica adicionada a base de dados." : "Parece que houve um erro. Tente novamente.")+"\n\n");
	}
	
	/**
	 * Menu para buscar musicas, por <strong>ID</strong>, na base de dados.
	 * <br> Caso o ID exista, é possivel atualiza-lo.
	 */
	private static void searchMusicByIdMenu() {
		String update;
		int id;
		MyIO.println("\n\nTP02 - AEDS III (Spotify Musics): BUSCAR MUSICA");
		id = MyIO.readInt("ID da musica: ");
		
		MusicDTO dto = db.readRecord(id);
		if(dto != null) {
			MyIO.println("ID encontrado:" + id);
			MyIO.println(dto.getMusic().toString());
			update = MyIO.readLine("Atualizar? (S - SIM | N - NAO): ");
			if(update.equalsIgnoreCase("s")) {
				updateMusicMenu(dto);
			}
		} else {
			MyIO.println("ID nao existente na base de dados.");
		}
		MyIO.println("\n");
	}
	
	/**
	 * Menu para buscar musicas, por <strong>nome</strong>, na base de dados.
	 */
	private static void searchMusicByNameMenu() {
		MyIO.println("\n\nTP02 - AEDS III (Spotify Musics): BUSCAR MUSICA POR NOME");
		String name = MyIO.readLine("Nome: ");
		
		db.searchByMusicName(name.split(" "));
		MyIO.println("\n");
	}
	
	/**
	 * Menu para buscar musicas, por <strong>artistas</strong>, na base de dados.
	 */
	private static void searchMusicByArtistsMenu() {
		MyIO.println("\n\nTP02 - AEDS III (Spotify Musics): BUSCAR MUSICA POR ARTISTA");
		String artists = MyIO.readLine("Artistas: ");
		
		db.searchByArtistName(artists.split(" "));
		MyIO.println("\n");
	}
	
	/**
	 * Menu para buscar musicas, por <strong>nome</strong> e 
	 * <strong>artistas</strong>, na base de dados.
	 */
	private static void searchMusicByNameAndArtistsMenu() {
		MyIO.println("\n\nTP02 - AEDS III (Spotify Musics): BUSCAR MUSICA POR NOME E ARTISTA");
		String name = MyIO.readLine("Nome: ");
		String artists = MyIO.readLine("Artistas: ");
		
		db.searchByMusicNameAndArtists(name.split(" "), artists.split(" "));
		MyIO.println("\n");
	}

	/**
	 * Menu para atualizar as informações de uma musica na base de dados
	 * @param dto - Musica e seus endereço na base de dados
	 */
	private static void updateMusicMenu(MusicDTO dto) {
		Music music = dto.getMusic().clone();
		String updates;
		MyIO.println("\n\nTP02 - AEDS III (Spotify Musics): ATUALIZAR MUSICA: " + music.getID());
		MyIO.println("Nao atualizar -> '.'");
		
		updates = MyIO.readLine("Nome: ");
		if(!updates.equals(".")) { music.setName(updates); }
		updates = MyIO.readLine("Artistas: ");
		if(!updates.equals(".")) { music.setArtists(updates.split(",")); }
		updates = MyIO.readLine("Data de lancamento(yyyy/MM/dd): ");
		if(!updates.equals(".")) { music.setReleaseDate(new Date(updates)); }
		updates = MyIO.readLine("Playlists: ");
		if(!updates.equals(".")) { music.setInSpotifyPlaylists(Integer.parseInt(updates)); }
		updates = MyIO.readLine("Ranking: ");
		if(!updates.equals(".")) { music.setRankSpotifyCharts(Short.parseShort(updates)); }
		updates = MyIO.readLine("Streams: ");
		if(!updates.equals(".")) { music.setSpotifyStreams(Long.parseLong(updates)); }
		
		boolean success = db.updateRecord(music, dto);
		MyIO.println("\n" + (success? "Musica Atualizada: " : "Falha ao atualizar musica: ") + music.toString());
	}
	
	/**
	 * Menu para apagar uma musica da base de dados.
	 */
	private static void deleteMusicByIdMenu() {
		int id;
		MyIO.println("\n\nTP02 - AEDS III (Spotify Musics): APAGAR MUSICA");
		id = MyIO.readInt("ID da musica: ");
		boolean success = db.deleteRecord(id);
		
		MyIO.println((success ? "Sucesso ao apagar musica: " : "Falha ao apagar musica: ")+id+ "\n\n");
	}
	
	/**
	 * Função inicial da aplicação, configura charset instancia conexao com
	 * a base de dados e inicia a interação com usuário.
	 * @param args
	 */
	public static void main(String[] args) {
		MyIO.setCharset("UTF-8");
		db = new DataBaseAccess();
		initalMenu();
		db.close();
	}
}
