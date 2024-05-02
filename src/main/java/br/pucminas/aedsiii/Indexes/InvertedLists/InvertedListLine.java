package main.java.br.pucminas.aedsiii.Indexes.InvertedLists;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class InvertedListLine {
	public static byte REFERENCES = 50;
	private String term;
	private int[] IDs;
	private byte size;
	
	public InvertedListLine(String term) {
		this.term = term;
		this.IDs = new int[REFERENCES];
		for(byte i=0; i<REFERENCES; i++) {
			IDs[i] = -1;
		}
		this.size = 0;
	}
	
	public InvertedListLine() {
		this("");
	}
	
	public void addID(int id) {
		this.IDs[size] = id;
		size++;
	}

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
	
	public Integer[] getIDs() {
		Integer[] ids = new Integer[size];
		for(byte i=0;i<size;i++) {
			ids[i] = IDs[i];
		}
		return ids;
	}
	
	public boolean equals(String term) {
		return this.term.equalsIgnoreCase(term);
	}
	
	public boolean contains(String term) {
		return this.term.contains(term);
	}
	
	public byte[] toByteArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
        	dos.writeUTF(term);
        	dos.writeByte(size);
        	for(int i=0;i<REFERENCES; i++) {
    			dos.writeInt(IDs[i]);
    		}
        } catch(Exception e) {
        	System.err.println("Erro ao converter line para byte array.");
        }
        
        return baos.toByteArray();
    }
	
	public static InvertedListLine fromByteArray(byte[] b) {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        
        InvertedListLine line = new InvertedListLine();
        try {
        	line.term = dis.readUTF();
        	line.size = dis.readByte();
	        for(byte i=0;i<REFERENCES;i++) {
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
