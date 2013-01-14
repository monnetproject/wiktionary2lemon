// run as scala -cp ../cnf/repo/scalasemweb/scalasemweb-1.1.2.jar -DentityExpansionLimit=1000000 scripts/WordNet2lemon.scala

import scalasemweb.rdf.model._
import scalasemweb.rdf.io._
import scala.io._
import java.io._
import java.net._
import scala.xml._

object lemon extends NameSpace("lemon","http://www.monnet-project.eu/lemon#") {
  val Lexicon = this&"Lexicon"
  val topic = this&"topic"
  val entry = this&"entry"
  val LexicalEntry = this&"LexicalEntry"
  val Word = this&"Word"
  val Phrase = this&"Phrase"
  val Part = this&"Part"
  val language = this&"language"
  val lexicalVariant = this&"lexicalVariant"
  val form=this&"form"
  val canonicalForm=this&"canonicalForm"
  val otherForm=this&"otherForm"
  val abstractForm=this&"abstractForm"
  val LexicalForm=this&"LexicalForm"
  val representation=this&"representation"
  val writtenRep=this&"writtenRep"
  val formVariant=this&"formVariant"
  val decomposition=this&"decomposition"
  val Component=this&"Component"
  val element=this&"element"
  val synBehavior=this&"synBehavior"
  val Frame=this&"Frame"
  val synArg=this&"synArg"
  val Argument=this&"Argument"
  val marker=this&"marker"
  val SyntacticRoleMarker=this&"SyntacticRoleMarker"
  val sense=this&"sense"
  val LexicalSense=this&"LexicalSense"
  val isSenseOf=this&"isSenseOf"
  val context=this&"context"
  val condition=this&"condition"
  val propDomain=this&"propDomain"
  val propRange=this&"propRange"
  val definition=this&"definition"
  val value=this&"value"
  val reference=this&"reference"
  val lexicalization=this&"lexicalization"
  val prefSem=this&"prefSem"
  val altSem=this&"altSem"
  val hiddenSem=this&"hiddenSem"
  val semArg=this&"semArg"
  val subjOfProp=this&"subjOfProp"
  val objOfProp=this&"objOfProp"
  val isA=this&"isA"
  val optional=this&"optional"
  val semArgRef=this&"semArgRef"
  val senseRelation=this&"senseRelation"
  val equivalent=this&"equivalent"
  val incompatible=this&"incompatible"
  val broader=this&"broader"
  val narrower=this&"narrower"
  val phraseRoot=this&"phraseRoot"
  val Node=this&"Node"
  val constituent=this&"constituent"
  val separator=this&"separator"
  val edge=this&"edge"
  val leaf=this&"leaf"
  val Context=this&"Context"
  val Condition=this&"Condition"
  val Definition=this&"Definition"
  val property=this&"property"
  val example=this&"example"
  val Example=this&"Example"
  val subsense=this&"subsense"
}

object lexinfo extends NameSpace("lexinfo", "http://www.lexinfo.net/ontology/2.0/lexinfo#")

object wn20instances extends NameSpace("wn20instances", "http://www.w3.org/2006/03/wn/wn20/instances/")

