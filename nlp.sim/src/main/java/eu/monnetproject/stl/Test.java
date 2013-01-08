package eu.monnetproject.stl;

public class Test {}
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.net.URI;
//import java.util.Collection;
//import java.util.List;
//
//import eu.monnetproject.corpus.NoSuchCorpusObjectException;
//import eu.monnetproject.corpus.UnimplementedQueryType;
//import eu.monnetproject.corpus.annotation.Annotator;
//import eu.monnetproject.corpus.search.query.CorpusObjectType;
//import eu.monnetproject.corpus.search.query.Query;
//import eu.monnetproject.corpus.search.result.Result;
//import eu.monnetproject.corpus.search.query.annotation.DocumentAnnotationQuery;
//import eu.monnetproject.corpus.search.query.impl.SentenceTextQuery;
//import eu.monnetproject.corpus.search.query.impl.TokenValueQuery;
//import eu.monnetproject.corpus.simple.SimpleCorpus;
//import eu.monnetproject.doc.Document;
//import eu.monnetproject.doc.txt.TextFileExtractor;
//import eu.monnetproject.lang.Language;
//import eu.monnetproject.lang.UnsupportedLanguageException;
//import eu.monnetproject.opennlp.OpenNLPSentenceAnnotator;
//import eu.monnetproject.sentence.Sentence;
//import eu.monnetproject.tagger.POSToken;
//import eu.monnetproject.tagger.stanford.StanfordTaggerAnnotator;
//import eu.monnetproject.tokens.Token;
//import eu.monnetproject.tokenizer.latin.LatinTokenAnnotator;
//
//public class Test {
//
//	private static List<File> getFiles(String dir, String filter) {
//		List<File> files = null;
//		try {
//			files = FileReader.getFileListing(new File(dir),filter);
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return files;
//	}
//
//	private static List<File> get_OBIE_DATA_SET() {
//		List<File> files = getFiles("data/obie_corpus/en","txt");
//		//files.addAll(getFiles("data/obie_corpus/en/gamesa","txt"));
//		return files;
//	}
//
//	//	private static List<File> get_SIMPLE_CORPUS_DATA_SET() {
//	//		List<File> files = getFiles("data/corpus/en","txt");
//	//		return files;
//	//	}
//
//	public static void main(String[] args) throws UnimplementedQueryType, UnsupportedLanguageException, NoSuchCorpusObjectException {
//
//		// prepare data set
//		Language lang = Language.ENGLISH;
//		List<File> files = get_OBIE_DATA_SET();
//
//		// pipeline annotators
//		SimpleCorpus corpus = new SimpleCorpus(lang);
//		TextFileExtractor textFileExtractor = new TextFileExtractor();
//		Annotator sentenceAnnotator = new OpenNLPSentenceAnnotator(lang);
//		//Annotator sentenceAnnotator = new LingpipeSentenceAnnotator(lang);
//		Annotator tokenAnnotator = new LatinTokenAnnotator();
//		StanfordTaggerAnnotator taggerAnnotator = new StanfordTaggerAnnotator();
//
//		// EXECUTE PIPELINE ON BLACKBOARD
//		// add text documents
//		long t0 = System.currentTimeMillis();
//		for(File textFile:files) {
//			Document doc = textFileExtractor.getDocument(textFile);
//			corpus.addDocument(doc);
//		}
//		System.out.println("extractor "+(System.currentTimeMillis()-t0));
//		// add sentence annotations
//		long t1 = System.currentTimeMillis();
//		sentenceAnnotator.annotate(corpus);
//		System.out.println("splitter "+(System.currentTimeMillis()-t1));
//		// add token annotation
//		t1 = System.currentTimeMillis();
//		tokenAnnotator.annotate(corpus);
//		System.out.println("token "+(System.currentTimeMillis()-t1));
//		// add postoken annotation
//		t1 = System.currentTimeMillis();
//		taggerAnnotator.annotate(corpus);
//		System.out.println("tagger "+(System.currentTimeMillis()-t1));
//		System.out.println("all "+(System.currentTimeMillis()-t0));
//		System.out.println("- - - - - - - - - - ");
//
//		// CHECK CORPUS ANNOTATIONS
////		System.out.println("documents: "+corpus.getDocuments().size());
////		System.out.println("sentences: "+corpus.getSentences().size());
////		System.out.println("   tokens: "+corpus.getTokens().size());
////		System.out.println("postokens: "+corpus.getPOSTokens().size());
////		System.out.println("  postags: "+corpus.getPOSTags().size());
////		System.out.println("  tokvals: "+corpus.getTokVals().size());
//
//		// doc-sen
////		for(Document doc:corpus.getDocuments()) {
////			System.out.println("doc-sen: sen="+doc.getSentences().size());
////		}
//		// sen-doc
////		int cnt=0;
////		for(Sentence sen:corpus.getSentences()) {
////			System.out.println("sen: "+sen);
////			//			Document doc = corpus.get(sen);
////			//			System.out.println("sen-doc: "+shortenString(doc.getText(), 20));
////			cnt++;if (cnt==5) break;
////		}
//
//		// sen-tok
////		cnt=0;
////		for(Sentence sen:corpus.getSentences()) {
////			System.out.println("sen-token: token="+sen.getTokens().size());
////			cnt++;if (cnt==5) break;
////		}
//		// tok-sen
////		cnt=0;
////		for(Token token:corpus.getTokens()) {
////			System.out.println("tok: "+token);
////			//			System.out.println("token-sen: "+shortenString(corpus.get(token).getText(),20));
////			//			cnt++;if (cnt==5) break;
////		}
//
//		// sen-tok
////		cnt=0;
////		for(Sentence sen:corpus.getSentences()) {
////			System.out.println("sen-token: token="+sen.getTokens().size());
////			cnt++;if (cnt==5) break;
////		}
//		// tok-sen
////		cnt=0;
////		for(Token token:corpus.getTokens()) {
////			System.out.println("tok: "+token);
////			//			System.out.println("token-sen: "+shortenString(corpus.get(token).getText(),20));
////			cnt++;if (cnt==5) break;
////		}
//
//		// sen-postok
////		cnt=0;
////		for(Sentence sen:corpus.getSentences()) {
////			System.out.println("sen-postoken: token="+sen.getPOSTokens().size());
////			cnt++;if (cnt==5) break;
////		}
//		// postok-sen
////		cnt=0;
////		for(POSToken token:corpus.getPOSTokens()) {
////			System.out.println("postoken: "+token);
////			//			System.out.println("postoken-sen: "+shortenString(corpus.get(token).getText(),20));
////			cnt++;if (cnt==5) break;
////		}
//
//		// pos-sen
//		//cnt=0;
////		for(String postag:corpus.getPOSTags()) {
////			URI posTagURI = URI.create("http:\\stanford.tagset#"+postag);
////			Query query = new DocumentAnnotationQuery(posTagURI, ResultType.Sentence);
////			Collection<Result> results = corpus.search(query);
////			System.out.println("query: "+query.toString());
////			for(Result res:results)
////				System.out.println("  "+res.toString());
////		}
//
//		// tokval-sen
////		cnt=0;
////		for(Token token:corpus.getTokens()) {
////			TokenValueQuery query = new TokenValueQuery(token, lang, ResultType.Sentence, true);
////			Collection<Result> results = corpus.search(query);
////			System.out.println("query: "+query.toString());
////			for(Result res:results)
////				System.out.println("  "+res.toString());
////			cnt++;if (cnt==100) break;
////		}
//
//		// term-sen
//		String[] termvals = {"minimum sublease payments", "amortised cost", "financial assets","at cost", "securieties", "risk securities","wind turbine","wind turbines","generator","generators","wind park","wind parks","megawatts","megawatt"};
//		for (int i = 0; i < termvals.length; i++) {
//			String termval = termvals[i];
//			Query query = new SentenceTextQuery(termval, lang, false);
//			Iterable<Result> results = corpus.search(query);
//			System.out.println("query: "+query.toString());
//			for(Result res:results)
//				System.out.println("  "+res.toString());
//		}
//
//	}
//
//	//	private static String shortenString(String in, int shortenLength) {
//	//		if (in.length()<shortenLength)
//	//			shortenLength=in.length();
//	//		return in.substring(0, shortenLength)+"...";
//	//	}
//
//}
