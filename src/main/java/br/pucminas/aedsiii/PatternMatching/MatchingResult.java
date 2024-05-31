package main.java.br.pucminas.aedsiii.PatternMatching;

import java.time.Duration;

/**
 * Classe para representar informações de uma busca de
 * Casamento de padrão. Contem o numero de comparações, resultado
 * e tempo de execução.
 * 
 * @since TP04
 * @version 1
 */
public class MatchingResult {
	
	private String methodComparisons;
	private long timeElapsedInMiliSeconds;
	private boolean result;

	public MatchingResult(String methodComparisons, Duration timeElapsed, boolean result) {
		this.methodComparisons = methodComparisons;
		this.timeElapsedInMiliSeconds = timeElapsed.toMillis();
		this.result = result;
	}
	
	@Override
	public String toString() {
		return methodComparisons+ " | Padrão "+(result ? "" : "NÃO ")+"ENCONTRADO | Tempo total: "+timeElapsedInMiliSeconds+" ms";
	}
}
