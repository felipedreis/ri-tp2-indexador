import indice.estrutura.Indice;
import indice.estrutura.IndiceLight;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.tartarus.snowball.ext.portugueseStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main{
    private static IndiceLight indice = new IndiceLight(15000000);

    public static void main(String[] args) throws IOException {

        String main_folder_pathname = "../wikiSample";

        long startTime = System.currentTimeMillis();
        processaDocumentos(main_folder_pathname);
        indice.concluiIndexacao();
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.print("Tempo para indexacao (s): ");
        System.out.println(elapsedTime);
        System.out.print("Numero de documentos indexados: ");
        System.out.println(indice.getNumDocumentos());
        System.out.println("Total de termos indexados: "+indice.getListTermos().size());

        // Grava indice em arquivo
        File file = new File("../indice.dat");
        indice.gravarIndice(file);
        /*  Carrega índice de arquivo
        file = new File("../indice.dat");
        IndiceLight novo = (IndiceLight) Indice.leIndice(file);
        */
    }

	public static String stemming(String word){
        String wordStemmed;
        // stemmers em ingles e portugues
        portugueseStemmer stemmerPt = new portugueseStemmer();
        englishStemmer stemmerEn = new englishStemmer();
        stemmerPt.setCurrent(word);
        stemmerEn.setCurrent(word);
        if(stemmerEn.stem()){
            wordStemmed = stemmerEn.getCurrent();
        }else if(stemmerPt.stem()){
            wordStemmed = stemmerPt.getCurrent();
        }else{
            wordStemmed = word;
        }
        return wordStemmed;
    }

	public static void indexacao(Set<String> nonStopwords,int docid){
        // mapa que conta a frequencia de cada termo em um documento
        Map<String,Integer> freq_termos = new HashMap<String,Integer>();
        String wordStemmed;
        for(String word : nonStopwords){
            // verifica se existe o stem de uma palavra tanto em ingles quanto em portugues
            wordStemmed = stemming(word);
            // calcula a frequencia do termo no documento
            if(!freq_termos.containsKey(wordStemmed)){
                freq_termos.put(wordStemmed,1);
            }else{
                int freq = freq_termos.get(wordStemmed);
                freq++;
                freq_termos.put(wordStemmed,freq);
            }
        }
        // indexa os termos apos o stemming e calculo da frequencia
        for(String word: nonStopwords) {
            wordStemmed = stemming(word);
            indice.index(wordStemmed,docid,freq_termos.get(wordStemmed));
        }
    }

    public static void processaDocumentos(String pathname){
        File main_folder = new File(pathname);
        File[] listOfFolders = main_folder.listFiles();
        int docid = 0;
        assert listOfFolders != null;
        for (int i = 0; i < listOfFolders.length; i++) {
            if (listOfFolders[i].isDirectory()) {
                File folder = new File("../wikiSample/" + listOfFolders[i].getName());
                System.out.println("Indexando pasta: "+listOfFolders[i].getName());
                File[] listOfFiles = folder.listFiles();
                assert listOfFiles != null;
                for (int j = 0; j < listOfFiles.length; j++) {
                    String filename = listOfFiles[j].toString();
                    File file = new File(filename);
                    StringBuilder fileContent = new StringBuilder();
                    try {
                        BufferedReader buffer = new BufferedReader(new FileReader(file.toString()));
                        while (buffer.ready()) {
                            fileContent.append(buffer.readLine());
                        }
                        buffer.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                    Document doc = Jsoup.parse(String.valueOf(fileContent));
                    String text = doc.body().text();
                    text = StringUtil.replaceAcento(text);
                    Set<String> nonStopwords;
                    nonStopwords = StringUtil.retiraStopWords(text);
                    indexacao(nonStopwords,docid);
                    docid++;
                }
            }
            break;
        }
    }
}