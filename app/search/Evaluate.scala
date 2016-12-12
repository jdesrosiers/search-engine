package search

import java.net.URLEncoder
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.io.Source
import QueryEngine._

object Evaluate extends App {

  var key = "AIzaSyCWL4S20mEvYSPDmjqi_lvOGVyjYvlbaKI";
  var cx = "006411474838894056030:3crxxx-ydi8";

  val queries = List(
      "mondego",
      "machine learning",
      "software engineering",
      "security",
      "student affairs",
      "graduate courses",
      "Crista Lopes",
      "REST",
      "computer games",
      "information retrieval"
    )

  var totalFMeasure = 0.0
  var results = Map[String, List[Double]]()
  queries.foreach { case query =>
    /*
    val encodedQuery = URLEncoder.encode(s"$query -filetype:pdf -filetype:ppt -filetype:pptx -filetype:txt -filetype:ps -filetype:gz", "UTF-8")

    var google = Json.parse(Source.fromURL(s"https://www.googleapis.com/customsearch/v1?key=$key&cx=$cx&q=$encodedQuery&start=1").mkString)
    var links = (google \ "items" \\ "link").map { case url => normalizeUrl(url.as[String]) }
    var ideal = links.zip(20.0 to 11.0 by -1.0).toMap
    */
    val ideal = GoogleFixture.fixtures(query)

    val result = QueryEngine.search(query)
    val relevances = result.items
      .map(page => (normalizeUrl(page.link) -> ideal.getOrElse(normalizeUrl(page.link), 0.0)))
      .toList
    println(query)
    relevances.foreach(println)
    val fmeas = Evaluation.fMeasure(ideal.map(_._1).toList, relevances.filter(_._2 > 0.0).map(_._1))
    println("F-measure:   " + fmeas)
    totalFMeasure += fmeas
    results = results + (query -> Evaluation.ndcg(relevances.unzip._2))
  }

  results.foreach { case (query, ndcg) => println(s"$query: $ndcg " + ndcg.reduce(_ + _)) }
  val totalScore = results
    .map { case (query, ndcg) =>
      val score = ndcg.reduce(_ + _)
      if (score.isNaN) 0.0 else score
    }
    .reduce(_ + _)
  println("Total Score:     " + totalScore)
  println("Total F-Measure: " + totalFMeasure)

  def normalizeUrl(url: String) = url.replaceAll("""^http(s)?://""", "").replaceAll("""index.html$""", "")

}

object GoogleFixture {

  val mondego = Map(
    "mondego.ics.uci.edu/" -> 40.0,
    "mondego.ics.uci.edu/projects/clonedetection/" -> 39.0,
    "mondego.ics.uci.edu/projects/SourcererCC/" -> 38.0,
    "www.ics.uci.edu/~lopes/" -> 37.0,
    "mondego.ics.uci.edu/datasets/wikipedia-events/" -> 36.0,
    "mondego.ics.uci.edu/datasets/" -> 35.0,
    "mondego.ics.uci.edu/datasets/wikipedia-events/files/" -> 34.0,
    "mondego.ics.uci.edu/projects/yelp/" -> 33.0,
    "mondego.ics.uci.edu/projects/clonedetection/tool/" -> 32.0,
    "mondego.ics.uci.edu/projects/clonedetection/tool/latest/plugins/" -> 31.0,
    "mondego.ics.uci.edu/projects/clonedetection/tool/latest/" -> 30.0,
    "mondego.ics.uci.edu/projects/clonedetection/tool/demo/" -> 29.0,
    "mondego.ics.uci.edu/projects/clonedetection/tool/latest/features/" -> 28.0,
    "mondego.ics.uci.edu/projects/clonedetection/tool/source/" -> 27.0,
    "www.ics.uci.edu/~lopes/datasets/sourcerer-maven-aug12.html" -> 26.0,
    "mondego.ics.uci.edu/icsergen/" -> 25.0,
    "mondego.ics.uci.edu/projects/mavenpopandqual/" -> 24.0,
    "mondego.ics.uci.edu/projects/bugpatternsinclones/" -> 23.0,
    "www.ics.uci.edu/~kay/courses/i141/hw/asst3.html" -> 22.0,
    "www.ics.uci.edu/~lopes/datasets/Koders-log-2007.html" -> 21.0,
    "www.ics.uci.edu/~lopes/datasets/SDS_source-repo-18k.html" -> 20.0,
    "www.ics.uci.edu/~lopes/datasets/" -> 19.0,
    "www.ics.uci.edu/~djp3/classes/2006_03_30_ICS105/" -> 18.0,
    "networkdata.ics.uci.edu/netdata/html/00Index.html" -> 17.0,
    "ecodataportal.ics.uci.edu/" -> 16.0,
    "networkdata.ics.uci.edu/netdata/html/" -> 15.0,
    "www.ics.uci.edu/community/news/notes/notes_2014.php" -> 14.0,
    "www.ics.uci.edu/community/news/notes/notes_2013.php" -> 13.0
  )

