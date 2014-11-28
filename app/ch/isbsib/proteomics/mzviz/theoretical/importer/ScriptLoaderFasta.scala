package ch.isbsib.proteomics.mzviz.theoretical.importer

import ch.isbsib.proteomics.mzviz.theoretical.models.FastaEntry

import scala.io.Source
import java.io.{FileReader, FileNotFoundException, IOException}

/**
 * Load Fasta file
 * @author Trinidad Martín
 */
object ScriptLoaderFasta extends App {

  val filename = "test/resources/M_100small.fasta"
  var seq=""
  var ac=""

  try
    for (line <- Source.fromFile(filename).getLines()) {
      //Parse ac and seq
      val patternAc = """\|(.*)\|""".r
      if(patternAc.findFirstIn(line).isDefined){
        if (!seq.equals("")){
          //Create Fasta object
          val fastaObject = new FastaEntry(ac, seq)
          seq=""
        }
        val words=line.split("""\|""")
        ac=words(1)
        }
      else {
        seq+=line
        }

    }
  catch {
    case ex: FileNotFoundException => println("Couldn't find that file.")
    case ex: IOException => println("Had an IOException trying to read that file")
  }
  //Create last Fasta object
  val fastaObject = new FastaEntry(ac, seq)

}