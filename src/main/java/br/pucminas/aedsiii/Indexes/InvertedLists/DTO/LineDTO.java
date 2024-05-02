package main.java.br.pucminas.aedsiii.Indexes.InvertedLists.DTO;

import main.java.br.pucminas.aedsiii.Indexes.InvertedLists.InvertedListLine;

public class LineDTO {
	public InvertedListLine line;
	public long address; 
	
	public LineDTO(InvertedListLine line, long address) {
		this.line = line;
		this.address = address;
	}
}