object wn20schema extends NameSpace("wn20schema", "http://www.w3.org/2006/03/wn/wn20/schema/") {
  val Synset =                           this&"Synset"                                         
  val AdjectiveSynset =                  this&"AdjectiveSynset"  
  val AdjectiveSatelliteSynset =         this&"AdjectiveSatelliteSynset"  
  val AdverbSynset =                     this&"AdverbSynset"  
  val NounSynset =                       this&"NounSynset"  
  val VerbSynset =                       this&"VerbSynset"  
  val Word =                             this&"Word"  
  val Collocation =                      this&"Collocation"  
  val WordSense =                        this&"WordSense"  
  val AdjectiveWordSense =               this&"AdjectiveWordSense"
  val AdjectiveSatelliteWordSense =      this&"AdjectiveSatelliteWordSense"
  val AdverbWordSense =                  this&"AdverbWordSense"  
  val NounWordSense =                    this&"NounWordSense"  
  val VerbWordSense =                    this&"VerbWordSense"  
  
  
  val adjectivePertainsTo =               this&"adjectivePertainsTo"
  val adverbPertainsTo =                  this&"adverbPertainsTo"
  val antonymOf =                         this&"antonymOf"
  val attribute =                         this&"attribute"
  val causes =                            this&"causes"
  val classifiedBy =                      this&"classifiedBy"
  val classifiedByUsage =                 this&"classifiedByUsage"
  val classifiedByRegion =                this&"classifiedByRegion"
  val classifiedByTopic =                 this&"classifiedByTopic"
  val containsWordSense =                 this&"containsWordSense"
  val derivationallyRelated =             this&"derivationallyRelated"
  val entails =                           this&"entails"
  val frame =                             this&"frame"
  val gloss =                             this&"gloss"
  val hyponymOf =                         this&"hyponymOf"
  val lexicalForm =                       this&"lexicalForm"
  val meronymOf =                         this&"meronymOf"
  val substanceMeronymOf =                this&"substanceMeronymOf"
  val partMeronymOf =                     this&"partMeronymOf"
  val memberMeronymOf =                   this&"memberMeronymOf"
  val participleOf =                      this&"participleOf"
  
  val sameVerbGroupAs =                   this&"sameVerbGroupAs"
  val seeAlso =                           this&"seeAlso"
  val similarTo =                         this&"similarTo"
  val synsetId =                          this&"synsetId"
  val tagCount =                          this&"tagCount"
  val word =                              this&"word"
}

object WordNet2lemon {
  
  val lwn = NameSpace("lwn", "http://monnetproject.deri.ie/lemonsource/wordnet/")
  
  val lexicon = "http://monnetproject.deri.ie/lemonsource/wordnet".uri
  
  def run(file : String, out : PrintStream) {
    val trips = getTrips(file) +
    (lexicon %> RDF._type %> lemon.Lexicon) +
    (lexicon %> lemon.language %> "en")
    
    
    val printer = new TurtlePrinter()
    out.println(printer.format(trips))
  }
  
  def loadTrips(file : String) = {
    val src = XML.loadFile(file)
    RDFXML.convert(src)
  }
  
  def getTrips(file : String) = {
    val src = loadTrips(file)
    src flatMap { convert(_) }
  }
  
  def enc(str : String) = URLEncoder.encode(str,"UTF-8").replaceAll("^([0-9%])","x-$1")
  
  val ws2leR = """w?o?r?d?sense-(.*)-(\d+)"""r
  val ws2leRAlt = """word-(.*)"""r
  
  def ws2le(suffix : String) = {
    suffix match {
      case ws2leR(x,n) => lwn&?enc(x)
      case ws2leRAlt(x) => lwn&?enc(x)
    }
  }

  def ws2wne(ns : NameSpace, suffix : String) = {
    suffix match {
      case ws2leR(x,n) => ns&?enc(x)
      case ws2leRAlt(x) => ns&?enc(x)
    }
  }
  
  val ss2lsR = "w?o?r?d?sense-(.*)-(\\d+)"r
  
  def ws2ls(suffix : String) = {
    val ss2lsR(x,n) = suffix
    lwn &? enc(x)+"#sense" + n
  }

