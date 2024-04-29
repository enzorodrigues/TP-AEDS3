package main.java.br.pucminas.aedsiii.Indexes.BTree.DTO;

import main.java.br.pucminas.aedsiii.Indexes.Index;

public class IndexDTO {
	public Index index;
	public long leftAddress;
	public long rightAddress;

	public IndexDTO(Index i, long left, long right) {
		this.index = i;
		this.leftAddress = left;
		this.rightAddress = right;
	}
}
