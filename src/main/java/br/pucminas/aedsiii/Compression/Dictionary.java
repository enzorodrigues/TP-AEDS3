package main.java.br.pucminas.aedsiii.Compression;

import java.util.HashMap;

/**
 * Classe que define o formato do dicionario gerado/utilizado.
 * 
 * @since TP03
 * @version 3
 */
public class Dictionary extends HashMap<String, Integer> {

	private static final long serialVersionUID = 7122442633188928431L;

	public Dictionary clone() {
		Dictionary dict = new Dictionary();
		for(String key : this.keySet()) {
			dict.put(key, this.get(key));
		}
		return dict;
	}

}
