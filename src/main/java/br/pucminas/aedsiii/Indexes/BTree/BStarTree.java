package main.java.br.pucminas.aedsiii.Indexes.BTree;

import java.io.IOException;
import java.io.RandomAccessFile;

import main.java.br.pucminas.aedsiii.Indexes.Index;
import main.java.br.pucminas.aedsiii.Indexes.BTree.DTO.IndexDTO;

/**
 * Classe responsavel pela estrutura e operacoes 
 * no arquivo de indices. Gerencia o CRUD e relacionamento
 * entre as paginas.
 * 
 * @since TP02
 * @version 1
 */
public class BStarTree {
	protected static int ORDER = 10;
	private RandomAccessFile db;
	
	/**
	 * Inicializa o acessso ao arquivo de indexação. <br>
	 * Se o arquivo nao existir/vazio: Cria uma pagina vazia.
	 */
	public BStarTree() {
		try {
			String path = System.getProperty("user.dir");
			db = new RandomAccessFile(path+"\\src\\main\\resources\\indexes\\index.db", "rwd");
			if(db.length() == 0) {
				db.seek(0);
				db.writeLong(8);
				db.write(new Node().toByteArray());
			}
		} catch(Exception e) {
			System.err.println("Error on open indexes.");
		}
	}

	/**
	 * Insere um novo indice na arquivo de indices
	 * Busca a partir da raiz da arvore onde será inserido. <br>
	 * Caso seja necessário, trata a definição de uma nova raiz.
	 * @param i - Novo indice
	 */
	public void insertIndex(Index i) {
		long address = getRootAddress();
		Node firstPage = getPage(address);
		IndexDTO newRoot = insert(firstPage, address, i);
		if(newRoot != null) { createNewRoot(newRoot); }
	}
	
	/**
	 * Realiza a busca de qual pagina deverá ser inserida o novo indice. <br>
	 * Apos a inserção, verifica se há indices promovidos e os insere em seus
	 * respectivos "pais"
	 * @param page - Pagina atual
	 * @param address - Endereco da pagina atual
	 * @param i - Novo indice
	 * @return IndexDTO - Nova raiz, Indice promovido e endereço das paginas filhas
	 */
	private IndexDTO insert(Node page, long address, Index i) {
		IndexDTO promoted = null;
		while(page != null) {
			if(page.isLeaf) {
				promoted = insertIntoPage(page, address, i, -1);
				break;
			} else {
				address = page.getLastChild();
				page = getPage(address);
				continue;
			}
		}
		
		if(promoted != null) {
			address = page.fatherAddress;
			page = getPage(address);
			while(promoted != null && page != null) {
				promoted = insertIntoPage(page, address, promoted.index, promoted.rightAddress);
				address = page.fatherAddress;
				page = getPage(address);
			}
		}
		return promoted;
	}
	
	/**
	 * Realiza a inserção de um novo indice na pagina. Verifica se há
	 * espaço na pagina, se há possibilidade de doação entre paginas irmãs
	 * e se é necessário a divisão de paginas.
	 * @param page - Pagina destino do novo indice
	 * @param address - Endereço da pagina destino
	 * @param i - Novo indice
	 * @param rightChild - Endereço da nova pagina filho
	 * @return IndexDTO - Indice promovido e endereço das paginas filhas
	 */
	private IndexDTO insertIntoPage(Node page, long address, Index i, long rightChild) {
		if(page.size < BStarTree.ORDER-1) {
			page.addIndex(i);
			page.addChild(rightChild);
			savePage(page, address);
			return null;
		} else {
			if(page.fatherAddress != -1 && sendToSister(page, address, i, rightChild)) {
				return null;
			}
			return splitPage(page, address, i, rightChild);
		}
	}
	
	/**
	 * Realiza a divisao de uma pagina cheia, e adiciona na nova pagina
	 * o novo indice. <br>
	 * Define para os "filhos" da nova pagina o novo "pai"
	 * @param page - Pagina a ser dividida
	 * @param address - Endereço da pagina
	 * @param i - Novo indice
	 * @param rightChild - Endereço da nova pagina filha
	 * @return IndexDTO - Indice promovido e endereço das paginas filhas
	 */
	private IndexDTO splitPage(Node page, long address, Index i, long rightChild) {
		Node newPage = new Node();

		Index in = Node.splitNodes(page, newPage);
		newPage.addIndex(i);
		newPage.addChild(rightChild);
		long newAddress = getLastAddress();
		savePage(page, address);
		savePage(newPage, newAddress);
		if(!page.isLeaf) {
			defineNewFatherToChildPages(newPage, newAddress);
		}
		
		return new IndexDTO(in, address, newAddress);
	}
	
	/**
	 * Verifica/realiza a possibilidade de doação de indice para a pagina irmã. <br>
	 * Caso possivel adiciona o novo indice na pagina destino e atualiza o endereço
	 * da pagina pai na pagina filho doada a irmã.
	 * @param page - Pagina origem (doadora)
	 * @param address - Endereço da pagina
	 * @param i - Novo indice
	 * @param rightChild - Endereço da nova pagina filha
	 * @return boolean - True: Doação feita / False: Não pode doar (Pagina irmã cheia)
	 */
	private boolean sendToSister(Node page, long address, Index i, long rightChild) {
		long fatherAddress = page.fatherAddress;
		Node father = getPage(fatherAddress);
		long sisterAddress = father.getPenultimateChild();
		Node sister = getPage(sisterAddress);
		
		boolean canDonate = Node.sendToSister(father, sister, page);
		
		if(canDonate) {
			page.addIndex(i);
			page.addChild(rightChild);
			
			savePage(sister, sisterAddress);
			savePage(father, fatherAddress);
			savePage(page, address);
			
			if(!page.isLeaf) {
				long newSisterChildAddress = sister.getLastChild();
				Node newSisterChild = getPage(newSisterChildAddress);
				newSisterChild.fatherAddress = sisterAddress;
				savePage(newSisterChild, newSisterChildAddress);
			}
		}
		
		return canDonate;
	}
	