  val machineLearning = Map(
    "archive.ics.uci.edu/ml/" -> 40.0,
    "archive.ics.uci.edu/ml/datasets.html" -> 39.0,
    "mlearn.ics.uci.edu/MLRepository.html" -> 38.0,
    "archive.ics.uci.edu/ml/machine-learning-databases/" -> 37.0,
    "archive.ics.uci.edu/ml/machine-learning-databases/wine-quality/" -> 36.0,
    "archive.ics.uci.edu/ml/citation_policy.html" -> 35.0,
    "archive.ics.uci.edu/ml/about.html" -> 34.0,
    "archive.ics.uci.edu/ml/datasets.html?format=&task=reg&att=&area=&numAtt=&numIns=&type=&sort=nameUp&view=table" -> 33.0,
    "archive.ics.uci.edu/ml/datasets/Spambase" -> 32.0,
    "archive.ics.uci.edu/ml/datasets/Echocardiogram" -> 31.0,
    "archive.ics.uci.edu/ml/machine-learning-databases/wine/" -> 30.0,
    "archive.ics.uci.edu/ml/machine-learning-databases/adult/" -> 29.0,
    "archive.ics.uci.edu/ml/datasets/Cardiotocography" -> 28.0,
    "archive.ics.uci.edu/ml/datasets/Balloons" -> 27.0,
    "archive.ics.uci.edu/ml/machine-learning-databases/statlog/german/" -> 26.0,
    "archive.ics.uci.edu/ml/datasets/Online+News+Popularity" -> 25.0,
    "archive.ics.uci.edu/ml/machine-learning-databases/iris/" -> 24.0,
    "archive.ics.uci.edu/ml/datasets/Hepatitis" -> 23.0,
    "archive.ics.uci.edu/ml/machine-learning-databases/breast-cancer-wisconsin/" -> 22.0,
    "archive.ics.uci.edu/ml/datasets/Blood+Transfusion+Service+Center" -> 21.0,
    "archive.ics.uci.edu/ml/datasets/Mushroom" -> 20.0,
    "archive.ics.uci.edu/ml/datasets/Vertebral+Column" -> 19.0,
    "archive.ics.uci.edu/ml/datasets/Abalone" -> 18.0,
    "www.ics.uci.edu/~qliu1/MLcrowd_ICML_workshop/" -> 17.0,
    "archive.ics.uci.edu/ml/datasets/seeds" -> 16.0,
    "archive.ics.uci.edu/ml/datasets/Automobile" -> 15.0,
    "archive.ics.uci.edu/ml/datasets.html?sort=nameUp&view=list" -> 14.0,
    "archive.ics.uci.edu/ml/machine-learning-databases/forest-fires/" -> 13.0,
    "archive.ics.uci.edu/ml/machine-learning-databases/00222/" -> 12.0,
    "archive.ics.uci.edu/ml/datasets/Adult" -> 11.0,
    "archive.ics.uci.edu/ml/datasets/ICU" -> 10.0,
    "archive.ics.uci.edu/ml/datasets/Wine+Quality" -> 9.0,
    "archive.ics.uci.edu/ml/datasets/Mice+Protein+Expression" -> 8.0,
    "archive.ics.uci.edu/ml/datasets/Bike+Sharing+Dataset" -> 7.0,
    "archive.ics.uci.edu/ml/datasets/Covertype" -> 6.0,
    "archive.ics.uci.edu/ml/datasets/Congressional+Voting+Records" -> 5.0,
    "archive.ics.uci.edu/ml/datasets/Forest+Fires" -> 4.0,
    "mlearn.ics.uci.edu/" -> 3.0,
    "archive.ics.uci.edu/ml/datasets/Statlog+(Shuttle)" -> 2.0,
    "archive.ics.uci.edu/ml/datasets/Human+Activity+Recognition+Using+Smartphones" -> 1.0
  )

