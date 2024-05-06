package main.java.br.pucminas.aedsiii.FileUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Classe utilitaria para leitura de um arquivo texto
 * 
 * @since TP01
 */
public class TextFileReader {
	private BufferedReader file;
	
	/**
	 * Instancia a conexao com o arquivo para leitura
	 * @param filePath - Caminho absoluto do arquivo
	 */
	public TextFileReader(String filePath) {	
		try {
			file = new BufferedReader(new FileReader(filePath));
		}
		catch (FileNotFoundException e) {
			System.err.println("File not found");
		}
	}
	
	/**
	 * Fecha a conexao com o arquivo
	 */
	public void close() {
		try {
			file.close();
		}
		catch (IOException e) {
			System.err.println("Error on close text file: " + e);	
		}
	}
	
	/**
	 * Realiza a leitura de uma linha do arquivo
	 * @return String - Conteudo obtido da linha
	 */
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
