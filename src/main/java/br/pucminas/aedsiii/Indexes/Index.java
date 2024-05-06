package main.java.br.pucminas.aedsiii.Indexes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Classe de estrutura para transitar os dados utilizados na
 * indexação de registros
 * 
 * @since TP02
 * @version 1
 */
public class Index {
	private int id;
	private long address;
	
	/**
	 * Instancia de um objeto indice para indexacao de registro
	 * @param id - ID do registro
	 * @param address - endereço onde o registro está salvo
	 */
	public Index(int id, long address) {
		this.id = id;
		this.address = address;
	}
	
	/**
	 * Instancia vazia de um index
	 */
	public Index() {
		this(-1,-1);
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setAddress(long address) {
		this.address = address;
	}

	public int getId() {
		return id;
	}

	public long getAddress() {
		return address;
	}
	
	/**
	 * Copia as informações de um index para si
	 * @param origin - Indice a copiar
	 */
	public void copy(Index origin) {
		this.id = origin.id;
		this.address = origin.address;
	}
	
	/**
	 * Zera as informações presentes no indice
	 */
	public void reset() {
		this.id = -1;
		this.address = -1;
	}
	
	/**
	 * Realiza um clone de um objeto indice
	 * @param origin - Indice a ser clonado
	 * @return Index - novo objeto contendo as informações do indice de entrada
	 */
	public static Index clone(Index origin) {
		return new Index(origin.id, origin.address);
	}
	
	/**
	 * Codifica as informações do indice para byte array
	 * @return byte[] - indice codificado
	 */
	public byte[] toByteArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
        	dos.writeInt(this.id);
            dos.writeLong(this.address);
        } catch(Exception e) {
        	System.err.println("Erro ao converter index para byte array.");
        }
        
        return baos.toByteArray();
    }
	
	/**
	 * Decodifica o conteudo de um array de bytes para um indice
	 * @param b - byte array a decodificar
	 * @return - Indice decodificado
	 */
	public static Index fromByteArray(byte[] b) {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        Index i = new Index();
        try {
	        i.id = dis.readInt();
	        i.address = dis.readLong();
		} catch(Exception e) {
			System.err.println("Erro ao converter byte array para index.");
	    }
        return i;
	}
}
