package main.java.br.pucminas.aedsiii.Database.DTO;

import main.java.br.pucminas.aedsiii.Entity.Music;

public class MusicDTO {
	private Music music;
	private long gravestonePointer;
	private long recordPointer;
	
	public MusicDTO(Music music, long gravestonePointer, long recordPointer) {
		super();
		this.music = music;
		this.gravestonePointer = gravestonePointer;
		this.recordPointer = recordPointer;
	}

	public Music getMusic() {
		return music;
	}

	public long getGravestonePointer() {
		return gravestonePointer;
	}

	public long getRecordPointer() {
		return recordPointer;
	}
}
