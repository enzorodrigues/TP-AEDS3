package main.java.br.pucminas.aedsiii.FileUtil;

import java.io.*;

public class TextFileReader {
	private BufferedReader file;
	
	public TextFileReader(String filePath) {	
		try {
			file = new BufferedReader(new FileReader(filePath));
		}
		catch (FileNotFoundException e) {
			System.out.println("File not found");
		}
	}
	
	public void close() {
		try {
			file.close();
		}
		catch (IOException e) {
			System.out.println("Error on close text file: " + e);	
		}
	}
	
	@SuppressWarnings("finally")
	public String readLine() {
		
		String text = null;
		
		try {
			text = file.readLine();
		} catch (EOFException e) {
			text = "EOF";
		} catch (IOException e) {
			System.out.println("Error on read line: " + e);
			text = null;
		}
		finally {
			return text;
		}
	}
}