  val softwareEngineering = Map(
    "www.ics.uci.edu/prospective/en/degrees/software-engineering/" -> 40.0,
    "www.ics.uci.edu/ugrad/degrees/degree_se.php" -> 39.0,
    "www.ics.uci.edu/~ziv/ooad/intro_to_se/tsld008.htm" -> 38.0,
    "www.ics.uci.edu/~icgse2016/2_0cfp.html" -> 37.0,
    "se4s.ics.uci.edu/" -> 36.0,
    "www.ics.uci.edu/~icgse2016/2_1conference.html" -> 35.0,
    "www.ics.uci.edu/faculty/area/area_software.php" -> 34.0,
    "www.ics.uci.edu/~emilyo/SimSE/se_rules.html" -> 33.0,
    "www.ics.uci.edu/~emilyo/SimSE/" -> 32.0,
    "www.ics.uci.edu/~djr/DebraJRichardson/SE4S.html" -> 31.0,
    "www.ics.uci.edu/prospective/en/degrees/computer-science-engineering/" -> 30.0,
    "www.ics.uci.edu/~jajones/Informatics211-Fall2015.html" -> 29.0,
    "www.ics.uci.edu/prospective/en/degrees/informatics/" -> 28.0,
    "www.ics.uci.edu/~ziv/ooad/intro_to_se/" -> 27.0,
    "www.ics.uci.edu/~icgse2016/6_0committees.html" -> 26.0,
    "www.ics.uci.edu/~emilyo/SimSE/publications.html" -> 25.0,
    "www.ics.uci.edu/~emilyo/SimSE/links.html" -> 24.0,
    "www.ics.uci.edu/ugrad/degrees/second_baccs.php" -> 23.0,
    "www.ics.uci.edu/~wscacchi/Pubs-Process.html" -> 22.0,
    "www.ics.uci.edu/~emilyo/teaching/info43s2015/" -> 21.0,
    "www.ics.uci.edu/~icgse2016/2_2workshop%20proposals.html" -> 20.0,
    "www.ics.uci.edu/~ziv/ooad/intro_to_se/tsld022.htm" -> 19.0,
    "www.ics.uci.edu/~michele/ICS52/Assignments/samplemid1.doc" -> 18.0,
    "www.ics.uci.edu/~icgse2016/2_3tutorial%20proposals.html" -> 17.0,
    "www.ics.uci.edu/grad/admissions/Prospective_ApplicationProcess.php" -> 16.0,
    "www.ics.uci.edu/~andre/ics52w2012.html" -> 15.0,
    "www.ics.uci.edu/~emilyo/SimSE/details.html" -> 14.0,
    "www.ics.uci.edu/~vpalepu/" -> 13.0,
    "www.ics.uci.edu/~andre/informatics291sf2015.html" -> 12.0,
    "www.ics.uci.edu/~andre/informatics43f2012.html" -> 11.0,
    "www.ics.uci.edu/~michele/ICS52/Assignments/Samplemid2Sol.doc" -> 10.0,
    "www.ics.uci.edu/~icgse2016/4_01tutorial.html" -> 9.0,
    "www.ics.uci.edu/~emilyo/publications.html" -> 8.0,
    "www.ics.uci.edu/~michele/ICS52/Assignments/SampleFinalAnswers.doc" -> 7.0,
    "www.ics.uci.edu/~hsajnani/" -> 6.0,
    "www.ics.uci.edu/~taylor/ICS_52_FQ02/ics52_desAssignment_1.doc" -> 5.0,
    "frost.ics.uci.edu/inf43/" -> 4.0,
    "www.ics.uci.edu/prospective/en/degrees/computer-game-science/" -> 3.0,
    "www.ics.uci.edu/~ziv/diss/intropaper/node2.html" -> 2.0,
    "www.ics.uci.edu/~ziv/ooad/intro_to_se/sld008.htm" -> 1.0
  )

  val security = Map(
    "ccsw.ics.uci.edu/15/" -> 40.0,
    "www.ics.uci.edu/computing/linux/file-security.php" -> 39.0,
    "www.ics.uci.edu/faculty/area/area_security.php" -> 38.0,
    "sconce.ics.uci.edu/" -> 37.0,
    "sprout.ics.uci.edu/past_projects/odb/" -> 36.0,
    "www.ics.uci.edu/~keldefra/manet.htm" -> 35.0,
    "ccsw.ics.uci.edu/15/speakers.html" -> 34.0,
    "www.ics.uci.edu/~goodrich/teach/ics8/" -> 33.0,
    "www.ics.uci.edu/~stasio/spring04/ics180.html" -> 32.0,
    "www.ics.uci.edu/~ics54/00w/doc/security/" -> 31.0,
    "sconce.ics.uci.edu/203-W15/" -> 30.0,
    "www.ics.uci.edu/~gts/" -> 29.0,
    "www.ics.uci.edu/computing/linux/security.php" -> 28.0,
    "www.ics.uci.edu/community/news/spotlight/spotlight_tsudik_a_whole_new_class.php" -> 27.0,
    "www.ics.uci.edu/~goodrich/teach/ics247/" -> 26.0,
    "sprout.ics.uci.edu/" -> 25.0,
    "sprout.ics.uci.edu/projects/uwsnwebpage/security_uwsn.html" -> 24.0,
    "www.ics.uci.edu/~sjcrane/" -> 23.0,
    "www.ics.uci.edu/~projects/privacygroup/rcccs/readings/secure-mashups" -> 22.0,
    "sprout.ics.uci.edu/past_projects/gac/" -> 21.0,
    "www.ics.uci.edu/~wscacchi/Tech-EC/Security+Privacy/The%20Open%20Web%20Application%20Security%20Project.html" -> 20.0,
    "www.ics.uci.edu/~kobsa/talks/privsecum/ppframe.htm" -> 19.0,
    "www.ics.uci.edu/~ics54/doc/security/pkhistory.html" -> 18.0,
    "sprout.ics.uci.edu/projects/dtn/" -> 17.0,
    "www.ics.uci.edu/~dan/class/267/rfc2301/chapter10.html" -> 16.0,
    "sprout.ics.uci.edu/projects/usec/usec.html" -> 15.0,
    "sprout.ics.uci.edu/projects/privacy-dna/" -> 14.0,
    "www.ics.uci.edu/~theory/workshop/" -> 13.0,
    "www.ics.uci.edu/~goodrich/teach/ics280/" -> 12.0,
    "www.ics.uci.edu/~stasio/fall04/ics268.html" -> 11.0,
    "www.ics.uci.edu/~projects/DataGuard/javadoc/org/itr_rescue/dataGuard/encryption/Security.html" -> 10.0,
    "asterix-gerrit.ics.uci.edu/Documentation/config-sso.html" -> 9.0,
    "www.ics.uci.edu/~goodrich/pubs/" -> 8.0,
    "www.ics.uci.edu/~goodrich/" -> 7.0,
    "www.ics.uci.edu/~wscacchi/Tech-EC/Security+Privacy/" -> 6.0,
    "www.ics.uci.edu/~goodrich/teach/ics8/notes/" -> 5.0,
    "www.ics.uci.edu/~goodrich/teach/ics247/hw/" -> 4.0,
    "www.ics.uci.edu/~stasio/spring04/outline180.html" -> 3.0,
    "www.ics.uci.edu/~theory/workshop/dimitris.html" -> 2.0,
    "www.ics.uci.edu/~theory/workshop/azer.html" -> 1.0
  )

