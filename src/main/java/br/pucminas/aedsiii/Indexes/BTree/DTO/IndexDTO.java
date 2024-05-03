package main.java.br.pucminas.aedsiii.Indexes.BTree.DTO;

import main.java.br.pucminas.aedsiii.Indexes.Index;

/**
 * Classe de auxilio na transferencia de informaçoes 
 * nas operacoes CRUD no arquivo de indexacao 
 * estruturado em uma arvore B*
 * 
 * @since TP02
 * @author Enzo Rodrigues Soares
 * @version 1
 */
public class IndexDTO {
	public Index index;
	public long leftAddress;
	public long rightAddress;

	/**
	 * Objeto que transita informações uteis para manipulacao de
	 * indices no arquivo de indices. 
	 * @param i - Indice a ser transferido
	 * @param left - Endereço  da pagina a esquerda
	 * @param right - Endereço da pagina a direita
	 */
	public IndexDTO(Index i, long left, long right) {
		this.index = i;
		this.leftAddress = left;
		this.rightAddress = right;
	}
}
