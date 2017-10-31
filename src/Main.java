import indice.estrutura.IndiceLight;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main{
    private static IndiceLight indice = new IndiceLight(10000);
    public static void main(String[] args){
        String main_folder_pathname = "/home/higor/Documentos/Engenharia de Computação - CEFET MG/7º período/RI/TP 2/Documentos Wiki/wikiSample";
        processaDocumentos(main_folder_pathname);
        System.out.println(indice.getNumDocumentos());
	}

	public static void indexacao(String text,int docid){
        Map<String,Integer> freq_termos = new HashMap<String,Integer>();
        String[] words = text.split(" ");
        for(String word : words){
            if(!freq_termos.containsKey(word)){
                freq_termos.put(word,1);
            }else{
                int freq = freq_termos.get(word);
                freq++;
                freq_termos.put(word,freq);
            }
        }
        for(String word: words) {
            indice.index(word,docid,freq_termos.get(word));
        }
    }

    public static void processaDocumentos(String pathname){
        File main_folder = new File(pathname);
        File[] listOfFolders = main_folder.listFiles();
        int docid = 0;
        assert listOfFolders != null;
        for (int i = 0; i < listOfFolders.length; i++) {
            if (listOfFolders[i].isDirectory()) {
                File folder = new File("/home/higor/Documentos/Engenharia de Computação - CEFET MG/7º período/RI/TP 2/Documentos Wiki/wikiSample/" + listOfFolders[i].getName());
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
                    text = StringUtil.retiraStopWords(text);
                    text = StringUtil.replaceAcento(text);
                    indexacao(text,docid);
                    docid++;
                }
                break;
            }
        }
    }
}