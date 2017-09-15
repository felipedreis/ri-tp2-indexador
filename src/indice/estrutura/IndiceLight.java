package indice.estrutura;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;




public class IndiceLight extends Indice  
{
	
	/**
	 * Versao - para gravação do arquivo binário
	 */
	private static final long serialVersionUID = 1L;


	private Map<String,PosicaoVetor> posicaoIndice;
	private Set<Integer> documentos;
	
	private String[] posicaoIndiceReverso;
	
	private int[] arrDocId;
	private int[] arrTermId;
	private int[] arrFreqTermo;
	
	
	/**
	 * Ultimo indice (com algum valor valido) nos vetores
	 */
	private int lastIdx = -1;
	/**
	 * Armazena o ultimo id de termo criado. Utilizado para criar um 
	 * id incremental dos termos.
	 */
	private int lastTermId = 0;
	private double newSize = 0.1;
	
	
	
	public IndiceLight(int initCap)
	{
		arrDocId = new int[initCap];
		arrTermId = new int[initCap];
		arrFreqTermo = new int[initCap];
		posicaoIndiceReverso = new String[initCap];
		posicaoIndice = new HashMap<String,PosicaoVetor>();
		documentos = new HashSet<>();
	}
	
	public static int[] aumentaCapacidadeVetor(int[] vetor, double d) {
		int novo[] = Arrays.copyOf(vetor, (int)Math.round(vetor.length * (1.0 + d)));
		return novo;
	}
	
	@Override
	public int getNumDocumentos()
	{
		return documentos.size();
	}

	/**
	 * Indexa um terminado termo que ocorreu freqTermo vezes em um determinado documento docId.
	 * Armazene o novo termo na última posição do vetor (usando o atributo lastIdx). 
	 * Utilize o posicaoIndice para resgatar o id do termo. 
	 * Caso este id não exista, crie-o utilizando a variável lastTermId. 
	 * Caso o vetor já esteja no seu limite, você deve criar um vetor 10% maior e realocar todos os elementos.
	 * 
	 * **Sobre o Map posicaoIndice***
	 * Você irá usar o Map posicaoIndice agora apenas para definir/resgatar o id deste termo passado como parametro.
	 * Não se preocupe em definir a posicao inicial no vetor (posInicial) nem o número de documentos 
	 * que este termo ocorreu (numOcorrencias). Estes dois atributos (posInicial e numOcorrencias) só serão 
	 * setados ao concluir a indexação (i.e. no método concluiIndexacao), pois, ao concluir, 
	 * o vetor será devidamente ordenado.
	 */
	@Override
	public void index(String termo,int docId,int freqTermo)
	{
		int idx;
		
		if (posicaoIndice.containsKey(termo)) {
			idx = posicaoIndice.get(termo).getIdTermo();
		} else {
			idx = ++lastTermId;
			posicaoIndice.put(termo, new PosicaoVetor(idx));
			
			if (idx >= posicaoIndiceReverso.length)
				posicaoIndiceReverso = Arrays.copyOf(posicaoIndiceReverso, (int)(posicaoIndiceReverso.length * 1.1));
			posicaoIndiceReverso[idx] = termo;
		}
		
		lastIdx++;
		if(lastIdx == arrTermId.length) {
			arrDocId = aumentaCapacidadeVetor(arrDocId, newSize);
			arrTermId = aumentaCapacidadeVetor(arrTermId, newSize);
			arrFreqTermo = aumentaCapacidadeVetor(arrFreqTermo, newSize);
			System.gc();
		}
		
		arrTermId[lastIdx] = idx;
		arrDocId[lastIdx] = docId;
		arrFreqTermo[lastIdx] = freqTermo;
		documentos.add(docId);
	}

	
	@Override
	public Map<String,Integer> getNumDocPerTerm()
	{
		Map<String,Integer> map = new HashMap<>();
		
		for(Map.Entry<String, PosicaoVetor> e:posicaoIndice.entrySet()){
			map.put(e.getKey(), e.getValue().getNumDocumentos());
		}
		
		return map;
	}
	
	@Override
	public Set<String> getListTermos()
	{
		return posicaoIndice.keySet();
	}
	
	@Override
	public List<Ocorrencia> getListOccur(String termo)
	{
		PosicaoVetor pos = posicaoIndice.get(termo);
		List<Ocorrencia> ocorrencias = new ArrayList<>();
		int offset = pos.getPosInicial();
		for(int i = 0; i < pos.getNumDocumentos(); ++i) {
			ocorrencias.add(new Ocorrencia(arrDocId[offset + i], arrFreqTermo[offset + i]));
		}
		
		return ocorrencias;
	}
	