  def ws2f(suffix : String) = {
    val ws2leR(x,n) = suffix
    lwn &? enc(x)+"#frame"+n
  }
  
  
  def convert(stat : Triple) : List[Triple] = stat match {
    case Triple(QName(ns, suffix), RDF._type, wn20schema.AdjectiveWordSense) => {
        List(lexicon %> lemon.entry %> ws2le(suffix),
             ws2le(suffix) %> RDF._type %> lemon.LexicalEntry,
             ws2le(suffix) %> RDFS.seeAlso %> ws2wne(ns,suffix),
             ws2le(suffix) %> (lexinfo&"partOfSpeech") %> (lexinfo&"adjective"),
             ws2le(suffix) %> lemon.sense %> ws2ls(suffix))
      }
    case Triple(QName(ns, suffix), RDF._type, wn20schema.AdjectiveSatelliteWordSense) => {
        List(lexicon %> lemon.entry %> ws2le(suffix),
             ws2le(suffix) %> RDF._type %> lemon.LexicalEntry,
             ws2le(suffix) %> RDFS.seeAlso %> ws2wne(ns,suffix),
             ws2le(suffix) %> (lexinfo&"partOfSpeech") %> (lexinfo&"adjective"),
             ws2le(suffix) %> lemon.sense %> ws2ls(suffix))
      }
    case Triple(QName(ns, suffix), RDF._type, wn20schema.AdverbWordSense) => {
        List(lexicon %> lemon.entry %> ws2le(suffix),
             ws2le(suffix) %> RDF._type %> lemon.LexicalEntry,
             ws2le(suffix) %> RDFS.seeAlso %> ws2wne(ns,suffix),
             ws2le(suffix) %> (lexinfo&"partOfSpeech") %> (lexinfo&"adverb"),
             ws2le(suffix) %> lemon.sense %> ws2ls(suffix))
      }
    case Triple(QName(ns, suffix), RDF._type, wn20schema.NounWordSense) => {
        List(lexicon %> lemon.entry %> ws2le(suffix),
             ws2le(suffix) %> RDF._type %> lemon.LexicalEntry,
             ws2le(suffix) %> RDFS.seeAlso %> ws2wne(ns,suffix),
             ws2le(suffix) %> (lexinfo&"partOfSpeech") %> (lexinfo&"noun"),
             ws2le(suffix) %> lemon.sense %> ws2ls(suffix))
      }
    case Triple(QName(ns, suffix), RDF._type, wn20schema.VerbWordSense) => {
        List(lexicon %> lemon.entry %> ws2le(suffix),
             ws2le(suffix) %> RDF._type %> lemon.LexicalEntry,
             ws2le(suffix) %> RDFS.seeAlso %> ws2wne(ns,suffix),
             ws2le(suffix) %> (lexinfo&"partOfSpeech") %> (lexinfo&"verb"),
             ws2le(suffix) %> lemon.sense %> ws2ls(suffix))
      }
    case Triple(QName(_,synset), wn20schema.containsWordSense, QName(_,wordsense)) => {
        List(ws2ls(wordsense) %> lemon.reference %> (wn20instances&?enc(synset)))
      }
    case Triple(QName(_,wordsense), wn20schema.word, QName(_,word)) => {
        List(ws2le(wordsense) %> lemon.canonicalForm %> (lwn&?enc(word+"-canonicalForm"))) :::
        (variants.getOrElse(word,Nil) map {
            inflected => ws2le(wordsense) %> lemon.otherForm %> (lwn&?enc(word+"-otherForm_"+inflected))
          })
      }
    case Triple(QName(_,word), wn20schema.lexicalForm, lit : Literal) => {
        List((lwn&?enc(word+"-canonicalForm")) %> lemon.writtenRep %> LangLiteral(lit.stringValue,"en")) :::
        (variants.getOrElse(lit.stringValue, Nil) map { 
            inflected => (lwn&?enc(word+"-otherForm_"+inflected)) %> lemon.writtenRep %> LangLiteral(inflected,"en")
          })
      }
    case Triple(QName(_,wordsense1), wn20schema.adjectivePertainsTo, QName(_,wordsense2)) => {
        List(ws2ls(wordsense1) %> (lexinfo&"pertainsTo") %> ws2ls(wordsense2))
      }
    case Triple(QName(_,wordsense1), wn20schema.adverbPertainsTo, QName(_,wordsense2)) => {
        List(ws2ls(wordsense1) %> (lexinfo&"pertainsTo") %> ws2ls(wordsense2))
      }
    case Triple(QName(_,wordsense1), wn20schema.antonymOf, QName(_,wordsense2)) => {
        List(ws2ls(wordsense1) %> (lexinfo&"antonym") %> ws2ls(wordsense2))
      }
    case Triple(QName(_,wordsense1), wn20schema.derivationallyRelated, QName(_,wordsense2)) => {
        List(ws2le(wordsense1) %> (lexinfo&"derivedForm") %> ws2le(wordsense2))
      }
    case Triple(QName(_,wordsense1), wn20schema.participleOf, QName(_,wordsense2)) => {
        List(ws2le(wordsense1) %> (lexinfo&"participleFormOf") %> ws2le(wordsense2))
      }
    case Triple(QName(_,wordsense1), wn20schema.seeAlso, QName(_,wordsense2)) => {
        List(ws2ls(wordsense1) %> (lexinfo&"relatedTerm") %> ws2ls(wordsense2))
      }
    case Triple(QName(_,wordsense1), wn20schema.frame, lit : Literal) => {
        (ws2le(wordsense1) %> lemon.synBehavior %> ws2f(wordsense1)) ::
        frameTrips(lwn,wordsense1,ws2f(wordsense1), lit.stringValue)
      }
    case Triple(QName(_,s1), y, QName(_,s2)) => {
        if((s1 matches """synset-.*""") &&
           (s2 matches """synset-.*""")) {
          List((lwn&?enc(s1)) %> y %> (lwn&?enc(s2)))
        } else {
          Nil
        }
      }
    case _ => Nil
  }
  
