package main.java.br.pucminas.aedsiii.Crypto;

/**
 * Classe utilitaria que contem os algoritmos de criptografia.
 * 
 * @since TP05
 * @version 1
 */
public class Crypto {
	private static int CAESAR_KEY = 13;
	private static String COLUMN_KEY="GALO";

	private Crypto() { }
	
	/**
	 * Funçao estatica que aplica a criptografia no texto. <br>
	 * Primeiro aplica a Cifra de César e depois a
	 * Cifra de Colunas.
	 * @param baseText - Texto original
	 * @return String - Texto criptografado
	 */
	public static String encrypt(String baseText) {
		return columnCipher(caesarCipher(baseText));
	}
	
	/**
	 * Funçao estatica que aplica a descriptografia no texto. <br>
	 * Primeiro descriptografa a Cifra de Colunas e depois a
	 * Cifra de César. 
	 * @param encrencryptedText - Texto criptografado
	 * @return String - Texto descriptografado
	 */
	public static String decrypt(String encrencryptedText) {
		return caesarCipherDecrypt(columnCipherDecrypt(encrencryptedText));
	}
	
	/**
	 * Funcao que aplica a Cifra de Cesar em um texto.
	 * Utilizada a chave privada definida na classe.
	 * @param baseText - Texto original
	 * @return String - Texto criptografado
	 */
	private static String caesarCipher(String baseText) {
		String cipher = "";

	    for (char symbol : baseText.toCharArray()) {
	        int symbolASCII = symbol + CAESAR_KEY;
	        cipher += (char)symbolASCII;
	    }

	    return cipher;
	}
	
	/**
	 * Funcao que aplica a descriptografia da Cifra de Cesar em um texto.
	 * Utilizada a chave privada definida na classe.
	 * @param encrencryptedText - Texto criptografado
	 * @return String - Texto descriptografado
	 */
	private static String caesarCipherDecrypt(String encrencryptedText) {
		String result = "";

	    for (char symbol : encrencryptedText.toCharArray()) {
	        int symbolASCII = symbol - (CAESAR_KEY);
	        result += (char)symbolASCII;
	    }

	    return result;
	}
	
	/**
	 * Funcao que aplica a Cifra de Colunas em um texto.
	 * Utilizada a chave privada definida na classe.
	 * @param baseText - Texto original
	 * @return String - Texto criptografado
	 */
	private static String columnCipher(String baseText) {
		int columns = COLUMN_KEY.length();
		int lines = (int) Math.ceil(baseText.length()/(columns*1.0));
		char[][] table = setupTable(lines, columns, baseText.length());
		
		for(int i=0, k=0; i<lines; i++) {
			for(int j=0; j<columns && k<baseText.length(); j++, k++) {
				table[i][j] = baseText.charAt(k);
			}
		}
		
		String result = "";
		int[] order = selectionSortOnColumnKey();
		for(int i=0; i<order.length; i++) {
			int j=order[i]; 
			for(int k=0; k<lines; k++) {
				result += table[k][j];
			}
		}
		
		return result.replace("\u001e", "");
	}
	
	/**
	 * Funcao que aplica a descriptografia da Cifra de Coluna em um texto.
	 * Utilizada a chave privada definida na classe.
	 * @param encrencryptedText - Texto criptografado
	 * @return String - Texto descriptografado
	 */
	private static String columnCipherDecrypt(String encrencryptedText) {
		int columns = COLUMN_KEY.length();
		int lines = (int) Math.ceil(encrencryptedText.length()/(columns*1.0));
		char[][] table = setupTable(lines, columns, encrencryptedText.length());
		int[] order = selectionSortOnColumnKey();
		
		int c=0;
		for(int i=0; i<order.length; i++) {
			int j=order[i]; 
			for(int k=0; k<lines && c<encrencryptedText.length(); k++) {
				if(table[k][j] == '\u001e') continue;
				table[k][j] = encrencryptedText.charAt(c++);
			}
		}
		
		String result = "";
		for(int i=0; i<lines; i++) {
			for(int j=0; j<columns; j++) {
				result += table[i][j];
			}
		}
		
		return result.replace("\u001e", "");
	}
	
	/**
	 * Inicializa a matriz para a Cifra de Coluna. <br>
	 * Identifica matematicamente os espaços que serao vazios e
	 * os deixa marcados para serem substituidos.
	 * @param lines - Quantidade de linhas da matriz
	 * @param columns - Quantidade de colunas da matriz
	 * @param textLength - Tamanho do texto base
	 * @return char[][] - Matriz inicial
	 */
	private static char[][] setupTable(int lines, int columns, int textLength) {
		char[][] table = new char[lines][columns];
		
		for(int mod = textLength % columns; mod>0 && mod < columns; mod++) {
			table[lines-1][mod] = '\u001e';
		}
		
		return table;
	}
	
	/**
	 * Aplica o algoritmo de selectionSort na chave da Cifra de Coluna
	 * para identificar qual a ordem extraçao da tabela resultante
	 * @return int[] - vetor com a ordem das colunas a serem extraidas
	 */
	private static int[] selectionSortOnColumnKey() {
		char[] keyArray = COLUMN_KEY.toCharArray();
		int[] order = new int[keyArray.length];
		for (int i = 0; i < keyArray.length; i++) {
			int smallest = i;
			for (int j = i + 1; j < keyArray.length; j++) {
				if (keyArray[j] < keyArray[smallest])
					smallest = j;
			}
			char aux = keyArray[i];
			keyArray[i] = keyArray[smallest];
			keyArray[smallest] = aux;
		}
		int k=0;
		for(char symbol : keyArray) {
			order[k++] = COLUMN_KEY.indexOf(symbol);
		}
		
		return order;
	}
}
