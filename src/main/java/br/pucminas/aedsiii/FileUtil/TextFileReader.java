package main.java.br.pucminas.aedsiii.FileUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TextFileReader {
	private BufferedReader file;
	
	public TextFileReader(String filePath) {	
		try {
			file = new BufferedReader(new FileReader(filePath));
		}
		catch (FileNotFoundException e) {
			System.err.println("File not found");
		}
	}
	
	public void close() {
		try {
			file.close();
		}
		catch (IOException e) {
			System.err.println("Error on close text file: " + e);	
		}
	}
	
	@SuppressWarnings("finally")
	public String readLine() {
		
		String text = null;
		
		try {
			text = file.readLine();
		} catch (Exception e) {
			System.err.println("Error on read line: " + e);
		}
		finally {
			return text;
		}
	}
}
