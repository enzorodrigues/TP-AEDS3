package main.java.br.pucminas.aedsiii.Entidade;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Music {
	private short id;
	private String name;
	private ArrayList<String> artists = new ArrayList<String>();
	private byte artistsCount;
	private Date releaseDate;
	private int inSpotifyPlaylists;
	private short rankSpotifyCharts;
	private int spotifyStreams;
	
	public Music(short id, String name,  ArrayList<String> artists, byte artistsCount, Date releaseDate, 
				int inSpotifyPlaylists, short rankSpotifyCharts, int spotifyStreams) {
		this.id = id;
		this.name = name;
		this.artists = artists;
		this.artistsCount = artistsCount;
		this.releaseDate = releaseDate;
		this.inSpotifyPlaylists = inSpotifyPlaylists;
		this.rankSpotifyCharts = rankSpotifyCharts;
		this.spotifyStreams = spotifyStreams;
	}

	@Override
	public String toString() {
		return "[ID: " + id + "| Nome: " + name + "| Artistas: " + artists.toString() + ", Quantidade: "
				+ artistsCount + "| Data de Lancamento: " + releaseDate + "| Spotify Playlists: " + inSpotifyPlaylists
				+ "| Spotify Ranking: " + rankSpotifyCharts + "| Spotify Streams: " + spotifyStreams + "]";
	}
	
	public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeShort(this.id);
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
        dos.writeInt(this.spotifyStreams);
        return baos.toByteArray();
    }
	
	
	public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readShort();
        dis.readShort();
        this.name = dis.readUTF();
        this.artistsCount = dis.readByte();
        for(int i=0; i < artistsCount; i++) {
        	dis.readShort();
        	artists.add(dis.readUTF());
        }
        this.releaseDate = new Date(dis.readLong());
        this.inSpotifyPlaylists = dis.readInt();
        this.rankSpotifyCharts = dis.readShort();
        this.spotifyStreams = dis.readInt();
	}
    
	
}
