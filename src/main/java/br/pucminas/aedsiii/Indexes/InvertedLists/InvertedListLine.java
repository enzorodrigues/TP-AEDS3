package main.java.br.pucminas.aedsiii.Indexes.InvertedLists;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Classe que traduz o conteudo de cada linha em um arquivo de lista invertida.
 * Auxilia nas operacoes CRUD de cada termo presente na indexacao.
 * 
 * @since TP02
 * @version 1
 */
public class InvertedListLine {
	private String file;
	private String term;
	private int[] IDs;
	private byte size;
	
	/**
	 * Cria uma instancia de uma linha contendo termo e suas referencias
	 * @param term - Termo a ser indexado
	 * @param references - Quantidade limite de ids referenciados pelo termo
	 */
	public InvertedListLine(String term, byte references, String file) {
		this.file = file;
		this.term = term;
		this.IDs = new int[references];
		for(byte i=0; i<references; i++) {
			IDs[i] = -1;
		}
		this.size = 0;
	}
	
	/**
	 * Cria uma instancia de uma linha de termo vazio
	 * @param references - Quantidade limite de ids referenciados pelo termo
	 */
	public InvertedListLine(byte references, String file) {
		this("", references, file);
	}
	
	/**
	 * Adiciona o id as referencias do termo
	 * @param id - ID referenciado pelo termo
	 */
	public void addID(int id) {
		try {
			this.IDs[size] = id;
			size++;
		} catch (Exception e) {
			System.err.println("Erro on add id "+ id+ " on term '"+term+"' - from file: "+file);
		}
		
	}

	/**
	 * Removeo ID das referencias do termo
	 * @param id - ID a ser removido das referencias
	 */
	public void removeID(int id) {
		int index;
		for(int i=0; i<size; i++){
			if(IDs[i] == id) {
				IDs[i] = IDs[size-1];
				IDs[size-1] = -1;
				size--;
				break;
			}
		}
	}
	/**
	 * Obtem todos os IDs referenciados pelo termo.
	 * @return Integer[] - Array de IDs referenciados pelo termo.
	 */
	public Integer[] getIDs() {
		Integer[] ids = new Integer[size];
		for(byte i=0;i<size;i++) {
			ids[i] = IDs[i];
		}
		return ids;
	}
	
	/**
	 * Compara a equidade entre os termos sem case sensitive
	 * @param term - Termo a comparar
	 * @return boolean - true: iguais / false: diferentes
	 */
	public boolean equals(String term) {
		return this.term.equalsIgnoreCase(term);
	}
	
	/**
	 * Verifica se existe um subTermo dentro do termo
	 * @param term - subTermo a validar
	 * @return boolean - true: existe / false: nao existe
	 */
	public boolean contains(String term) {
		return this.term.contains(term);
	}
	
	/**
	 * Codifica o conteudo da linha para um array de byte
	 * @return byte[] - Array de bytes com o conteudo da linha a codificar
	 */
	public byte[] toByteArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
        	dos.writeUTF(term);
        	dos.writeByte(size);
        	for(int i=0;i<IDs.length; i++) {
    			dos.writeInt(IDs[i]);
    		}
        } catch(Exception e) {
        	System.err.println("Erro ao converter line para byte array.");
        }
        
        return baos.toByteArray();
    }
	
	/**
	 * Decodifica o conteudo de um array de bytes para uma linha
	 * @param b - Array de bytes a decodificar
	 * @param references - Quantidade maxima de referencias da linha
	 * @return InvertedListLine - Linha decodificada (termo e suas referencias)
	 */
	public static InvertedListLine fromByteArray(byte[] b, byte references, String file) {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        
        InvertedListLine line = new InvertedListLine(references, file);
        try {
        	line.term = dis.readUTF();
        	line.size = dis.readByte();
	        for(byte i=0;i<line.IDs.length;i++) {
	        	line.IDs[i] = dis.readInt();
	        }
	        return line;
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println("Erro ao converter byte array para Line.\n");
	    	return null;
	    }
	}
}