  val studentAffairs = Map(
    "www.ics.uci.edu/prospective/en/contact/student-affairs/" -> 40.0,
    "www.ics.uci.edu/ugrad/" -> 39.0,
    "www.ics.uci.edu/about/search/search_sao.php" -> 38.0,
    "www.ics.uci.edu/grad/sao/" -> 37.0,
    "www.ics.uci.edu/grad/" -> 36.0,
    "www.ics.uci.edu/about/visit/" -> 35.0,
    "www.ics.uci.edu/about/about_contact.php" -> 34.0,
    "www.ics.uci.edu/ugrad/sao/Appts_Walk-Ins.php" -> 33.0,
    "www.ics.uci.edu/about/annualreport/2006-07/sao.php" -> 32.0,
    "www.ics.uci.edu/about/annualreport/2005-06/sao.php" -> 31.0,
    "www.ics.uci.edu/ugrad/qa/" -> 30.0,
    "www.ics.uci.edu/ugrad/QA_Petitions" -> 29.0,
    "www.ics.uci.edu/ugrad/policies/OLD_index.php" -> 28.0,
    "www.ics.uci.edu/about/search/search_dean.php" -> 27.0,
    "www.ics.uci.edu/ugrad/policies/Academic_Standing" -> 26.0,
    "www.ics.uci.edu/ugrad/policies/Withdrawal_Readmission" -> 25.0,
    "www.ics.uci.edu/~welling/teaching/ICS171fall08/ICS171fall08.html" -> 24.0,
    "www.ics.uci.edu/grad/policies/GradPolicies_GradStudentReview.php" -> 23.0,
    "www.ics.uci.edu/grad/policies/GradPolicies_TransferCredit.php" -> 22.0,
    "www.ics.uci.edu/~peer/Site/About_Student_Affairs_Office.html" -> 21.0,
    "www.ics.uci.edu/ugrad/degrees/degree_ics.php" -> 20.0,
    "www.ics.uci.edu/grad/policies/GradPolicies_CopyrightInfringement.php" -> 19.0,
    "www.ics.uci.edu/ugrad/degrees/degree_in4matx.php" -> 18.0,
    "www.ics.uci.edu/ugrad/degrees/degree_cgs.php" -> 17.0,
    "www.ics.uci.edu/grad/policies/GradPolicies_PreviousMSdegree.php" -> 16.0,
    "www.ics.uci.edu/ugrad/sao/" -> 15.0,
    "www.ics.uci.edu/grad/policies/GradPolicies_ComprehensiveExam.php" -> 14.0,
    "www.ics.uci.edu/grad/policies/GradPolicies_Defense.php" -> 13.0,
    "www.ics.uci.edu/ugrad/degrees/degree_bim.php" -> 12.0,
    "www.ics.uci.edu/ugrad/resources/specialprograms.php" -> 11.0,
    "www.ics.uci.edu/ugrad/degrees/second_baccs.php" -> 10.0,
    "www.ics.uci.edu/ugrad/degrees/degree_cse.php" -> 9.0,
    "www.ics.uci.edu/prospective/en/degrees/information-computer-science" -> 8.0,
    "www.ics.uci.edu/ugrad/degrees/degree_dis.php" -> 7.0,
    "www.ics.uci.edu/ugrad/degrees/degree_se.php" -> 6.0,
    "www.ics.uci.edu/community/news/features/view_feature?id=66" -> 5.0,
    "www.ics.uci.edu/~pattis/ICS-33/handouts/academicintegrity.doc" -> 4.0,
    "www.ics.uci.edu/ugrad/degrees/degree_bmc.php" -> 3.0,
    "www.ics.uci.edu/~kay/checker.html" -> 2.0,
    "www.ics.uci.edu/ugrad/DELETEadmissions/transfer/" -> 1.0
  )

