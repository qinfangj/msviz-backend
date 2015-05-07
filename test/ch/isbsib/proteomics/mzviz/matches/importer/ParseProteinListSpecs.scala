package ch.isbsib.proteomics.mzviz.matches.importer

import java.io.File

import ch.isbsib.proteomics.mzviz.experimental.{SpectrumIdentifictionItem, RunId, SpectrumUniqueId}
import ch.isbsib.proteomics.mzviz.matches.SearchId
import ch.isbsib.proteomics.mzviz.matches.models.{ProteinIdent, PepSpectraMatch, ProteinRef}
import ch.isbsib.proteomics.mzviz.theoretical.models.SearchDatabase
import ch.isbsib.proteomics.mzviz.theoretical.{AccessionCode, SequenceSource}
import org.specs2.mutable.Specification

/**
 * @author Roman Mylonas, Trinidad Martin & Alexandre Masselot
 * copyright 2014-2015, SIB Swiss Institute of Bioinformatics
 */

class ParseProteinListSpecs extends Specification {


  "parseSpectrumIdAndTitleRelation" should {

    "check size and title" in {
      val mzIdentML = scala.xml.XML.loadFile(new File("test/resources/F001644.mzid"))
      val spIdTitleRelation = ParseProteinList.parseSpectrumIdAndTitleRelation(mzIdentML \\ "SpectrumIdentificationList")

      spIdTitleRelation.get(SpectrumIdentifictionItem("SII_71_1")) must equalTo(Some(SpectrumUniqueId("20141008_BSA_25cm_column2.9985.9985.2")))

      spIdTitleRelation.size must equalTo(437)
    }

  }


  "convertDbSeqId" should {

    val db1 = SearchDatabase(id = "SDB_contaminants_PAF", version="1.0", entries=999)
    val db2 = SearchDatabase(id = "SDB_custom", version="2.0", entries=100)
    val searchDbs = Seq(db1, db2)

    "check convertion 1" in {
      def acAndDb = ParseProteinList.convertDbSeqId("DBSeq_1_Q0IIK2", searchDbs).get

      acAndDb._1 must equalTo(AccessionCode("Q0IIK2"))
      acAndDb._2 must equalTo(SequenceSource("SDB_contaminants_PAF"))
    }

    "check convertion 2" in {
      def acAndDb = ParseProteinList.convertDbSeqId("DBSeq_2_sp|TRFL_HUMAN|", searchDbs).get

      acAndDb._1 must equalTo(AccessionCode("sp|TRFL_HUMAN|"))
      acAndDb._2 must equalTo(SequenceSource("SDB_custom"))
    }

  }


  "parse protein list" should {

    val db1 = SearchDatabase(id = "SDB_contaminants_PAF", version="1.0", entries=999)
    val db2 = SearchDatabase(id = "SDB_custom", version="2.0", entries=100)
    val searchDbs = Seq(db1, db2)

    "check size and title" in {
      val mzIdentML = new File("test/resources/F001644.mzid")
      val spIdTitleRelation = ParseProteinList.parseProtList(mzIdentML, SearchId("hoho"), searchDbs)

      spIdTitleRelation.size must equalTo(24)

      // check main protein
      spIdTitleRelation(0).mainProt.proteinAC mustEqual(AccessionCode("P02769"))
      spIdTitleRelation(0).mainProt.nrPsms mustEqual(368)
      spIdTitleRelation(0).mainProt.passThreshold mustEqual(true)
      spIdTitleRelation(0).mainProt.score.mainScore mustEqual(10884.08)
      spIdTitleRelation(0).mainProt.nrSequences mustEqual(32)

      // check subsets
      spIdTitleRelation(0).subsetProts.size mustEqual(2)
      spIdTitleRelation(0).subsetProts(0).proteinAC mustEqual(AccessionCode("P02768-1"))
      spIdTitleRelation(0).subsetProts(1).proteinAC mustEqual(AccessionCode("Q3SZ57"))

      // check other protein
      spIdTitleRelation(7).mainProt.proteinAC mustEqual(AccessionCode("mAbEDAR_1_2"))
      spIdTitleRelation(8).mainProt.proteinAC mustEqual(AccessionCode("sp|OVAL_CHICK|"))
      spIdTitleRelation(11).mainProt.proteinAC mustEqual(AccessionCode("REFSEQ:XP_001252647"))

    }




  }


}