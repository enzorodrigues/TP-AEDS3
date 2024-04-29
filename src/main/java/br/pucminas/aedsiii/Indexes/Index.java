package main.java.br.pucminas.aedsiii.Indexes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Index {
	private int id;
	private long address;
	
	public Index(int id, long address) {
		this.id = id;
		this.address = address;
	}
	
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
	
	public void copy(Index origin) {
		this.id = origin.id;
		this.address = origin.address;
	}
	
	public void reset() {
		this.id = -1;
		this.address = -1;
	}
	
	public static Index clone(Index origin) {
		return new Index(origin.id, origin.address);
	}
	
	public byte[] toByteArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
        	dos.writeInt(this.id);
            dos.writeLong(this.address);
        } catch(Exception e) {
        	System.out.println("Erro ao converter index para byte array.");
        }
        
        return baos.toByteArray();
    }
	
	
	public void fromByteArray(byte[] b) {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        try {
	        this.id = dis.readInt();
	        this.address = dis.readLong();
		} catch(Exception e) {
	    	System.out.println("Erro ao converter byte array para index.");
	    }
	}
}
