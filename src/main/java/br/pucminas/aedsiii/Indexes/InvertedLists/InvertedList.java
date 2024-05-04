package main.java.br.pucminas.aedsiii.Indexes.InvertedLists;

import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashSet;

import main.java.br.pucminas.aedsiii.Indexes.InvertedLists.DTO.LineDTO;

/**
 * Cria na pasta resources/indexes/invertedLists um arquvio binario
 * para indexacao de termos. Esta classe auxilia nas operacoes CRUD entre a
 * aplicacao e o arquivo.
 * 
 * @since TP02
 * @version 1
 */
public class InvertedList {
	private RandomAccessFile db;
	private byte lineReferenceSize;
	
	/**
	 * Cria uma instancia de um arquivo de lista invertida para indexacao
	 * @param fileName - Nome do arquivo de lista invertida
	 * @param lineReferencesQuantity - Quantidade maxima de referencias de cada termo
	 */
	public InvertedList(String fileName, byte lineReferencesQuantity) {
		try {
			this.lineReferenceSize = lineReferencesQuantity;
			String path = System.getProperty("user.dir")+"\\src\\main\\resources\\indexes\\invertedLists\\";
			db = new RandomAccessFile(path+fileName, "rwd");
		} catch(Exception e) {
			System.err.println("Error on open invertedList: "+fileName);
		}
	}
	
	/** 
	 * Adiciona ao arquivo de lista invertida um conjunto de termos,
	 * atribuindo o ID em suas referencias 
	 * @param words - Conjunto de termos a serem adicionados
	 * @param id - ID a ser atribuido nas referencias de cada termo
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
	
	/**
	 * Cria no arquivo uma nova linha contendo o termo e ID recebidos
	 * @param word - Novo termo a ser inserido no arquivo
	 * @param id - Primeiro ID referenciado pelo termo
	 */
	private void createNewTerm(String word, int id) {
		InvertedListLine newLine = new InvertedListLine(word, lineReferenceSize);
		newLine.addID(id);
		saveLine(newLine, getLastAddress());
	}
	
	/**
	 * Busca no arquivo um array de IDs que atendem a todos os
	 * termos da busca
	 * @param terms - Array de termos que filtraram a busca
	 * @return Integer[] - Array de IDs que contenham todos os termos da busca
	 */
	public Integer[] searchTerm(String[] terms) {
		terms = filter(terms);
		HashSet<Integer> finalSet = new HashSet<Integer>();
		HashSet<Integer> set;
		boolean firstTerm = true;
		for(String term : terms) {
			set = findIDsByTerm(term);

			if(firstTerm) {
				finalSet.addAll(set);
				firstTerm = false;
				set.clear();
				set = null;
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
	
	/**
	 * Atualiza no arquivo as referencias de cada termo dada uma alteracao
	 * no arquivo de dados
	 * @param oldTerms - Termos nao mais presentes no ID
	 * @param newTerms - Novos termos presentes no ID
	 * @param id - ID atualizado
	 */
	public void updateTerms(String[] oldTerms, String[] newTerms, int id) {
		createTerms(newTerms, id);
		deleteIdFromTerms(oldTerms, id);
	}
	
	/**
	 * Remove o ID das referencias de um termo
	 * @param terms - Termos que nao referenciam mais o ID
	 * @param id - ID a ser removido das referencias dos termos
	 */
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
	
	/**
	 * Verifica a existencia de um termo no arquivo
	 * @param term - Termo a ser encontrado
	 * @return LineDTO - Se existir no arquivo: objeto contendo a linha e endereço onde se encontra o termo. 
	 * 					 Se nao: nulo
	 */
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
				line = InvertedListLine.fromByteArray(lineByteArray, lineReferenceSize);
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
	
	/**
	 * Busca os IDs pertencentes aos Termos que contenham o subTermo procurado
	 * @param term - Termo/subTermo a procurar
	 * @return HashSet(Integer) - Array de IDs unicos que contenham o termo de busca
	 */
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
				line = InvertedListLine.fromByteArray(lineByteArray, lineReferenceSize);
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

	/**
	 * Filtra os termos validos para realizacao de uma funcao CRUD.
	 * Remove dos termos pontuacoes, aspas(simples e dupla) e transforma para letras minusculas.
	 * Sao consideradas validas apenas termos de tamanho maiores que 2.
	 * Para evitar redundancia de informacoes, termos apos chaves/colchetes/parenteses sao ignorados.
	 * @param terms - Termos a serem filtrados
	 * @return Array de termos validos para operacoes
	 */
	private String[] filter(String[] terms) {
		HashSet<String> valids = new HashSet<String>();
		for(String term : terms) {
			String word = term.replace(".", "")
							  .replace("'", "")
							  .replace("\"", "")
							  .replace(":", "")
							  .replace(",", "")
							  .toLowerCase();
			if(word.length() > 2) {
				if(word.contains("(") || word.contains("[") || word.contains("{")) { break; }
				valids.add(word);
			}
		}
		String[] validTerms = new String[valids.size()];
		valids.toArray(validTerms);
		valids.clear();
		valids = null;
		return validTerms;
	}
	
	/**
	 * Salva o termo e suas referencias no arquivo
	 * @param line - Linha contendo termo e referencias
	 * @param address - Endereco onde o termo esta salvo/sera salvo
	 */
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
	
	/**
	 * Retorna o fim do arquivo(ultimo endereco)
	 * @return long - endereco final do arquivo
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
	 * Fecha o arquivo de lista invertida
	 */
	public void close() {
		try {
			db.close();
		} catch(Exception e) {
			System.err.println("Error on closing InvertedList.");
		}
	}
}
