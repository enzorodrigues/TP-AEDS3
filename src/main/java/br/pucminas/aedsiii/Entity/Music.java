package main.java.br.pucminas.aedsiii.Entity;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class Music {
	private int id = -1;
	private String name;
	private String[] artists;
	private byte artistsCount;
	private Date releaseDate;
	private int inSpotifyPlaylists;
	private short rankSpotifyCharts;
	private long spotifyStreams;
	
	public Music() {
	}
	
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
	
	public void setID(int id) {
		this.id = id;
	}
	
	public int getID() {
		return id;
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

	@Override
	public String toString() {
		return "[ID: " + id + " | Nome: " + name + " | Artistas: " + artistsToString() + ", Quantidade: "
				+ artistsCount + " | Data de Lancamento: " + dateToString() + " | Spotify Playlists: " + inSpotifyPlaylists
				+ " | Spotify Ranking: " + rankSpotifyCharts + " | Spotify Streams: " + spotifyStreams + " ]";
	}
	
	private String artistsToString() {
		String artists = "[";
		for(String artist : this.artists) {
			artists+= ", "+artist;
		}
		artists+= "]";
		return artists.replaceFirst(", ", "");
	}

	private String dateToString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
		String date = simpleDateFormat.format(releaseDate);
		return date;
	}
	
	public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
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
        return baos.toByteArray();
    }
	
	
	public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        dis.readShort();
        this.name = dis.readUTF();
        this.artistsCount = dis.readByte();
        this.artists = new String[artistsCount];
        for(int i=0; i < artistsCount; i++) {
        	dis.readShort();
        	artists[i] = (dis.readUTF());
        }
        this.releaseDate = new Date(dis.readLong());
        this.inSpotifyPlaylists = dis.readInt();
        this.rankSpotifyCharts = dis.readShort();
        this.spotifyStreams = dis.readLong();
	}
    
	
}
