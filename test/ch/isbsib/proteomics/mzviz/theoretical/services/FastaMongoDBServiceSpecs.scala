package ch.isbsib.proteomics.mzviz.theoretical.services

import ch.isbsib.proteomics.mzviz.commons.{SpectraSource, TempMongoDBForSpecs}
import ch.isbsib.proteomics.mzviz.experimental.RunId
import ch.isbsib.proteomics.mzviz.experimental.importer.LoaderMGF
import ch.isbsib.proteomics.mzviz.experimental.services.ExpMongoDBService
import ch.isbsib.proteomics.mzviz.matches.importer.LoaderMzIdent
import ch.isbsib.proteomics.mzviz.theoretical.{SequenceSource, AccessionCode}
import ch.isbsib.proteomics.mzviz.theoretical.importer.FastaParser
import ch.isbsib.proteomics.mzviz.theoretical.models.FastaEntry
import ch.isbsib.proteomics.mzviz.theoretical.services.SequenceMongoDBService
import org.expasy.mzjava.proteomics.io.ms.ident.pepxml.v117.MsmsPipelineAnalysis.MsmsRunSummary.SearchSummary.SequenceSearchConstraint
import org.scalacheck.Prop.True
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.specs2.mutable.Specification

import scala.collection.mutable
import scala.concurrent.Future

/**
 * Created by tmartinc on 05/12/14.
 */


class FastaMongoDBServiceSpecs extends Specification with ScalaFutures {
  implicit val defaultPatience =
    PatienceConfig(timeout = Span(15, Seconds), interval = Span(5000, Millis))

  /**
   * extends the temp mongodatabase and add a exp service above it
   */
  trait TempMongoDBService extends TempMongoDBForSpecs {
    val service = new SequenceMongoDBService(db)
  }


  "insert" should {
    "insert  2" in new TempMongoDBService {
      val entries = FastaParser("test/resources/M_100small.fasta").parse;
      val n: Int = service.insert(entries).futureValue
      n must equalTo(2)
    }
  }

  "listSources" should {
    "get 2 fasta" in new TempMongoDBService {

      val f1 = service.insert(FastaParser("test/resources/M_100small.fasta", SequenceSource("small-1")).parse)
      val f2 = service.insert(FastaParser("test/resources/M_100small.fasta", SequenceSource("small-2")).parse)
      Future.sequence(List(f1, f2)).futureValue
      val sources = service.listSources.futureValue.sortBy(_.value) mustEqual (List(SequenceSource("small-1"), SequenceSource("small-2")))
    }
  }

  "countSequences" should {
    "get 2 sequences" in new TempMongoDBService {
      val f1 = service.insert(FastaParser("test/resources/M_100small.fasta", SequenceSource("small-1")).parse)
      val f2 = service.insert(FastaParser("test/resources/M_100small.fasta", SequenceSource("small-2")).parse)
      Future.sequence(List(f1, f2)).futureValue
      val n: Int = service.countSequencesBySource(SequenceSource("small-1")).futureValue
      n must equalTo(2)
    }
  }

  "deleteAllBySource" should {
    "remove 2 sequences" in new TempMongoDBService {
      val f1 = service.insert(FastaParser("test/resources/M_100small.fasta", SequenceSource("small-1")).parse)
      val f2 = service.insert(FastaParser("test/resources/M_100small.fasta", SequenceSource("small-2")).parse)
      Future.sequence(List(f1, f2)).futureValue
      //remove
      val n: Boolean = service.deleteAllBySource(SequenceSource("small-1")).futureValue
      n must equalTo(true)
      val n2: Int = service.countSequencesBySource(SequenceSource("small-1")).futureValue
      n2 must equalTo(0)
    }
  }