  def frameTrips(nameSpace : NameSpace, suffix : String, frameRes : NamedNode, frameExample : String) : List[Triple] = {
    frameExample match {
      case "It ----s that CLAUSE" => { 
          System.err.println(frameExample); 
          Nil 
        }
      case "Somebody ----s" => {
          val arg1 = nameSpace &? (suffix + "-subject")
          List(frameRes %> (lexinfo&"subject") %> arg1,
               frameRes %> RDF._type %> (lexinfo&"IntransitiveFrame"))
        }
      case "Somebody ----s PP" => {
          val arg1 = nameSpace &? (suffix + "-subject")
          val arg2 = nameSpace &? (suffix + "-prepositionalObject")
          List(frameRes %> (lexinfo&"subject") %> arg1,
               frameRes %> (lexinfo&"prepositionalObject") %> arg2,
               frameRes %> RDF._type %> (lexinfo&"IntransitivePPFrame"))
        }
      case "Somebody ----s somebody" => {
          val arg1 = nameSpace &? (suffix + "-subject")
          val arg2 = nameSpace &? (suffix + "-directObject")
          List(frameRes %> (lexinfo&"subject") %> arg1,
               frameRes %> (lexinfo&"directObject") %> arg2,
               frameRes %> RDF._type %> (lexinfo&"TransitiveFrame"))
        } 
      case "Somebody ----s somebody something" => {
          val arg1 = nameSpace &? (suffix + "-subject")
          val arg2 = nameSpace &? (suffix + "-directObject")
          val arg3 = nameSpace &? (suffix + "-indirectObject")
          List(frameRes %> (lexinfo&"subject") %> arg1,
               frameRes %> (lexinfo&"directObject") %> arg2,
               frameRes %> (lexinfo&"indirectObject") %> arg3,
               frameRes %> RDF._type %> (lexinfo&"IntransitiveFrame"))
        } 
      case "Somebody ----s somebody to INFINITIVE" => {
          System.err.println(frameExample); 
          Nil 
        }
      case "Somebody ----s something" => {
          val arg1 = nameSpace &? (suffix + "-subject")
          val arg2 = nameSpace &? (suffix + "-directObject")
          List(frameRes %> (lexinfo&"subject") %> arg1,
               frameRes %> (lexinfo&"directObject") %> arg2,
               frameRes %> RDF._type %> (lexinfo&"TransitiveFrame"))
        } 
      case "Somebody ----s something PP" => {
          val arg1 = nameSpace &? (suffix + "-subject")
          val arg2 = nameSpace &? (suffix + "-directObject")
          val arg3 = nameSpace &? (suffix + "-prepositionalObject")
          List(frameRes %> (lexinfo&"subject") %> arg1,
               frameRes %> (lexinfo&"directObject") %> arg2,
               frameRes %> (lexinfo&"prepositionalObject") %> arg3,
               frameRes %> RDF._type %> (lexinfo&"TransitivePPFrame"))
        }
      case "Somebody ----s something to somebody"=> {
          val arg1 = nameSpace &? (suffix + "-subject")
          val arg2 = nameSpace &? (suffix + "-directObject")
          val arg3 = nameSpace &? (suffix + "-indirectObject")
          List(frameRes %> (lexinfo&"subject") %> arg1,
               frameRes %> (lexinfo&"directObject") %> arg2,
               frameRes %> (lexinfo&"indirectObject") %> arg3,
               frameRes %> RDF._type %> (lexinfo&"DitransitiveFrame"))
        }
      case "Somebody ----s that CLAUSE" => {
          System.err.println(frameExample); 
          Nil
        }
      case "Somebody ----s VERB-ing" => {
          System.err.println(frameExample); 
          Nil
        }
      case "Somebody ----s whether INFINITIVE" => {
          System.err.println(frameExample); 
          Nil
        } 
      case "Something is ----ing PP" => {
          System.err.println(frameExample); 
          Nil
        } 
      case "Something ----s" => {
          val arg1 = nameSpace &? (suffix + "-subject")
          List(frameRes %> (lexinfo&"subject") %> arg1,
               frameRes %> RDF._type %> (lexinfo&"IntransitiveFrame"))
        }
      case "Something ----s Adjective/Noun" => {
          val arg1 = nameSpace &? (suffix + "-subject")
          val arg2 = nameSpace &? (suffix + "-directObject")
          List(frameRes %> (lexinfo&"subject") %> arg1,
               frameRes %> (lexinfo&"directObject") %> arg2,
               frameRes %> RDF._type %> (lexinfo&"TransitiveFrame"))
        }
      case "Something ----s somebody" => {
          val arg1 = nameSpace &? (suffix + "-subject")
          val arg2 = nameSpace &? (suffix + "-directObject")
          List(frameRes %> (lexinfo&"subject") %> arg1,
               frameRes %> (lexinfo&"directObject") %> arg2,
               frameRes %> RDF._type %> (lexinfo&"TransitiveFrame"))
        }
      case "Something ----s something" => {
          val arg1 = nameSpace &? (suffix + "-subject")
          val arg2 = nameSpace &? (suffix + "-directObject")
          List(frameRes %> (lexinfo&"subject") %> arg1,
               frameRes %> (lexinfo&"directObject") %> arg2,
               frameRes %> RDF._type %> (lexinfo&"TransitiveFrame"))
        }
      case "Something ----s something Adjective/Noun" => {
          val arg1 = nameSpace &? (suffix + "-subject")
          val arg2 = nameSpace &? (suffix + "-directObject")
          val arg3 = nameSpace &? (suffix + "-indirectObject")
          List(frameRes %> (lexinfo&"subject") %> arg1,
               frameRes %> (lexinfo&"directObject") %> arg2,
               frameRes %> (lexinfo&"indirectObject") %> arg3,
               frameRes %> RDF._type %> (lexinfo&"DitransitiveFrame"))
        }
      case x => println("Unrecognized: " + x) ; Nil
    }
  }
  
