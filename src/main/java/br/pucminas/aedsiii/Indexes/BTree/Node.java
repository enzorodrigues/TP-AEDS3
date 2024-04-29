package main.java.br.pucminas.aedsiii.Indexes.BTree;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import main.java.br.pucminas.aedsiii.Indexes.Index;

public class Node {
	protected static short NODE_SIZE = 197;
	protected long fatherAddress;
	protected byte size;
	public boolean isLeaf;
	Index[] indexes;
	long[] children;
	
	public Node() {
		this.fatherAddress = -1;
		this.size = 0;
		this.isLeaf = false;
		indexes = new Index[BTree.ORDER-1];
		children = new long[BTree.ORDER];
		
		for(int i = 0; i<indexes.length; i++) {
			indexes[i] = new Index();
			children[i] = -1;
		}
		children[BTree.ORDER-1] = -1;
	}
	

	public byte[] toByteArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        int max = BTree.ORDER-1;
        try {
        	dos.writeLong(fatherAddress);
        	dos.writeByte(size);
        	for(int i=0; i<max;i++) {
        		dos.writeLong(children[i]);
        		dos.write(indexes[i].toByteArray());
        	}
        	dos.writeLong(children[max]);
        } catch(Exception e) {
        	System.out.println("Erro ao converter node para byte array.");
        }
        
        return baos.toByteArray();
    }
	
	
	public static Node fromByteArray(byte[] b) {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        Node node = new Node();
        int max = BTree.ORDER-1;
        try {
        	node.fatherAddress = dis.readLong();
        	node.size = dis.readByte();
	        for(int i=0;i<max;i++) {
	        	node.children[i] = dis.readLong();
	        	node.indexes[i].fromByteArray(dis.readNBytes(12));
	        }
	        node.children[max] = dis.readLong();
	        
	        node.isLeaf = node.children[0] == -1;
	        return node;
		} catch(Exception e) {
	    	System.out.println("Erro ao converter byte array para index.");
	    	return null;
	    }
	}
	
	public void addIndex(Index i) {
		this.indexes[size] = i;
		size++;
	}
	
	public void addChild(long rightChild) {
		this.children[size] = rightChild;
	}
	
	public long getLastChild() {
		return children[size];
	}
	
	public Index getLastIndex() {
		return indexes[size-1];
	}
	
	public static Index splitNodes(Node origin, Node destiny) {
		destiny.fatherAddress = origin.fatherAddress;
		int half = (BTree.ORDER-1)/2;
		
		for(int i = half+1, j=0; i<BTree.ORDER-1; i++, j++) {
			destiny.children[j] = origin.children[i];
			origin.children[i] = -1;
			
			destiny.indexes[j].copy(origin.indexes[i]);
			origin.indexes[i].reset();
			
			destiny.size++;
			origin.size--;
		}
		destiny.children[half] = origin.children[BTree.ORDER-1];
		origin.children[BTree.ORDER-1] = -1;
		
		Index promoted = Index.clone(origin.indexes[half]);
		origin.indexes[half].reset();
		origin.size--;
		
		return promoted;
	}
}