  "findAllEntriesBySource" should {
    "find all" in new TempMongoDBService {

      val f1 = service.insert(FastaParser("test/resources/M_100small.fasta", SequenceSource("small-1")).parse).futureValue
      val sequenceList = service.findAllEntriesBySource(SequenceSource("small-1")).futureValue
      Thread.sleep(200)
      sequenceList.size must equalTo(2)

      //sequenceList(0).toString must equalTo(">P04899\nMGCTVSAEDKAAAERSKMIDKNLREDGEKAAREVKLLLLGAGESGKSTIVKQMKIIHEDGYSEEECRQYRAVVYSNTIQSIMAIVKAMGNLQIDFADPSRADDARQLFALSCTAEEQGVLPDDLSGVIRRLWADHGVQACFGRSREYQLNDSAAYYLNDLERIAQSDYIPTQQDVLRTRVKTTGIVETHFTFKDLHFKMFDVGGQRSERKKWIHCFEGVTAIIFCVALSAYDLVLAEDEEMNRMHESMKLFDSICNNKWFTDTSIILFLNKKDLFEEKITHSPLTICFPEYTGANKYDEAASYIQSKFEDLNKRKDTKEIYTHFTCATDTKNVQFVFDAVTDVIIKNNLKDCGLF")
      //sequenceList(1).toString must equalTo(">P07355\nMSTVHEILCKLSLEGDHSTPPSAYGSVKAYTNFDAERDALNIETAIKTKGVDEVTIVNILTNRSNAQRQDIAFAYQRRTKKELASALKSALSGHLETVILGLLKTPAQYDASELKASMKGLGTDEDSLIEIICSRTNQELQEINRVYKEMYKTDLEKDIISDTSGDFRKLMVALAKGRRAEDGSVIDYELIDQDARDLYDAGVKRKGTDVPKWISIMTERSVPHLQKVFDRYKSYSPYDMLESIRKEVKGDLENAFLNLVQCIQNKPLYFADRLYDSMKGKGTRDKVLIRIMVSRSEVDMLKIRSEFKRKYGKSLYYYIQQDTKGDYQKALLYLCGGDD")
      sequenceList.toString() must equalTo("List(>P04899\nMGCTVSAEDKAAAERSKMIDKNLREDGEKAAREVKLLLLGAGESGKSTIVKQMKIIHEDGYSEEECRQYRAVVYSNTIQSIMAIVKAMGNLQIDFADPSRADDARQLFALSCTAEEQGVLPDDLSGVIRRLWADHGVQACFGRSREYQLNDSAAYYLNDLERIAQSDYIPTQQDVLRTRVKTTGIVETHFTFKDLHFKMFDVGGQRSERKKWIHCFEGVTAIIFCVALSAYDLVLAEDEEMNRMHESMKLFDSICNNKWFTDTSIILFLNKKDLFEEKITHSPLTICFPEYTGANKYDEAASYIQSKFEDLNKRKDTKEIYTHFTCATDTKNVQFVFDAVTDVIIKNNLKDCGLF, >P07355\nMSTVHEILCKLSLEGDHSTPPSAYGSVKAYTNFDAERDALNIETAIKTKGVDEVTIVNILTNRSNAQRQDIAFAYQRRTKKELASALKSALSGHLETVILGLLKTPAQYDASELKASMKGLGTDEDSLIEIICSRTNQELQEINRVYKEMYKTDLEKDIISDTSGDFRKLMVALAKGRRAEDGSVIDYELIDQDARDLYDAGVKRKGTDVPKWISIMTERSVPHLQKVFDRYKSYSPYDMLESIRKEVKGDLENAFLNLVQCIQNKPLYFADRLYDSMKGKGTRDKVLIRIMVSRSEVDMLKIRSEFKRKYGKSLYYYIQQDTKGDYQKALLYLCGGDD)")
    }
  }

  "findEntryByAccessionCodeAndSource" should {
    "find one entry" in new TempMongoDBService {

      val f1 = service.insert(FastaParser("test/resources/M_100small.fasta", SequenceSource("small-1")).parse).futureValue
      val entry = service.findEntryByAccessionCodeAndSource(AccessionCode("P04899"),SequenceSource("small-1")).futureValue
      Thread.sleep(200)
      entry.ac must equalTo(AccessionCode("P04899"))
      entry.sequence must equalTo("MGCTVSAEDKAAAERSKMIDKNLREDGEKAAREVKLLLLGAGESGKSTIVKQMKIIHEDGYSEEECRQYRAVVYSNTIQSIMAIVKAMGNLQIDFADPSRADDARQLFALSCTAEEQGVLPDDLSGVIRRLWADHGVQACFGRSREYQLNDSAAYYLNDLERIAQSDYIPTQQDVLRTRVKTTGIVETHFTFKDLHFKMFDVGGQRSERKKWIHCFEGVTAIIFCVALSAYDLVLAEDEEMNRMHESMKLFDSICNNKWFTDTSIILFLNKKDLFEEKITHSPLTICFPEYTGANKYDEAASYIQSKFEDLNKRKDTKEIYTHFTCATDTKNVQFVFDAVTDVIIKNNLKDCGLF")

    }
  }


}
