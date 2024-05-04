package main.java.br.pucminas.aedsiii.Indexes.InvertedLists.DTO;

import main.java.br.pucminas.aedsiii.Indexes.InvertedLists.InvertedListLine;

/**
 * Classe de auxilio na transferencia de informaçoes 
 * nas operacoes CRUD de um arquivo binario de lista
 * invertida
 * 
 * @since TP02
 * @version 1
 */
public class LineDTO {
	public InvertedListLine line;
	public long address; 
	
	/**
	 * Objeto que transita informações uteis para manipulação de
	 * dados dos termos no arquivo de lista invertida
	 * @param line - Termo e suas referencias
	 * @param address - Endereço onde o termo está salvo
	 */
	public LineDTO(InvertedListLine line, long address) {
		this.line = line;
		this.address = address;
	}
}