  val graduateCourses = Map(
    "www.ics.uci.edu/grad/courses/" -> 40.0,
    "www.ics.uci.edu/grad/degrees/degree_cs.php" -> 39.0,
    "www.ics.uci.edu/grad/policies/GradPolicies_Grading.php" -> 38.0,
    "www.ics.uci.edu/grad/policies/GradPolicies_TransferCredit.php" -> 37.0,
    "www.ics.uci.edu/grad/degrees/" -> 36.0,
    "www.ics.uci.edu/grad/" -> 35.0,
    "www.ics.uci.edu/grad/degrees/degree_inf-sw.php" -> 34.0,
    "www.ics.uci.edu/ugrad/courses/" -> 33.0,
    "www.ics.uci.edu/ugrad/degrees/degree_cs.php" -> 32.0,
    "www.ics.uci.edu/grad/admissions/" -> 31.0,
    "www.ics.uci.edu/prospective/en/degrees/software-engineering/" -> 30.0,
    "www.ics.uci.edu/ugrad/QA_Graduation" -> 29.0,
    "www.ics.uci.edu/ugrad/qa/" -> 28.0,
    "www.ics.uci.edu/grad/qa/" -> 27.0,
    "www.ics.uci.edu/~welling/teaching/courses.html" -> 26.0,
    "www.ics.uci.edu/ugrad/degrees/degree_in4matx.php" -> 25.0,
    "www.ics.uci.edu/ugrad/policies/Add_Drop_ChangeOption" -> 24.0,
    "www.ics.uci.edu/ugrad/policies/Course_Outside_UCI" -> 23.0,
    "isg.ics.uci.edu/courses.html" -> 22.0,
    "www.ics.uci.edu/ugrad/degrees/degree_cgs.php" -> 21.0,
    "www.ics.uci.edu/ugrad/policies/Grade_Options" -> 20.0,
    "www.ics.uci.edu/ugrad/QA_Petitions" -> 19.0,
    "www.ics.uci.edu/grad/degrees/degree_cs_2011.php" -> 18.0,
    "www.ics.uci.edu/ugrad/degrees/degree_se.php" -> 17.0,
    "www.ics.uci.edu/" -> 16.0,
    "www.ics.uci.edu/ugrad/degrees/degree_cs_project.php" -> 15.0,
    "www.ics.uci.edu/ugrad/honors/honors_program.php" -> 14.0,
    "www.ics.uci.edu/ugrad/degrees/degree_stats.php" -> 13.0,
    "www.ics.uci.edu/grad/policies/" -> 12.0,
    "www.ics.uci.edu/ugrad/sao/" -> 11.0,
    "www.ics.uci.edu/prospective/en/degrees/computer-game-science/" -> 10.0,
    "www.ics.uci.edu/~eppstein/261/" -> 9.0,
    "www.ics.uci.edu/~eppstein/163/" -> 8.0,
    "www.ics.uci.edu/ugrad/degrees/degree_cse_electives.php" -> 7.0,
    "www.ics.uci.edu/prospective/en/degrees/computer-science/" -> 6.0,
    "www.ics.uci.edu/grad/policies/GradPolicies_MSThesisOption.php" -> 5.0,
    "www.ics.uci.edu/ugrad/degrees/degree_ics_project.php" -> 4.0,
    "www.ics.uci.edu/grad/degrees/degree_kdd.php" -> 3.0,
    "www.ics.uci.edu/~smyth/courses/cs271/" -> 2.0,
    "www.ics.uci.edu/ugrad/courses/details.php?id=69" -> 1.0
  )

