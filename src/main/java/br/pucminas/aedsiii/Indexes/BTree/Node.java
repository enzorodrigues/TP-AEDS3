package main.java.br.pucminas.aedsiii.Indexes.BTree;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import main.java.br.pucminas.aedsiii.Indexes.Index;

/**
 * Classe responsavel pela estrutura e operacoes 
 * de uma pagina do arquivo de indices.
 * 
 * @since TP02
 * @author Enzo Rodrigues Soares
 * @version 1
 */
public class Node {
	protected static short NODE_SIZE = 197;
	protected long fatherAddress;
	protected byte size;
	public boolean isLeaf;
	Index[] indexes;
	long[] children;
	
	/**
	 * Instancia de uma pagina vazia
	 */
	public Node() {
		this.fatherAddress = -1;
		this.size = 0;
		this.isLeaf = false;
		indexes = new Index[BStarTree.ORDER-1];
		children = new long[BStarTree.ORDER];
		
		for(int i = 0; i<indexes.length; i++) {
			indexes[i] = new Index();
			children[i] = -1;
		}
		children[BStarTree.ORDER-1] = -1;
	}
	
	/**
	 * Adiciona as informações de um novo indice para 
	 * ultima posição do array de indices da pagina
	 * @param i - Indice a ser adicionado na pagina
	 */
	public void addIndex(Index i) {
		this.indexes[size].copy(i);
		size++;
	}
	
	/**
	 * Adiciona um novo ponteiro para uma pagina
	 * no final do array de filhos
	 * @param rightChild - Endereço da pagina filha
	 */
	public void addChild(long rightChild) {
		this.children[size] = rightChild;
	}
	
	/**
	 * Retorna o endereco do primeiro filho da pagina
	 * @return long - endereço da pagina
	 */
	private long getFirstChild() {
		return children[0];
	}
	
	/**
	 * Retorna o endereco do ultimo filho da pagina
	 * @return long - endereço da pagina
	 */
	public long getLastChild() {
		return children[size];
	}
	
	/**
	 * Retorna o endereco do penultimo filho da pagina
	 * @return long - endereço da pagina
	 */
	public long getPenultimateChild() {
		return children[size-1];
	}
	
	/**
	 * Retorna o ultimo do indice da pagina
	 * @return Index - id e endereço
	 */
	public Index getLastIndex() {
		return indexes[size-1];
	}
	
	/**
	 * Alterar o valor do ultimo indice
	 * @param i - Novo indice
	 */
	private void setLastIndex(Index i) {
		 indexes[size-1].copy(i);
	}
	
	/**
	 * Reposiciona todo o conteudo dos arrays de indices e
	 * filhos uma posicao para tras. Reinicia as posicoes 
	 * nao utilizadas
	 */
	private void moveBackward() {
		for(byte i=0; i<size-1; i++) {
			indexes[i].copy(indexes[i+1]);
			children[i] = children[i+1];
		}
		children[size-1] = children[size];

		indexes[size-1].reset();
		children[size] = -1;
		size--;
	}

	/**
	 * Codifica em byte array o conteudo de uma pagina
	 * @return byte[] - Pagina codificada
	 */
	public byte[] toByteArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        int max = BStarTree.ORDER-1;
        try {
        	dos.writeLong(fatherAddress);
        	dos.writeByte(size);
        	for(int i=0; i<max;i++) {
        		dos.writeLong(children[i]);
        		dos.write(indexes[i].toByteArray());
        	}
        	dos.writeLong(children[max]);
        } catch(Exception e) {
        	System.err.println("Erro ao converter node para byte array.");
        }
        
        return baos.toByteArray();
    }

	/**
	 * Decodifica de um byte array o conteudo de uma pagina
	 * @param b - byte array a decodificar
	 * @return Node - pagina decodificada
	 */
	public static Node fromByteArray(byte[] b) {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        Node node = new Node();
        int max = BStarTree.ORDER-1;
        try {
        	node.fatherAddress = dis.readLong();
        	node.size = dis.readByte();
	        for(int i=0;i<max;i++) {
	        	node.children[i] = dis.readLong();
	        	node.indexes[i].copy(Index.fromByteArray(dis.readNBytes(12)));
	        }
	        node.children[max] = dis.readLong();
	        
	        node.isLeaf = node.children[0] == -1;
	        return node;
		} catch(Exception e) {
			System.err.println("Erro ao converter byte array para Node.");
	    	return null;
	    }
	}

	/**
	 * Realiza a divisao (em dois) de uma pagina cheia. <br>
	 * Origem: Mantem os menores IDs; <br>
	 * Destino: Recebe os maiores IDs da origem;
	 * @param origin - Pagina cheia a ser dividida
	 * @param destiny - Pagina a receber metade do conteudo da origem
	 * @return Index - indice promovido
	 */
	public static Index splitNodes(Node origin, Node destiny) {
		destiny.fatherAddress = origin.fatherAddress;
		int half = (BStarTree.ORDER-1)/2;
		
		for(int i = half+1, j=0; i<BStarTree.ORDER-1; i++, j++) {
			destiny.children[j] = origin.children[i];
			origin.children[i] = -1;
			
			destiny.indexes[j].copy(origin.indexes[i]);
			origin.indexes[i].reset();
			
			destiny.size++;
			origin.size--;
		}
		destiny.children[half] = origin.children[BStarTree.ORDER-1];
		origin.children[BStarTree.ORDER-1] = -1;
		
		Index promoted = Index.clone(origin.indexes[half]);
		origin.indexes[half].reset();
		origin.size--;
		
		return promoted;
	}
	
	/**
	 * Valida/realiza a possibilidade de doar indices e filhos para uma pagina irma,
	 * evitando a necessidade criar uma nova pagina.
	 * @param father - Pagina que aponta para paginas envolvidas na doação
	 * @param sister - Pagina irma a receber a doação de indices
	 * @param page - Pagina cheia, doadora de indices
	 * @return boolean - True: Conteudo doado / False: Pagina irmã cheia
	 */
	public static boolean sendToSister(Node father, Node sister, Node page) {
		if(sister.size == BStarTree.ORDER-1) {
			return false;
		}
		
		sister.addIndex(father.getLastIndex());
		sister.addChild(page.getFirstChild());
		father.setLastIndex(page.indexes[0]);
		page.moveBackward();
		
		return true;
	}
}
