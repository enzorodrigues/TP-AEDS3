package main.java.br.pucminas.aedsiii.PatternMatching;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;

import main.java.br.pucminas.aedsiii.MyIO;

/**
 * Classe utilitaria que contem os algoritmos de casamento de padrão.
 * 
 * @since TP04
 * @version 1
 */
public class PatternMatching {
	private static int rabinKarpCompares = 0;

	public PatternMatching() { }
	
	// Brute Force
	
	/**
	 * Método do casamento de padrão por força-bruta.
	 * Analisa todo o texto em busca do padrão, movendo o padrão através do texto
	 * sem otimização, comparando caracter a caracter, da esquerda para direita.
	 * @param text - Texto base
	 * @param pattern - Padrão a ser encontrado no texto
	 * @return MatchingResult - resultado da busca
	 */
	public MatchingResult bruteForce(String text, String pattern) {
		int comparisons = 0, j = 0;
		int textSize = text.length();
		int patternSize = pattern.length();
		
		Instant start = Instant.now();
		for(int i=0; (i < textSize) && (j < patternSize); i++, j++, comparisons++) {
			if(text.charAt(i) != pattern.charAt(j)) {
				i = i- j;
				j = -1;
			}
		}
		Instant end = Instant.now();
		Duration timeElapsed = Duration.between(start, end);
		String methodComparisons = "Força-Bruta - Comparações: "+comparisons;
		boolean result = j == patternSize;
		
		return new MatchingResult(methodComparisons, timeElapsed, result);
	}
	
	
	// KMP
	
	/**
	 * Método do casamento de padrão de Knuth–Morris–Pratt.
	 * Analisa o texto em busca do padrão, otimiza a busca realizando um pré-processamento
	 * no padrão analisando prefixos. O pré-processamento pode ser sua versão melhorada ou não. <br>
	 * As comparações sao feitas da esquerda para direita.
	 * @param text - Texto base
	 * @param pattern - Padrão a ser encontrado no texto
	 * @param improvedPrefix - versão do pré-processamento
	 * @return MatchingResult - resultado da busca
	 */
	public MatchingResult KMP(String text, String pattern, boolean improvedPrefix) {
		int comparisons = 0, j=0;
		int textSize = text.length();
		int patternSize = pattern.length();
		Instant start = Instant.now();
		int[] PI = improvedPrefix ? improvedPrefixFunction(pattern) : prefixFunction(pattern);
		
		for(int i=0; (i < textSize) && (j < patternSize); i++, j++) {
			while(j >= 0 && text.charAt(i) != pattern.charAt(j)) {
				comparisons++;
				j = PI[j];
			}
			if(j >= 0 && text.charAt(i) == pattern.charAt(j)) {
				comparisons++;
            }
		}
		
		Instant end = Instant.now();
		Duration timeElapsed = Duration.between(start, end);
		String methodComparisons = (improvedPrefix ? "KMP Melhorado" : "KMP")+" - Comparações: "+comparisons;
		boolean result = j == patternSize;
		
		return new MatchingResult(methodComparisons, timeElapsed, result);
	}
	
	/**
	 * Realiza o pre-processamento do padrão analizando prefixos no mesmo.
	 * Versão inicial.
	 * @param pattern - Padrão a ser analisado
	 * @return int[] - array de analise dos prefixos
	 */
	private int[] prefixFunction(String pattern) {
		int patternSize = pattern.length();
		int[] PI = new int[patternSize+1];
		PI[0] = -1;
		
		for(int i=0, j=-1; i < patternSize; i++, j++, PI[i]=j) {
			while(j >=0 && pattern.charAt(i) != pattern.charAt(j)) {
				j = PI[j];
			}
		}
		
		return PI;
	}
	