	/**
	 * Ao concluir a indexação, deve-se ordenar o indice de acordo com o id do termo.
	 * Logo após, atualize a posicaoInicial e numOcorrencia de cada
	 * termo no Map posicaoIndice. 
	 * 
	 * Dica: ao percorrer os vetores, para saber qual instancia PosicaoVetor um id de termo se refere, 
	 * crie um vetor que relaciona o id do termo (como indice) e a instancia PosicaoVetor que esta no mapa posicaoIndice. 
	 * Percorra o mapa posicaoIndice para obter essa relação. 
	 * Ou seja, cosidere que o arrTermoPorId é o vetor criado. Este vetor 
	 * possuirá o tamanho lastTermId+1 (pois o id do termo é incremental) você povoará o este vetor da seguinte forma:
	 * para cada termo pertencente em posicaoIndice: arrTermoPorId[posicaoIndice.get(termo).getIdTermo()] = posicaoIndice.get(termo);
	 * 
	 */
	@Override
	public void concluiIndexacao(){
		ordenaIndice();
		String termo;
		PosicaoVetor pos;
		int idx = arrTermId[0];
		int count = 1;
		int firstOccurrence = 0;
		for(int i = 1; i < lastIdx; ++i) {
			if (arrTermId[i] == idx) {
				count++;
				continue;
			} else {
				termo = posicaoIndiceReverso[idx];
				pos = posicaoIndice.get(termo);
				pos.setNumDocumentos(count);
				pos.setPosInicial(firstOccurrence);
				
				firstOccurrence = i;
				count = 1;
				idx = arrTermId[i];
			}
		}
		
		termo = posicaoIndiceReverso[idx];
		pos = posicaoIndice.get(termo);
		pos.setNumDocumentos(count);
		pos.setPosInicial(firstOccurrence);
	}

	public void ordenaIndice()
	{
		quickSort(0, lastIdx);
		//insertionSort();
	}

	/**
	 * Algoritmo qucksort baseado em Cormen et. al, Introduction to Algorithms 
	 * e adaptado para utilizar a partição com o pivot aleatório
	 * @param p
	 * @param r
	 */
	private void quickSort(int p, int r){
		if(p<r){
			//System.out.println("p: "+p+" r: "+r);
			int q = partition(p, r);
			quickSort(p,q-1);
			quickSort(q+1, r);
		}
	}
	private int partition(int p,int r){
		//partição com pivot aleatório
		int pivot = (int)(p+Math.random()*(r-p));
		exchange(r,pivot);
		
		int i = p-1;
		for(int j = p; j<=r-1; j++){
			if(compare(j,r)<=0){
				i = i+1;
				exchange(i,j);
			}
		}
		exchange(i+1,r);
		return i+1;
	}
	
	/**
	 * Usando os vetores do indice, 
	 * Retorna >0 se posI>posJ
	 * 		   <0 se posI<posJ
	 * 			0, caso contrário
	 * @param posI
	 * @param posJ
	 * @return
	 */
	public int compare(int posI, int posJ){
		//ordena primeirmente pelo termId
		if(this.arrTermId[posI]!=this.arrTermId[posJ]){
			return this.arrTermId[posI]-this.arrTermId[posJ];
		}else{
			return this.arrDocId[posI]-this.arrDocId[posJ];
		}
	}
	/**
	 * Troca a posição dos vetores
	 * @param posI
	 * @param posJ
	 */
	public void exchange(int posI,int posJ){
		int docAux = this.arrDocId[posI];
		int freqAux = this.arrFreqTermo[posI];
		int termAux = this.arrTermId[posI];
		
		this.arrDocId[posI] = this.arrDocId[posJ];
		this.arrFreqTermo[posI] = this.arrFreqTermo[posJ];
		this.arrTermId[posI] = this.arrTermId[posJ];
		
		this.arrDocId[posJ] = docAux;
		this.arrFreqTermo[posJ] = freqAux;
		this.arrTermId[posJ] = termAux;
		
	}
	
	
	public void setArrs(int[] arrDocId,int[] arrTermId,int[] arrFreqTermo){
		this.arrDocId = Arrays.copyOf(arrDocId, arrDocId.length);
		this.arrTermId = Arrays.copyOf(arrTermId, arrTermId.length);
		this.arrFreqTermo = Arrays.copyOf(arrFreqTermo, arrFreqTermo.length);
		lastIdx = arrFreqTermo.length-1;
		
	}
	public int[] getArrDocId(){
		return this.arrDocId;
	}
	public int[] getArrTermId(){
		return this.arrTermId;
	}
	public int[] getArrFreq(){
		return this.arrFreqTermo;
	}


}