  val cristaLopes = Map(
    "www.ics.uci.edu/~lopes/" -> 40.0,
    "www.ics.uci.edu/faculty/profiles/view_faculty.php?ucinetid=lopes" -> 39.0,
    "www.ics.uci.edu/~lopes/publications.html" -> 38.0,
    "www.ics.uci.edu/~lopes/patents.html" -> 37.0,
    "mondego.ics.uci.edu/" -> 36.0,
    "sourcerer.ics.uci.edu/publications.html" -> 35.0,
    "www.ics.uci.edu/~hsajnani/" -> 34.0,
    "www.ics.uci.edu/~lopes/aop/aop-pics.html" -> 33.0,
    "luci.ics.uci.edu/blog/?tag=crista-lopes" -> 32.0,
    "www.ics.uci.edu/~lopes/dv/dv.html" -> 31.0,
    "luci.ics.uci.edu/blog/?p=416" -> 30.0,
    "www.ics.uci.edu/~lopes/teaching/cs221W12/" -> 29.0,
    "luci.ics.uci.edu/lightweight/bioFaculty/lopes/" -> 28.0,
    "www.ics.uci.edu/~lopes/opensim/HypergridReferenceGuide.html" -> 27.0,
    "www.ics.uci.edu/community/news/features/view_feature?id=89" -> 26.0,
    "www.ics.uci.edu/community/news/features/view_feature?id=67" -> 25.0,
    "www.ics.uci.edu/~tdebeauv/" -> 24.0,
    "www.ics.uci.edu/~lopes/datasets/Koders-log-2007.html" -> 23.0,
    "luci.ics.uci.edu/blog/?p=98" -> 22.0,
    "www.ics.uci.edu/community/news/articles/view_article?id=95" -> 21.0,
    "www.ics.uci.edu/~lopes/teaching/inf212W12/" -> 20.0,
    "www.ics.uci.edu/community/news/articles/?select_year=2007" -> 19.0,
    "luci.ics.uci.edu/blog/?p=180" -> 18.0,
    "www.ics.uci.edu/community/news/articles/view_article?id=154" -> 17.0,
    "www.ics.uci.edu/community/events/openhouse/gallery_02.html" -> 16.0,
    "www.ics.uci.edu/community/events/openhouse/building.html" -> 15.0,
    "www.ics.uci.edu/community/events/openhouse/secondlife.html" -> 14.0,
    "luci.ics.uci.edu/blog/?p=592" -> 13.0,
    "luci.ics.uci.edu/websiteContent/weAreLuci/biographies/faculty.xml" -> 12.0,
    "www.ics.uci.edu/openhouse" -> 11.0,
    "www.ics.uci.edu/community/news/features/?select_year=2014" -> 10.0,
    "www.ics.uci.edu/community/news/articles/view_article?id=85" -> 9.0,
    "www.ics.uci.edu/community/news/articles/?select_year=2008" -> 8.0,
    "www.ics.uci.edu/community/news/articles/?select_year=2014" -> 7.0,
    "www.ics.uci.edu/~rsilvafi/" -> 6.0,
    "www.ics.uci.edu/~lopes/teaching/inf295S10/INF%20295.docx" -> 5.0,
    "luci.ics.uci.edu/blog/?tag=software-engineering" -> 4.0,
    "www.ics.uci.edu/community/news/articles/?select_year=2015" -> 3.0,
    "sourcerer.ics.uci.edu/people.html" -> 2.0,
    "luci.ics.uci.edu/blog/?tag=ieee" -> 1.0
  )

  val rest = Map(
    "www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm" -> 40.0,
    "www.ics.uci.edu/~fielding/pubs/dissertation/top.htm" -> 39.0,
    "www.ics.uci.edu/~fielding/pubs/dissertation/introduction.htm" -> 38.0,
    "www.ics.uci.edu/~fielding/talks/webarch_9805/" -> 37.0,
    "www.ics.uci.edu/~fielding/pubs/dissertation/evaluation.htm" -> 36.0,
    "www.ics.uci.edu/~fielding/pubs/dissertation/abstract.htm" -> 35.0,
    "www.ics.uci.edu/~fielding/pubs/dissertation/conclusions.htm" -> 34.0,
    "asterixdb.ics.uci.edu/documentation/api.html" -> 33.0,
    "asterix-gerrit.ics.uci.edu/Documentation/rest-api.html" -> 32.0,
    "www.ics.uci.edu/~fielding/" -> 31.0,
    "asterix-gerrit.ics.uci.edu/Documentation/rest-api-plugins.html" -> 30.0,
    "asterix-gerrit.ics.uci.edu/Documentation/dev-rest-api.html" -> 29.0,
    "www.ics.uci.edu/~fielding/pubs/dissertation/net_arch_styles.htm" -> 28.0,
    "asterix-gerrit.ics.uci.edu/Documentation/rest-api-projects.html" -> 27.0,
    "archive.ics.uci.edu/ml/datasets/SPECTF+Heart" -> 26.0,
    "asterix-gerrit.ics.uci.edu/Documentation/rest-api-access.html" -> 25.0,
    "asterix-gerrit.ics.uci.edu/Documentation/rest-api-config.html" -> 24.0,
    "www.ics.uci.edu/~rohit/" -> 23.0,
    "www.ics.uci.edu/~kay/courses/141/schemenotes.html" -> 22.0,
    "www.ics.uci.edu/~fielding/pubs/dissertation/software_arch.htm" -> 21.0,
    "asterixdb.ics.uci.edu/documentation/" -> 20.0,
    "www.ics.uci.edu/~eppstein/pix/sunsets/ontheroad-bra.html" -> 19.0,
    "asterix-gerrit.ics.uci.edu/Documentation/js-api.html" -> 18.0,
    "www.ics.uci.edu/~feldman/LEC08.htm" -> 17.0,
    "www.ics.uci.edu/community/news/press/view_press?id=30" -> 16.0,
    "www.ics.uci.edu/~eppstein/pix/thorn/" -> 15.0,
    "www.ics.uci.edu/~eppstein/161/kmp/lookup.c" -> 14.0,
    "www.ics.uci.edu/~eppstein/PADS/IntegerPartitions.py" -> 13.0,
    "www.ics.uci.edu/~kay/courses/141/hw/hw2.html" -> 12.0,
    "archive.ics.uci.edu/ml/datasets/Arrhythmia" -> 11.0,
    "www.ics.uci.edu/~ishklovs/therest.html" -> 10.0,
    "scratch.proteomics.ics.uci.edu/explanation.html" -> 9.0,
    "www.ics.uci.edu/~taylor/" -> 8.0,
    "www.ics.uci.edu/~darrellb/induetime.html" -> 7.0,
    "www.ics.uci.edu/~eppstein/pix/wads15/" -> 6.0,
    "www.ics.uci.edu/~eppstein/pix/brenglass/" -> 5.0,
    "www.ics.uci.edu/~dock/mkayala/bioblog/aucCalculation/aucCalc.Rnw" -> 4.0,
    "www.ics.uci.edu/~kay/courses/31/design-recipe.html" -> 3.0,
    "www.ics.uci.edu/~rohit/Acknowledgments.htm" -> 2.0
  )