	/**
	 * Realiza o pre-processamento do padrão analisando prefixos no mesmo.
	 * Versão melhorada.
	 * @param pattern - Padrão a ser analisado
	 * @return int[] - array de analise dos prefixos
	 */
	private int[] improvedPrefixFunction(String pattern) {
		int patternSize = pattern.length(), k=0;
		int[] _PI = new int[patternSize+1];
		_PI[0] = -1;
		
		for(int i=0, j=-1; i < patternSize; i++, j++,
			_PI[i] = (pattern.charAt(k) == pattern.charAt(j)) ? _PI[j] : j) {
			while(j >=0 && pattern.charAt(i) != pattern.charAt(j)) {
				j = _PI[j];
			}
			
			k = i+1 == patternSize ? k : k+1;
		}
		
		return _PI;
	}
	
	// Boyer-Moore
	
	/**
	 * Método do casamento de padrão de Boyer-Moore.
	 * Analisa o texto em busca do padrão, otimiza a busca realizando um pré-processamento
	 * no padrão analisando sufixos. As comparações sao feitas da direta para esquerda.
	 * @param text - Texto base
	 * @param pattern - Padrão a ser encontrado no texto
	 * @return MatchingResult - resultado da busca
	 */
	public MatchingResult boyerMoore(String text, String pattern) {
		int comparisons = 0, shift=0, j=0;
		int textSize = text.length();
		int patternSize = pattern.length();
		Instant start = Instant.now();
		HashMap<Character, Integer> offsetByBadCharacter = offsetByBadCharacter(pattern.substring(0, patternSize-1));
		int[] offsetByGoodSuffix = offsetByGoodSuffix(pattern);
		
		while (shift <= (textSize - patternSize)) {
            j = patternSize - 1;

            while (j >= 0 && pattern.charAt(j) == text.charAt(shift + j)) {
            	comparisons++;
                j--;
            }
            if(j >= 0 && pattern.charAt(j) != text.charAt(shift + j)) {
            	comparisons++;
            }

            if (j < 0) {
                break; 
            } else {
                int badCharacterShift = j - (offsetByBadCharacter.containsKey(text.charAt(shift + j)) ? offsetByBadCharacter.get(text.charAt(shift + j)) : -1);
                int goodSuffixShift = offsetByGoodSuffix[j];
                shift += Math.max(1, Math.max(badCharacterShift, goodSuffixShift));
            }
        }
		
		Instant end = Instant.now();
		Duration timeElapsed = Duration.between(start, end);
		String methodComparisons = "Boyer-Moore - Comparações: "+comparisons;
		boolean result = j < 0;
		
		return new MatchingResult(methodComparisons, timeElapsed, result);
	}
	
	/**
	 * Gera a tabela de caracteres da Heuristica do Deslocamento
	 * do Caracter Ruim. Contem as ultimas aparições de cada caracter no padrão,
	 * execeto o ultimo.
	 * @param cleanPattern - Padrão sem o ultimo caracter
	 * @return HashMap<Character, Integer> - Tabela com as ultimas aparições de cada caracter
	 */
	private HashMap<Character, Integer> offsetByBadCharacter(String cleanPattern) {
		Character[] uniques = uniqueCharacters(cleanPattern);
		HashMap<Character, Integer> offsetByBadCharacter = new HashMap<Character, Integer>();
		
		for(char ch : uniques) {
			offsetByBadCharacter.put(ch, cleanPattern.lastIndexOf(ch));
		}
		
		return offsetByBadCharacter;
	}
	
	/**
	 * Identifica cada caracter unico dentro do padrão
	 * @param cleanPattern - Padrão sem o ultimo caracter
	 * @return Character[] - Array de caracteres unicos
	 */
	private Character[] uniqueCharacters(String cleanPattern) {
		HashSet<Character> uniques = new HashSet<Character>();
		for(char ch : cleanPattern.toCharArray()) {
			uniques.add(ch);
		}
		
		Character[] chars = new Character[uniques.size()];
		uniques.toArray(chars);
		return chars;
	}
	
