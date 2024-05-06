package main.java.br.pucminas.aedsiii.Entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Entidade referente aos dados manipulados na aplicação.
 * Define a estrutura dos dos registros que serão salvos no
 * arquivo de dado.
 * 
 * @since TP01
 * @version 2
 */
public class Music {
	private int id = -1;
	private String name;
	private String[] artists;
	private byte artistsCount;
	private Date releaseDate;
	private int inSpotifyPlaylists;
	private short rankSpotifyCharts;
	private long spotifyStreams;
	
	/**
	 * Intancia vazia de um objeto musica
	 */
	private Music() {}
	
	/**
	 * Intancia de um objeto musica com todos os dados definidos
	 * @param name - Nome da musica
	 * @param artists - Artistas participantes
	 * @param artistsCount - Quantidade de artistas
	 * @param releaseDate - Data de lançamento
	 * @param inSpotifyPlaylists - Quantidade de playlists adicionadas
	 * @param rankSpotifyCharts - Posição no Ranking Global do Spotify 
	 * @param spotifyStreams - Quantidade de streams
	 */
	public Music(String name,  String[] artists, byte artistsCount, Date releaseDate, 
				int inSpotifyPlaylists, short rankSpotifyCharts, long spotifyStreams) {
		this.name = name;
		this.artists = artists;
		this.artistsCount = artistsCount;
		this.releaseDate = releaseDate;
		this.inSpotifyPlaylists = inSpotifyPlaylists;
		this.rankSpotifyCharts = rankSpotifyCharts;
		this.spotifyStreams = spotifyStreams;
	}
	
	/**
	 * Intancia de um objeto musica com todos os dados definidos
	 * @param name - Nome da musica
	 * @param artists - Artistas participantes
	 * @param releaseDate - Data de lançamento
	 * @param inSpotifyPlaylists - Quantidade de playlists adicionadas
	 * @param rankSpotifyCharts - Posição no Ranking Global do Spotify 
	 * @param spotifyStreams - Quantidade de streams
	 */
	public Music(String name,  String artists, String releaseDate, int inSpotifyPlaylists, 
				short rankSpotifyCharts, long spotifyStreams) {
		
		this(name, null, (byte)0, null, inSpotifyPlaylists, rankSpotifyCharts, spotifyStreams);
		String[] arts = artists.split(",");
		Date date = new Date(releaseDate);
		
		this.artists = arts;
		this.artistsCount = (byte) arts.length;
		this.releaseDate = date;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public int getID() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String[] getArtists() {
		return artists;
	}

	public void setInSpotifyPlaylists(int inSpotifyPlaylists) {
		this.inSpotifyPlaylists = inSpotifyPlaylists;
	}

	public void setRankSpotifyCharts(short rankSpotifyCharts) {
		this.rankSpotifyCharts = rankSpotifyCharts;
	}

	public void setSpotifyStreams(long spotifyStreams) {
		this.spotifyStreams = spotifyStreams;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setArtists(String[] artists) {
		this.artists = artists;
		this.artistsCount = (byte)artists.length;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	/**
	 * Retorna uma string contendo todas as informações da musica
	 */
	@Override
	public String toString() {
		return "[ID: " + id + " | Nome: " + name + " | Artistas: " + artistsToString() + ", Quantidade: "
				+ artistsCount + " | Data de Lancamento: " + dateToString() + " | Spotify Playlists: " + inSpotifyPlaylists
				+ " | Spotify Ranking: " + rankSpotifyCharts + " | Spotify Streams: " + spotifyStreams + " ]";
	}
	
	/**
	 * Retorna um objeto clone, contem todos os dados da musica original
	 */
	public Music clone() {
		Music music = new Music(this.name, this.artists.clone(), this.artistsCount, new Date(releaseDate.getTime()), 
								this.inSpotifyPlaylists, this.rankSpotifyCharts, this.spotifyStreams);
		music.setID(this.id);
		return music;
	}
	
	/**
	 * Gera uma string contendo todos os artistas de forma organizada para exibição.
	 * @return String - Artistas participantes da musica 
	 */
	private String artistsToString() {
		String artists = "[";
		for(String artist : this.artists) {
			artists+= ", "+artist;
		}
		artists+= "]";
		return artists.replaceFirst(", ", "").trim();
	}
	
	/**
	 * Artistas participantes sem colchetes
	 * @return String - Artistas participantes sem colchetes
	 */
	public String artistsConcat() {
		return artistsToString().replace("[", "").replace("]", "");
	}

	/**
	 * Converte a data de lançamento da musica para string em um formato padrão
	 * @return String - data de lancamento
	 */
	private String dateToString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
		String date = simpleDateFormat.format(releaseDate);
		return date;
	}
	
	/**
	 * Codifica as informações da musica para byteArray
	 * @return byte[] - Conteudo da musica codificado
	 */
	public byte[] toByteArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        try {
        	dos.writeInt(this.id);
            dos.writeShort(this.name.length());
            dos.writeUTF(this.name);
            dos.writeByte(this.artistsCount);
            for(String artist : this.artists) {
            	dos.writeShort(artist.length());
                dos.writeUTF(artist);
            }
            dos.writeLong(this.releaseDate.getTime());
            dos.writeInt(this.inSpotifyPlaylists);
            dos.writeShort(this.rankSpotifyCharts);
            dos.writeLong(this.spotifyStreams);
        } catch (Exception e) {
			System.err.println("Erro ao codificar a musica para byteArray: "+id);
		}
        
        return baos.toByteArray();
    }

	/**
	 * Decodifica o conteudo de um array de bytes para uma musica
	 * @param b - byte array a decodificar
	 * @return - Musica decodificada
	 */
	public static Music fromByteArray(byte[] b) {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        Music music = new Music();
        try {
        	music.id = dis.readInt();
            dis.readShort();
            music.name = dis.readUTF();
            music.artistsCount = dis.readByte();
            music.artists = new String[music.artistsCount];
            for(int i=0; i < music.artistsCount; i++) {
            	dis.readShort();
            	music.artists[i] = (dis.readUTF());
            }
            music.releaseDate = new Date(dis.readLong());
            music.inSpotifyPlaylists = dis.readInt();
            music.rankSpotifyCharts = dis.readShort();
            music.spotifyStreams = dis.readLong();
        } catch (Exception e) {
			System.err.println("Erro ao decodificar musica");
		}
        return music;
	}
}