  private var variants = scala.collection.mutable.HashMap[String,List[String]]()
  
  def readVariant(variant : File) { 
    val in = new BufferedSource(new FileInputStream(variant))
    val splitRegex = """(.*) (.*)""".r
    for(line <- in.getLines()) {
      line match {
        case splitRegex(inflected,canonical) => if(variants contains canonical) {
            variants += (canonical -> (inflected :: variants(canonical)))
          } else {
            variants += (canonical -> (inflected :: Nil))
          }
      }
    }
  }
}

System.setProperty("entityExpansionLimit","1000000")

//object WordNet2lemonRunner extends Application {
  try {
    new File("lexicon-tmp/").mkdir
  } catch {
    case x : Exception => 
  }
  val srcDir = new File("dumps/")
  for(srcFile <- srcDir.listFiles) {
    if(srcFile.getName matches """.*\.exc""") {
      WordNet2lemon.readVariant(srcFile)
      System.err.println("Read: "+srcFile.getName())
    }
  }
  for(srcFile <- srcDir.listFiles) {
    if(srcFile.getName matches """wordnet-.*.rdf""") {
      System.err.println("Reading: "+srcFile.getName())
      WordNet2lemon.run(srcFile.getPath(), new PrintStream("lexicon-tmp/"+srcFile.getName()))
      System.err.println("Read: "+srcFile.getName())
    } 
  }
//}
