package main.java.br.pucminas.aedsiii.Indexes.InvertedLists;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import main.java.br.pucminas.aedsiii.Indexes.InvertedLists.DTO.LineDTO;

public class InvertedList {
	private RandomAccessFile db;
	
	public InvertedList(String fileName) {
		try {
			String path = System.getProperty("user.dir")+"\\src\\main\\resources\\indexes\\invertedLists\\";
			db = new RandomAccessFile(path+fileName, "rwd");
		} catch(Exception e) {
			System.err.println("Error on open invertedList: "+fileName);
		}
	}

	// MARK: - CREATE
	
	/** 
	 * Adiciona ao arquivo de lista invertida o conjunto de palavras,
	 * atribuindo o ID em suas referencias 
	 * @param words - Conjunto de palavras previamente separados
	 * @param id - Id da musica a ser atribuido nas referencias de cada termo
	 */
	public void createTerms(String[] words, int id) {
		words = filter(words);
		LineDTO dto;
		for(String word : words) {
			dto = termExists(word);
			if(dto != null) {
				dto.line.addID(id);
				saveLine(dto.line, dto.address);
			} else {
				createNewTerm(word, id);
			}
		}
	}
	
	private void createNewTerm(String word, int id) {
		InvertedListLine newLine = new InvertedListLine(word);
		newLine.addID(id);
		saveLine(newLine, getLastAddress());
	}

	// MARK: - FIND
	
	public Integer[] searchTerm(String[] terms) {
		terms = filter(terms);
		HashSet<Integer> finalSet = new HashSet<Integer>();
		HashSet<Integer> set;
		for(String term : terms) {
			set = findIDsByTerm(term);
			
			if(finalSet.size() == 0) {
				finalSet.addAll(set);
				continue;
			}
			
			finalSet.retainAll(set);
			set.clear();
			set = null;
		}
		
		Integer[] finalArray = new Integer[finalSet.size()];
		finalSet.toArray(finalArray);
		finalSet.clear();
		finalSet = null;
		
		return finalArray;
	}
	
	// MARK - UPDATE
	
	public void updateTerms(String[] oldTerms, String[] newTerms, int id) {
		createTerms(newTerms, id);
		deleteIdFromTerms(oldTerms, id);
	}
	
	// MARK: - DELETE
	
	public void deleteIdFromTerms(String[] terms, int id) {
		terms = filter(terms);
		LineDTO dto;
		for(String term : terms) {
			dto = termExists(term);
			if(dto != null) {
				dto.line.removeID(id);
				saveLine(dto.line, dto.address);
			}
		}
	}
	
	// MARK: - PRIVATE METHODS - UTILS
	
	private LineDTO termExists(String term) {
		InvertedListLine line;
		short size;
		byte[] lineByteArray;
		long address;
		try {
			db.seek(0);
			while(db.getFilePointer() < db.length()) {
				address = db.getFilePointer();
				size = db.readShort();
				lineByteArray = new byte[size];
				db.read(lineByteArray);
				line = InvertedListLine.fromByteArray(lineByteArray);
				if(line.equals(term)) {
					return new LineDTO(line, address);
				}
			}
			return null;
		} catch (Exception e) {
			System.err.println("Erro on verify existing term: " + term);
			return null;
		}
	}
	
	private HashSet<Integer> findIDsByTerm(String term) {
		HashSet<Integer> set = new HashSet<Integer>();
		InvertedListLine line;
		short size;
		byte[] lineByteArray;
		try {
			db.seek(0);
			while(db.getFilePointer() < db.length()) {
				size = db.readShort();
				lineByteArray = new byte[size];
				db.read(lineByteArray);
				line = InvertedListLine.fromByteArray(lineByteArray);
				if(line.contains(term)) {
					set.addAll(Arrays.asList(line.getIDs()));
				}
			}

			return (HashSet<Integer>) set.clone();
			
		} catch (Exception e) {
			System.err.println("Erro on searching term: " + term);
			return new HashSet<Integer>();
		}
	}

	private String[] filter(String[] terms) {
		ArrayList<String> valids = new ArrayList<String>();
		for(String term : terms) {
			String word = term.replace(".", "").replace("'", "").replace("\"", "").replace(":", "").toLowerCase();
			if(word.length() > 2) {
				if(word.contains("(") || word.contains("[")) { break; }
				valids.add(word);
			}
		}
		String[] validTerms = new String[valids.size()];
		valids.toArray(validTerms);
		valids.clear();
		valids = null;
		return validTerms;
	}
	
	private void saveLine(InvertedListLine line, long address) {
		try {
			byte[] content = line.toByteArray();
			db.seek(address);
			db.writeShort(content.length);
			db.write(content);
		} catch(Exception e) {
			System.err.println("Erro ao salvar linha!");
		}
	}
	
	private long getLastAddress() {
		try {
			return db.length();
		} catch (Exception e) {
			System.err.println("Fim do arquivo");
			return -1;
		}
	}

	public void close() {
		try {
			db.close();
		} catch(Exception e) {
			System.err.println("Error on closing InvertedList.");
		}
	}
}
