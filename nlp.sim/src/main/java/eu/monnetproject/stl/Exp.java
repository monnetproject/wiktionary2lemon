package eu.monnetproject.stl;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.net.URI;
//import java.net.URL;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Set;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import eu.monnetproject.corpus.Corpus;
//import eu.monnetproject.corpus.NoSuchCorpusObjectException;
//import eu.monnetproject.corpus.UnimplementedQueryType;
//import eu.monnetproject.corpus.annotation.Annotation;
//import eu.monnetproject.corpus.annotator.AnnotationFactory;
//import eu.monnetproject.corpus.impl.LuceneCorpus;
//import eu.monnetproject.corpus.search.query.CorpusObjectType;
//import eu.monnetproject.corpus.search.query.Query;
//import eu.monnetproject.corpus.search.query.annotation.AnnotationQuery;
//import eu.monnetproject.corpus.search.query.annotation.DocumentAnnotationQuery;
//import eu.monnetproject.corpus.search.query.impl.CorpusObjectQuery;
//import eu.monnetproject.corpus.search.query.impl.SentenceTextQuery;
//import eu.monnetproject.corpus.search.result.Result;
//import eu.monnetproject.corpus.search.result.impl.DocumentResultImpl;
//import eu.monnetproject.corpus.search.result.impl.SentenceResultImpl;
//import eu.monnetproject.doc.Document;
//import eu.monnetproject.doc.DocumentExtractor;
//import eu.monnetproject.doc.DocumentFactory;
//import eu.monnetproject.doc.TextExtractor;
//import eu.monnetproject.doc.txt.DocumentExtractorImpl;
//import eu.monnetproject.doc.txt.TextFileExtractor;
//import eu.monnetproject.lang.Language;
//import eu.monnetproject.lang.UnsupportedLanguageException;
//import eu.monnetproject.sentence.Sentence;
//import eu.monnetproject.sentence.SentenceSplitter;
//import eu.monnetproject.sentence.AcronymSentenceSplitter;
//import eu.monnetproject.tokens.Token;
//import eu.monnetproject.tokenizer.Tokenizer;
//import eu.monnetproject.tokenizer.latin.LatinTokenizerImpl;
//
public class Exp {}
//
//	static Language lang = Language.ENGLISH;
//	static TextExtractor textExtractor = new TextFileExtractor();
//	static SentenceSplitter splitter = new AcronymSentenceSplitter();
//	static Tokenizer tokenizer = new LatinTokenizerImpl();
//	static DocumentExtractorImpl docExtractor = new DocumentExtractorImpl();
//
//	private static List<File> getDataFileList(File dataPath,String fileType) {
//		LinkedList<File> data = new LinkedList<File>();
//		try {
//			for(File f:FileReader.getFileListing(dataPath, fileType))
//				data.add(f);
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return data;
//	}
//
//	private static List<Document> getDataAsDocumentsWithMetaData(List<File> dataAsFiles,String fileType) {
//		LinkedList<Document> data = new LinkedList<Document>();
//		System.out.println("OK");
//		for(File f:dataAsFiles) {
//			String filePath;
//			try {
//				filePath = f.getCanonicalPath().toString();
//				//System.out.println(filePath);
//				//String langCode = lang.getIso639_1();
//				String startPattern = "^.*/raw/";
//				Set<URI> annotations = FilePathMetaDataExtractor.getMetaData(filePath, startPattern, fileType,0,4);
//				String shortName = filePath.replaceAll(startPattern, "");
//				System.out.println(shortName);
//				if (annotations.size()>1) {
//					//					System.out.println(shortName);
//					//					System.out.println(filePath);
//					//					for(URI anno:annotations)
//					//						System.out.println("  "+anno);
//					String text = textExtractor.getText(f);
//					String docname = shortName;
//					URL sourceURL = f.toURL();
//					List<Sentence> sentences = splitter.split(text);
//					//Document doc = DocumentFactory.makeDocument(docname, lang,text, sourceURL);
//					Document doc = DocumentFactory.makeDocument(docname, lang, sentences, sourceURL);
//					data.add(doc);
//					//System.out.println("  "+text.length());
//					//System.out.println("  "+sentences.size());
//				}
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		return data;
//	}
//
//
//	private static int getSize(Iterable<Token> tokens) {
//		Iterator<Token> it = tokens.iterator();
//		int cnt = 0;
//		while(it.hasNext()) {
//			it.next();
//			cnt++;
//		}
//		return cnt;
//	}
//
//	public static void main(String[] args) throws UnimplementedQueryType, UnsupportedLanguageException, NoSuchCorpusObjectException {
//
//		// corpus params
//		//String basedir = "/Users/tobys/Desktop/corpus/tmp/windenergy/release2/raw";
//		String basedir = "C:\\Documents and Settings\\tobwun\\My Documents\\deri\\data\\corpus\\windenergy";
//		String lastbasedir = "windenergy";
//		//		Pattern pattern = Pattern.compile("[^a-zA-Z]([a-zA-Z0-9]*)$");
//		//		Matcher matcher = pattern.matcher(basedir);
//		//		if (matcher.matches()) {
//		//			System.out.println("match");
//		//			System.out.println(matcher.group(1));
//		//		}
//
//		String finrepdir = "finreps"+File.separator+"en"+File.separator+"txt";
//		Corpus corpus = new LuceneCorpus();
//		String fileType = "txt";
//
//		// read files
//		System.out.println("1 - GATHER DATA");
//		List<File> files = new LinkedList<File>();
//		//String[] companies = {"Gamesa","Vestas","Nordex","Siemens","Suzlon"};
//		//String[] companies = {"Gamesa","Vestas","Nordex"};
//		String[] companies = {"Gamesa"};
//		int cnt=0;
//		for (int i = 0; i < companies.length; i++) {
//			String company = companies[i];
//			files.addAll(getDataFileList(new File(basedir+File.separator+company+File.separator+finrepdir),fileType));
//		}
//		cnt=0;
//		for(File file:files) {
//			cnt++;
//			System.out.println("  "+cnt+" "+file);
//		}
//
//		// init document extractor and create monnet documents
//		System.out.println("\n2 - CREATE MONNET DOCS");
//		docExtractor.setDocumentNameExtractionPattern("^.*"+lastbasedir+".");
//		docExtractor.setSentenceSplitter(splitter);
//		docExtractor.setTextExtractor(textExtractor);
//		List<Document> docs = docExtractor.getDocuments(files, lang);
//		cnt=0;
//		for(Document doc:docs) {
//			cnt++;
//			System.out.println("  "+cnt+" "+doc.getName());
//			corpus.addDocument(doc);
//		}
//		System.out.println(docs.size()+" documents");
//
//		// get annotations for each document
//		System.out.println("\n3 - ADD ANNOTATIONS");
//		for(Result res:corpus.search(new CorpusObjectQuery(CorpusObjectType.Document))) {
//			String corpusObjectID = res.getCorpusObjectID();
//			DocumentResultImpl docres = (DocumentResultImpl)res;
//			String docName = docres.getDocName();
//			System.out.println(docName+" ("+docres.getCorpusObjectID()+")");
//			Set<URI> annotations = FilePathMetaDataExtractor.getMetaData(docName.toString(), "^.*"+lastbasedir+".", "txt",0,4);
//			for(URI annURI:annotations) {
//				Annotation annotation = AnnotationFactory.makeAnnotation(annURI, corpusObjectID);
//				corpus.addAnnotation(annotation);
//			}
//		}
//		//
//		//		// semantic query
//		//		System.out.println("\n4 - SEARCH ALL Q1 REPORTS");
//		//		Query query = new DocumentAnnotationQuery(URI.create("http://unlp.deri.ie/WindCorpus#Q1"));
//		//		for(Result res:corpus.search(query)) {
//		//			DocumentResultImpl docres = (DocumentResultImpl)res;
//		//			System.out.println("  "+docres.getName()+"("+res.getCorpusObjectID()+")");
//		//		}
//
//		// some stats
//		System.out.println("\n5 - SOME DOCUMENT STATS (sentences, words)");
//		int scnt = 0;
//		cnt = 0;
//		int totalNumTokens = 0;
//		for(Result res:corpus.search(new CorpusObjectQuery(CorpusObjectType.Document))) {
//			DocumentResultImpl docres = (DocumentResultImpl)res;
//			String text = docres.getText();
//			int numberOfSentences = splitter.split(text).size();
//			int numberOfTokens = getSize(tokenizer.tokenize(text));
//			System.out.println(" "+docres.getDocName()+"  (s="+numberOfSentences+", w="+numberOfTokens+")");
//			cnt++;
//			scnt=scnt+numberOfSentences;
//			totalNumTokens=totalNumTokens+numberOfTokens;
//			if (cnt>20)
//				break;
//		}
//		System.out.println("\n sentences: "+scnt);
//		System.out.println(" tokens: "+totalNumTokens);
//
//		// search all documents
//		System.out.println("\n4 - SEARCH FIRST FIVE SENTENCE");
//		cnt = 0;
//		for(Result res:corpus.search(new CorpusObjectQuery(CorpusObjectType.Sentence))) {
//			SentenceResultImpl senres = (SentenceResultImpl)res;
//			if (senres.getText().length()>30) {
//				System.out.println(cnt+" sentence = "+senres.getText().substring(0, 30)+"...");
//				System.out.println("  docID="+senres.getReferencedCorpusObjectID());
//				System.out.println("  order="+senres.getOrder());
//				cnt++;	
//			}
//			if (cnt>5)
//				break;
//		}
//
////		// do a query
////		System.out.println("\n4 - SEARCH ALL Q1 REPORTS");
////		String[] searchTerms = {" assets "," revenue "," wind turbine "," wind park","lease payments"};
////		for (int i = 0; i < searchTerms.length; i++) {
////			String searchTerm = searchTerms[i];
////			System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
////			int rescnt = 0;
////			for(Result res:corpus.search(new SentenceTextQuery(searchTerm,Language.ENGLISH,false))) {
////				SentenceResultImpl senres = (SentenceResultImpl)res;
////				if (senres.getText().length()<200) {
////					System.out.println(senres);
////					System.out.println(senres.getDocID());
////					System.out.println(senres.getOrder());
////					rescnt++;
////				}
////			}
////			System.out.println("res="+rescnt);
////		}
//
//
//	}
//
//}