  val computerGames = Map(
    "cgvw.ics.uci.edu/" -> 40.0,
    "www.ics.uci.edu/prospective/en/degrees/computer-game-science/" -> 39.0,
    "www.ics.uci.edu/ugrad/degrees/degree_cgs.php" -> 38.0,
    "cgvw.ics.uci.edu/affiliated-faculty/" -> 37.0,
    "cgvw.ics.uci.edu/tag/computer-games/" -> 36.0,
    "www.ics.uci.edu/~ebaumer/us12/" -> 35.0,
    "cgvw.ics.uci.edu/author/venita/" -> 34.0,
    "cgvw.ics.uci.edu/author/admin/" -> 33.0,
    "www.ics.uci.edu/community/events/gameday/" -> 32.0,
    "cgvw.ics.uci.edu/news-events/" -> 31.0,
    "cgvw.ics.uci.edu/author/lopes/" -> 30.0,
    "cgvw.ics.uci.edu/news-events/page/2/" -> 29.0,
    "cgvw.ics.uci.edu/category/uncategorized/" -> 28.0,
    "cgvw.ics.uci.edu/tag/virtual-worlds/" -> 27.0,
    "www.ics.uci.edu/~wscacchi/" -> 26.0,
    "www.ics.uci.edu/community/news/press/view_press?id=135" -> 25.0,
    "cgvw.ics.uci.edu/san-francisco-moma-video-games-as-art-and-interaction-design/" -> 24.0,
    "cgvw.ics.uci.edu/project-showcase/" -> 23.0,
    "cgvw.ics.uci.edu/news-events/page/3/" -> 22.0,
    "cgvw.ics.uci.edu/tag/law/" -> 21.0,
    "cgvw.ics.uci.edu/uci-students-build-games-in-a-week/" -> 20.0,
    "cgvw.ics.uci.edu/tag/emily-navarro/" -> 19.0,
    "cgvw.ics.uci.edu/seminar-series/" -> 18.0,
    "www.ics.uci.edu/~eppstein/gina/vidgames.html" -> 17.0,
    "www.ics.uci.edu/community/news/articles/view_article?id=161" -> 16.0,
    "www.ics.uci.edu/community/news/features/view_feature?id=82" -> 15.0,
    "cgvw.ics.uci.edu/seminar-series/2011-12-seminar-series/" -> 14.0,
    "cgvw.ics.uci.edu/seminar-talk-2013-01-16/" -> 13.0,
    "cgvw.ics.uci.edu/seminar-series/distinguished-lecture-series/" -> 12.0,
    "www.ics.uci.edu/community/news/spotlight/spotlight_gamejam_winter_2012.php" -> 11.0,
    "frost.ics.uci.edu/cs113/" -> 10.0,
    "cgvw.ics.uci.edu/ethnography-and-virtual-worlds/" -> 9.0,
    "cgvw.ics.uci.edu/2012/10/" -> 8.0,
    "cgvw.ics.uci.edu/oc-video-game-companies-raise-millions-on-kickstarter/" -> 7.0,
    "cgvw.ics.uci.edu/2009/09/" -> 6.0,
    "frost.ics.uci.edu/" -> 5.0,
    "cgvw.ics.uci.edu/2011/02/" -> 4.0,
    "cgvw.ics.uci.edu/2011/01/" -> 3.0,
    "cgvw.ics.uci.edu/seminar-talk-2013-02-27/" -> 2.0,
    "cgvw.ics.uci.edu/2009/11/" -> 1.0
  )

