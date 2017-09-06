package indice.estrutura;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;




public class IndiceSimples extends Indice
{
	
	
	/**
	 * Versao - para gravação do arquivo binário
	 */
	private static final long serialVersionUID = 1L;
	
	private Map<String,List<Ocorrencia>> mapIndice = new HashMap<String,List<Ocorrencia>>();

	
	public IndiceSimples()
	{

	}
	


	@Override
	public void index(String termo,int docId,int freqTermo) 
	{
		// verificar se o termo existe no mapIndice
		List<Ocorrencia> listaOcorr;
		if (mapIndice.get(termo) == null) {
			listaOcorr = new ArrayList<>();
			Ocorrencia ocorr = new Ocorrencia(docId, freqTermo);
			listaOcorr.add(ocorr);
			mapIndice.put(termo, listaOcorr);
		}else{			
			listaOcorr = mapIndice.get(termo);
			Ocorrencia ocorr = new Ocorrencia(docId, freqTermo);
			listaOcorr.add(ocorr);
		}
	}

	
	

	@Override
	public Map<String,Integer> getNumDocPerTerm()
	{
		Map<String,Integer> mapNumDoc = new HashMap<String,Integer>();
		for(Map.Entry<String, List<Ocorrencia>> e:mapIndice.entrySet()){
			mapNumDoc.put(e.getKey(), e.getValue().size());
		}
		return mapNumDoc;
	}
	
	@Override
	public int getNumDocumentos()
	{
		Set<Integer> docs = new HashSet<>();
		for(Map.Entry<String, List<Ocorrencia>> e:mapIndice.entrySet()){
			for(Ocorrencia o : e.getValue()){
				docs.add(o.getDocId());
			}
		}
		return docs.size();
	}
	
	@Override
	public Set<String> getListTermos()
	{
		return mapIndice.keySet();
	}	
	
	@Override
	public List<Ocorrencia> getListOccur(String termo)
	{
		return mapIndice.get(termo);
	}	
	



}
