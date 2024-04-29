package main.java.br.pucminas.aedsiii.Indexes.BTree;

import java.io.IOException;
import java.io.RandomAccessFile;

import main.java.br.pucminas.aedsiii.Indexes.Index;
import main.java.br.pucminas.aedsiii.Indexes.BTree.DTO.IndexDTO;

public class BTree {
	protected static int ORDER = 10;
	private RandomAccessFile db;
	
	public BTree() {
		try {
			String path = System.getProperty("user.dir");
			db = new RandomAccessFile(path+"\\src\\main\\resources\\index.db", "rwd");
			initTree(db.length() != 0);
		} catch(Exception e) {
			System.out.println("Error on open indexes.");
		}
	}
	
	private void initTree(boolean BTree_Created) throws IOException {
		if(!BTree_Created) {
			db.seek(0);
			db.writeLong(8);
			db.write(new Node().toByteArray());
			db.getChannel().force(false);
			db.getFD().sync();
		}
	}

	// MARK: - INSERT
	public void insert(Index i) {
		long address = getRootAddress();
		Node firstPage = getPage(address);
		IndexDTO newRoot = insert(firstPage, address, i);
		if(newRoot != null) { createNewRoot(newRoot); }
	}
	
	private IndexDTO insert(Node page, long address, Index i) {
		if(i.getId() == 94) {
			System.out.println("95");
		}
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
	
	private IndexDTO insertIntoPage(Node page, long address, Index i, long rightChild) {
		if(page.size < BTree.ORDER-1) {
			page.addIndex(i);
			page.addChild(rightChild);
			savePage(page, address);
			return null;
		} else {
			return splitPage(page, address, i, rightChild);
		}
	}
	
	private IndexDTO splitPage(Node page, long address, Index i, long rightChild) {
		Node newPage = new Node();

		Index in = Node.splitNodes(page, newPage);
		newPage.addIndex(i);
		newPage.addChild(rightChild);
		long newAddress = getLastAddress();
		savePage(page, address);
		savePage(newPage, newAddress);
		
		return new IndexDTO(in, address, newAddress);
	}
	
	// MARK: - FIND
	
	public long find(int id) {
		long address = getRootAddress();
		Node firstPage = getPage(address);
		return find(firstPage, id);
	}
	
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

	// MARK - Utils
	
	// Pages - Get/Save
	private Node getPage(long address) {
		try {
			byte[] pageByteArray = new byte[Node.NODE_SIZE];
			db.seek(address);
			db.read(pageByteArray);
			return Node.fromByteArray(pageByteArray);
		} catch(Exception e) {
			//System.out.println("Erro ao obter pagina! Address: "+address+"\n\n");
			return null;
		}
	}
	
	private void savePage(Node page, long address) {
		try {
			db.seek(address);
			db.write(page.toByteArray());
			db.getChannel().force(false);
			db.getFD().sync();
		} catch(Exception e) {
			System.out.println("Erro ao salvar pagina!");
		}
	}
	
	// Root
	private long getRootAddress() {
		try {
			db.seek(0);
			return db.readLong();
		} catch(Exception e) {
			System.out.println("Erro ao obter raiz!");
			return -1;
		}
	}
	
	private void defineNewRoot(long rootAddress) {
		try {
			db.seek(0);
			db.writeLong(rootAddress);
			db.getChannel().force(false);
			db.getFD().sync();
		} catch(Exception e) {
			System.out.println("Erro ao definir raiz!");
		}
	}
	
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

	private void defineNewFatherToChildPages(Node page, long address) {
		for(int i=0; i<=page.size; i++) {
			long ad = page.children[i];
			Node child = getPage(ad);
			child.fatherAddress = address;
			savePage(child, ad);
		} 
	}
	
	// End of File
	private long getLastAddress() {
		try {
			return db.length();
		} catch (Exception e) {
			System.out.println("Fim do arquivo");
			return -1;
		}
	}

	// Close
	public void close() {
		try {
			db.close();
		} catch (Exception e) {
			System.out.println("Erro ao fechar indices");
		}
	}
}
