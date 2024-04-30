package main.java.br.pucminas.aedsiii;

import java.util.Date;

import main.java.br.pucminas.aedsiii.Database.DataBaseAccess;
import main.java.br.pucminas.aedsiii.Database.DTO.MusicDTO;
import main.java.br.pucminas.aedsiii.Entity.Music;
import main.java.br.pucminas.aedsiii.FileUtil.TextFileReader;

public class App {
	private static String csvFilePath = "\\src\\main\\resources\\popularSpotifySongs.csv";
	private static DataBaseAccess db;
	private static int max = 110;
	
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
			
			music = new Music(musicData[0], musicData[1].split(","), Byte.parseByte(musicData[2]),
									new Date(date), Integer.parseInt(musicData[6]), 
									Short.parseShort(musicData[7]), Long.parseLong(musicData[8]));
			addRecord(music);
			total++;
//			if(total == max) {
//				break;
//			}
			line = base.readLine();
		}
		base.close();
		MyIO.println("Sucesso na importacao do .csv para base de dados! Total: "+total+" registros.\n");
	}
	
	private static void list() {
		for(int i=0; i<max; i++) {
			MusicDTO dto = readRecord(i);
			if(dto != null) {
				MyIO.println("ID encontrado: " + i);
				MyIO.println(dto.getMusic().toString());
			} else {
				MyIO.println("ID nao existente na base de dados.");
			}
		}
	}
	
	private static boolean addRecord(Music music) {
		return db.createRecord(music);
	}
	
	private static MusicDTO readRecord(int id){
		return db.readRecord(id);
	}
	
	private static boolean updateRecord(Music music, MusicDTO dto) {
		return db.updateRecord(music, dto);
	}
	
	private static boolean deleteRecord(int id) {
		return db.deleteRecord(id);
	}

	private static void initalMenu() {
		int option;
		do {
			MyIO.println("TP01 - AEDS III (Spotify Musics): MENU INICIAL");
			MyIO.println("1 - Importar base de dados");
			MyIO.println("2 - Adicionar nova musica ");
			MyIO.println("3 - Buscar musica por ID");
			MyIO.println("4 - Apagar musica por ID");
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
					deleteMusicByIdMenu();
					break;
				case 9:
					//list();
					break;
				default:
					MyIO.println("OPCAO INVALIDA");
			}
		} while(option != 9);
		
		MyIO.println("Ate logo...");
	}
	
	private static void createMusicMenu() {
		String name, artists, date;
		int playlists;
		short rank;
		long streams;

		MyIO.println("\n\nTP01 - AEDS III (Spotify Musics): ADICIONAR MUSICA");
		
		name = MyIO.readLine("Nome: ");
		artists =  MyIO.readLine("Artistas (separados por virgula): ");
		date =  MyIO.readLine("Data de lancamento(yyyy/MM/dd): ");
		playlists =  MyIO.readInt("Numero de playlist adicionadas: ");
		rank = MyIO.readShort("Ranking no Spotify Charts: ");
		streams = MyIO.readLong("Numero de streams: ");
		
		Music music = new Music(name, artists, date, playlists, rank, streams);
		boolean success = addRecord(music);
		MyIO.println("\n"+music.toString());
		MyIO.println((success? "Musica adicionada a base de dados." : "Parece que houve um erro. Tente novamente.")+"\n\n");
	}
	
	private static void searchMusicByIdMenu() {
		String update;
		int id;
		MyIO.println("\n\nTP01 - AEDS III (Spotify Musics): BUSCAR MUSICA");
		id = MyIO.readInt("ID da musica: ");
		
		MusicDTO dto = readRecord(id);
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
	
	private static void updateMusicMenu(MusicDTO dto) {
		Music music = dto.getMusic().clone();
		String updates;
		MyIO.println("\n\nTP01 - AEDS III (Spotify Musics): ATUALIZAR MUSICA: " + music.getID());
		MyIO.println("Nao atualizar -> '...'");
		
		updates = MyIO.readLine("Nome: ");
		if(!updates.equals("...")) { music.setName(updates); }
		updates = MyIO.readLine("Artistas: ");
		if(!updates.equals("...")) { music.setArtists(updates.split(",")); }
		updates = MyIO.readLine("Data de lancamento(yyyy/MM/dd): ");
		if(!updates.equals("...")) { music.setReleaseDate(new Date(updates)); }
		updates = MyIO.readLine("Playlists: ");
		if(!updates.equals("...")) { music.setInSpotifyPlaylists(Integer.parseInt(updates)); }
		updates = MyIO.readLine("Ranking: ");
		if(!updates.equals("...")) { music.setRankSpotifyCharts(Short.parseShort(updates)); }
		updates = MyIO.readLine("Streams: ");
		if(!updates.equals("...")) { music.setSpotifyStreams(Long.parseLong(updates)); }
		
		boolean success = updateRecord(music, dto);
		MyIO.println("\n" + (success? "Musica Atualizada: " : "Falha ao atualizar musica: ") + music.toString());
	}
	
	private static void deleteMusicByIdMenu() {
		int id;
		MyIO.println("\n\nTP01 - AEDS III (Spotify Musics): APAGAR MUSICA");
		id = MyIO.readInt("ID da musica: ");
		boolean success = deleteRecord(id);
		
		MyIO.println((success ? "Sucesso ao apagar musica: " : "Falha ao apagar musica: ")+id+ "\n\n");
	}
	
	public static void main(String[] args) throws Exception {
		MyIO.setCharset("UTF-8");
		db = new DataBaseAccess();
		initalMenu();
		db.close();
	}
}
