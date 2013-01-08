// run command scala -cp ../cnf/repo/scalasemweb/scalasemweb-1.1.2.jar MergeSenses.scala

import eu.monnetproject.sim.string._
import scalasemweb.rdf.model._
import scalasemweb.rdf.io._
import java.io._

object MergeSenses {
  val levenshtein = new Levenshtein()
  
  def findMerges(triples : TripleSet) = {
    for(entry %> _ %> _ <- triples.get(None,Some("""http://www.monnet-project.eu/lemon#canonicalForm""".uri),None)) {
      val senses = for(_ %> _ %> (sense : Resource) <- triples.get(Some(entry), Some("""http://www.monnet-project.eu/lemon#sense""".uri), None)) yield {
        for(_ %> _ %> define <- triples.get(Some(sense),Some("""http://www.monnet-project.eu/lemon#definition""".uri),None)) yield {
          triples.get(Some(define.asInstanceOf[Resource]),Some("""http://www.monnet-project.eu/lemon#value""".uri),None).headOption match {
            case Some(_ %> _ %> defineVal) => defineVal match {
                case d : Literal => d.stringValue
                case _ => ""
              }
            case _ => ""
          }
        }
      }
    
      for(sense1 <- senses) {
        for(sense2 <- senses ; if sense1 != sense2 && sense1 != "" && sense2 != "") {
          for(def1 <- sense1 ; def2 <- sense2) {
            println("\"" + def1 + "\",\"" + def2+"\","+levenshtein.getScore(def1,def2))
          }
        }
      }
    }
  }
}

MergeSenses.findMerges(Turtle.parse(new FileReader("dumps/wiktionary-short.ttl")))