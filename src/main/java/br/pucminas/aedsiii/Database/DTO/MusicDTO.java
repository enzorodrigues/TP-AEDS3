package main.java.br.pucminas.aedsiii.Database.DTO;

import main.java.br.pucminas.aedsiii.Entity.Music;

/**
 * Classe de auxilio na transferencia de informaçoes 
 * nas operacoes CRUD no arquivo binario de dados
 * 
 * @since TP01
 * @version 1
 */
public class MusicDTO {
	private Music music;
	private long gravestonePointer;
	private long recordPointer;
	
	/**
	 * Objeto transita informações uteis para manipulação de registros
	 * na base de dados.
	 * @param music - Registro recuperado da base de dados
	 * @param gravestonePointer - Endereco onde está a lápide do registro
	 * @param recordPointer - Endereço onde o registro está salvo
	 */
	public MusicDTO(Music music, long gravestonePointer, long recordPointer) {
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
