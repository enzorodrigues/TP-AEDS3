package main.java.br.pucminas.aedsiii.Indexes.InvertedLists.DTO;

import main.java.br.pucminas.aedsiii.Indexes.InvertedLists.InvertedListLine;

/**
 * Classe de auxilio na transferencia de informaçoes 
 * nas operacoes CRUD de um arquivo binario de lista
 * invertida
 * 
 * @since TP02
 * @author Enzo Rodrigues Soares
 * @version 1
 */
public class LineDTO {
	public InvertedListLine line;
	public long address; 
	
	public LineDTO(InvertedListLine line, long address) {
		this.line = line;
		this.address = address;
	}
}