	/**
	 * Realiza a busca de um ID no arquivo de indices a partir
	 * da pagina raiz.
	 * @param id - ID procurado
	 * @return long - Endereço do ID no arquivo de dados
	 */
	public long findIndex(int id) {
		long address = getRootAddress();
		Node firstPage = getPage(address);
		return find(firstPage, id);
	}
	
	/**
	 * Realiza a busca pelo ID informado, navegando entre as paginas
	 * existentes no arquivo.
	 * @param page - Pagina raiz
	 * @param id - ID procurado
	 * @return - Endereço do ID no arquivo de dados
	 */
	private long find(Node page, int id) {
		while(page != null) {
			if(id > page.getLastIndex().getId()) {
				page = getPage(page.getLastChild());
				continue;
			}
			
			for(byte i=0; i<page.size; i++) {
				if(id == page.indexes[i].getId()) {
					return page.indexes[i].getAddress();
				}
				else if(id < page.indexes[i].getId()) {
					page = getPage(page.children[i]);
					break;
				}
			}
		}
		return -4;
	}

	/**
	 * Atualiza o endereço do ID que aponta para pagina de dados.
	 * Realiza a busca pelo ID e atualiza seu indice.
	 * @param id - ID procurado
	 * @param newAdress - Novo endereço do ID na pagina de dados.
	 * @return boolean - Endereco atualizado? 
	 */
	public boolean updateIndex(int id, long newAdress) {
		long address = getRootAddress();
		Node page = getPage(address);
		
		while(page != null) {
			if(id > page.getLastIndex().getId()) {
				address = page.getLastChild();
				page = getPage(address);
				continue;
			}
			
			for(byte i=0; i<page.size; i++) {
				if(id == page.indexes[i].getId()) {
					page.indexes[i].setAddress(newAdress);
					savePage(page, address);
					return true;
				}
				else if(id < page.indexes[i].getId()) {
					page = getPage(page.children[i]);
					break;
				}
			}
		}
		return false;
	}

	// MARK: - DELETE
	public void deleteIndex() {
		
	}
	
	// MARK - Utils
	
	/**
	 * Retorna um objeto com o conteudo de uma pagina
	 * do arquivo de indices
	 * @param address - Endereço da pagina
	 * @return Node - pagina decodificada
	 */
	private Node getPage(long address) {
		try {
			byte[] pageByteArray = new byte[Node.NODE_SIZE];
			db.seek(address);
			db.read(pageByteArray);
			return Node.fromByteArray(pageByteArray);
		} catch(Exception e) {
			return null;
		}
	}
	
	/**
	 * Salva o conteudo codificado de uma pagina no
	 * arquivo de indices
	 * @param page - Pagina a ser salva
	 * @param address - Endereço onde salvar a pagina
	 */
	private void savePage(Node page, long address) {
		try {
			db.seek(address);
			db.write(page.toByteArray());
		} catch(Exception e) {
			System.err.println("Erro ao salvar pagina!");
		}
	}
	
	/**
	 * Retorna o endereço da pagina raiz
	 * @return long - Endereço da pagina raiz
	 */
	private long getRootAddress() {
		try {
			db.seek(0);
			return db.readLong();
		} catch(Exception e) {
			System.err.println("Erro ao obter raiz!");
			return -1;
		}
	}
	
	/**
	 * Define no cabeçalho do arquivo de indices o
	 * endereço da nova pagina raiz
	 * @param rootAddress - Endereço da pagina
	 */
	private void defineNewRoot(long rootAddress) {
		try {
			db.seek(0);
			db.writeLong(rootAddress);
		} catch(Exception e) {
			System.err.println("Erro ao definir raiz!");
		}
	}
	
	/**
	 * Cria uma nova pagina que será definida como a pagina raiz da
	 * arvore do arquivo do indices.<br>
	 * Atualiza o endereco da pagina pai nas paginas filhas.
	 * @param i - Indice promovido e paginas filhas
	 */
	private void createNewRoot(IndexDTO i) {
		Node newRoot = new Node();
		newRoot.addChild(i.leftAddress);
		newRoot.addIndex(i.index);
		newRoot.addChild(i.rightAddress);
		
		long address = getLastAddress();
		savePage(newRoot, address);
		defineNewRoot(address);
		defineNewFatherToChildPages(newRoot, address);
	}

	/**
	 * Para cada pagina filha de uma pagina, atualiza o endereço da
	 * pagina pai.
	 * @param page - Pagina pai
	 * @param address - Endereco da pagina pai
	 */
	private void defineNewFatherToChildPages(Node page, long address) {
		for(int i=0; i<=page.size; i++) {
			long ad = page.children[i];
			Node child = getPage(ad);
			child.fatherAddress = address;
			savePage(child, ad);
		} 
	}
	
	/**
	 * Retorna o ultimo endereço do arquivo de indices
	 * @return long - Fim do arquivo de indice
	 */
	private long getLastAddress() {
		try {
			return db.length();
		} catch (Exception e) {
			System.err.println("Fim do arquivo");
			return -1;
		}
	}

	/**
	 * Fecha a conexao com o arquivo de indice
	 */
	public void close() {
		try {
			db.close();
		} catch (Exception e) {
			System.err.println("Erro ao fechar indices");
		}
	}
}