	/**
	 * Gera o array da Heuristica do Deslocamento por Sufixo Bom.
	 * Analisa sufixos e suas aparições, dentro do padrão, com prefixos diferentes.
	 * @param pattern - Padrão a analisar
	 * @return int[] - Array de deslocamentos para sufixos
	 */
	private int[] offsetByGoodSuffix(String pattern) {
		int patternSize = pattern.length();
		int[] offsetByGoodSuffix = new int[patternSize];
		
		offsetByGoodSuffix[offsetByGoodSuffix.length-1] = 1;
		
		for(int i = offsetByGoodSuffix.length-2; i >= 0; i--) {
			String sufix = pattern.substring(i+1);
			int haveBefore = hasSuffixWithDifferentPrefix(pattern, sufix, pattern.charAt(i));
			
			if(haveBefore != -1) { // Case 1
				offsetByGoodSuffix[i] = i - haveBefore;
			} else if(i != 0  && pattern.startsWith(sufix)) { // Case 2
				offsetByGoodSuffix[i] = i+1;
			} else if(sufix.length() != 1) { // Case 2.1
				int prefixFirstCase = reducedSuffixIsThePrefix(pattern, sufix.substring(1));
				if(prefixFirstCase != -1) { offsetByGoodSuffix[i] = prefixFirstCase; }
			} else { // Case 3
				offsetByGoodSuffix[i] = patternSize;
			}
		}
		
		return offsetByGoodSuffix;
	}
	
	/**
	 * Analisa se o sufixo existe antes no padrao com um prefixo diferente.
	 * @param pattern - Padrão analisado
	 * @param sufix - Sufixo a procurar
	 * @param prefix - Prefixo a diferenciar
	 * @return int - Se existir: posicao onde começa. Se não: -1
	 */
	private int hasSuffixWithDifferentPrefix(String pattern, String sufix, char prefix) {
		for(int i = 0; i < pattern.length() - sufix.length(); i++) {
			int j=i, k=0;
			
			while(k < sufix.length() && pattern.charAt(j) == sufix.charAt(k)) {
				j++;
				k++;
			}
			
			try {
				if(k == sufix.length() && pattern.charAt(i-1) != prefix) { return i-1; }
			} catch (Exception e) { 
				return -1; 
			}
		}
		return -1;
	}
	
	/**
	 * Analise se enquanto existir sufixo ele é o prefixo do padrão.
	 * @param pattern - Padrão analisado
	 * @param reducedSuffix - Sufixo reduzido
	 * @return int - Se existir: Deslocamento necessário. Se não: -1
	 */
	private int reducedSuffixIsThePrefix(String pattern, String reducedSuffix) {
		do {
			if(pattern.startsWith(reducedSuffix)) {
				return pattern.length() - reducedSuffix.length();
			} 
			reducedSuffix = reducedSuffix.substring(1);
		} while(!reducedSuffix.isEmpty());
		
		return -1;
	}
	
	public MatchingResult rabinKarp(String text, String pattern) {
		int textSize = text.length();
		int patternSize = pattern.length();
		boolean result = false;
		rabinKarpCompares = 0;
		
		Instant start = Instant.now();
		
		int patternHash = hash(pattern);
		for(int i=0; i < textSize-patternSize; i++) {
			String sub = text.substring(i, i+patternSize);
			if(hash(sub) == patternHash && compare(sub, pattern)) {
				result = true;
				break;
			}
		}
		
		Instant end = Instant.now();
		Duration timeElapsed = Duration.between(start, end);
		String methodComparisons = "Rabin-Karp - Comparações: "+rabinKarpCompares;
		
		return new MatchingResult(methodComparisons, timeElapsed, result);
	}
	
	private int hash(String text) {
		int mod = 937, result=0;
		for(char i : text.toCharArray()) {
			result+=i;
		}
		return result % mod;
	}
	
	private boolean compare(String text, String pattern) {
		for(int i=0; i<text.length(); i++) {
			rabinKarpCompares++;
			if(text.charAt(i) != pattern.charAt(i)) {
				return false;
			}
		}
		return true;
	}
}