  val informationRetrieval = Map(
    "www.ics.uci.edu/~lopes/teaching/cs221W12/" -> 40.0,
    "www.ics.uci.edu/~lopes/" -> 39.0,
    "www.ics.uci.edu/~lopes/teaching/cs221W13/" -> 38.0,
    "www.ics.uci.edu/~djp3/classes/2014_01_INF141/calendar.html" -> 37.0,
    "www.ics.uci.edu/~djp3/classes/2014_01_INF141/structure.html" -> 36.0,
    "www.ics.uci.edu/~djp3/classes/2010_01_CS221/" -> 35.0,
    "www.ics.uci.edu/~kay/courses/i141/refs.html" -> 34.0,
    "www-db.ics.uci.edu/pages/research/mars/" -> 33.0,
    "www.ics.uci.edu/~djp3/classes/2014_01_INF141/tasks/task_22/project.html" -> 32.0,
    "www.ics.uci.edu/~djp3/classes/2014_01_INF141/tasks/task_22.html" -> 31.0,
    "www.ics.uci.edu/~djp3/classes/2014_01_INF141/tasks/task_12.html" -> 30.0,
    "www.ics.uci.edu/~kay/courses/i141/w15.html" -> 29.0,
    "www.ics.uci.edu/~djp3/classes/2014_01_INF141/tasks/task_22/openlab.html" -> 28.0,
    "archive.ics.uci.edu/ml/datasets/Reuters-21578+Text+Categorization+Collection" -> 27.0,
    "www.ics.uci.edu/~djp3/classes/2009_01_02_INF141/calendar.html" -> 26.0,
    "www.ics.uci.edu/~djp3/classes/2014_01_INF141/admin.html" -> 25.0,
    "www.ics.uci.edu/~kay/courses/i141/hw/asst1.html" -> 24.0,
    "www.ics.uci.edu/~djp3/classes/2009_01_02_INF141/" -> 23.0,
    "www.ics.uci.edu/ugrad/courses/details.php?id=287" -> 22.0,
    "www.ics.uci.edu/~djp3/classes/2014_01_INF141/tasks/task_34.html" -> 21.0,
    "fano.ics.uci.edu/cites/Document/Incremental-clustering-and-dynamic-information-retrieval.html" -> 20.0,
    "www.ics.uci.edu/~djp3/classes/2014_01_INF141/tasks/task_29.html" -> 19.0,
    "www.ics.uci.edu/~djp3/classes/2014_01_INF141/tasks/task_27.html" -> 18.0,
    "www-db.ics.uci.edu/pages/research/mars/nsf-report2000.doc" -> 17.0,
    "www.ics.uci.edu/~djp3/classes/2009_01_02_INF141/Assignments/Assignment07.html" -> 16.0,
    "www.ics.uci.edu/~djp3/classes/2009_01_02_INF141/Assignments/Assignment02.html" -> 15.0,
    "www.ics.uci.edu/~djp3/classes/2008_01_01_INF141/materials.html" -> 14.0,
    "www.ics.uci.edu/~djp3/classes/2008_01_01_INF141/" -> 13.0,
    "www.ics.uci.edu/~djp3/classes/2008_09_26_CS221/materials.html" -> 12.0,
    "www.ics.uci.edu/~djp3/classes/2009_01_02_INF141/structure.html" -> 11.0,
    "www.ics.uci.edu/~djp3/classes/2008_09_26_CS221/calendar.html" -> 10.0,
    "www.ics.uci.edu/~kay/courses/i141/hw/asst2.html" -> 9.0,
    "www.ics.uci.edu/~newman/courses/cs277/backgroundreading.html" -> 8.0,
    "www.ics.uci.edu/~djp3/classes/2009_01_02_INF141/Assignments/Assignment05.html" -> 7.0,
    "luci.ics.uci.edu/blog/?tag=information-retrieval" -> 6.0,
    "www.ics.uci.edu/~djp3/classes/2009_01_02_INF141/Assignments/Assignment03.html" -> 5.0,
    "www.ics.uci.edu/ugrad/degrees/degree_cs.php" -> 4.0,
    "vision.ics.uci.edu/papers/GehlerHW_ICML_2006/" -> 3.0,
    "www.ics.uci.edu/~kay/courses/i141/hw/asst5.html" -> 2.0,
    "www.ics.uci.edu/~djp3/classes/2007_04_02_CS221/assignmentSchedule.html" -> 1.0
  )

  val fixtures = Map(
    "mondego" -> mondego,
    "machine learning" -> machineLearning,
    "software engineering" -> softwareEngineering,
    "security" -> security,
    "student affairs" -> studentAffairs,
    "graduate courses" -> graduateCourses,
    "Crista Lopes" -> cristaLopes,
    "REST" -> rest,
    "computer games" -> computerGames,
    "information retrieval" -> informationRetrieval
  )

}

