-- MySQL dump 10.13  Distrib 5.7.9, for Win64 (x86_64)
--
-- Host: localhost    Database: employmeo
-- ------------------------------------------------------
-- Server version	5.7.10-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `accounts`
--

DROP TABLE IF EXISTS `accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `accounts` (
  `ACCOUNT_ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ACCOUNT_NAME` varchar(255) NOT NULL DEFAULT '<TBD>',
  `ACCOUNT_STATUS` int(11) NOT NULL DEFAULT '1',
  `ACCOUNT_TYPE` int(11) NOT NULL DEFAULT '1',
  `MODIFIED_DATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ACCOUNT_CURRENCY` varchar(45) DEFAULT NULL,
  `ACCOUNT_TIMEZONE` varchar(45) DEFAULT NULL,
  `ACCOUNT_CREATOR` bigint(20) NOT NULL,
  PRIMARY KEY (`ACCOUNT_ID`),
  KEY `ACCOUNT_NAME` (`ACCOUNT_NAME`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `accounts`
--

LOCK TABLES `accounts` WRITE;
/*!40000 ALTER TABLE `accounts` DISABLE KEYS */;
INSERT INTO `accounts` VALUES (0,'Default',1,1,'2015-12-24 21:16:52','1','1',0),(1,'Employmeo',1,1,'2015-12-21 20:20:05',NULL,NULL,0);
/*!40000 ALTER TABLE `accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `answers`
--

DROP TABLE IF EXISTS `answers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `answers` (
  `ANSWER_ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ANSWER_QUESTION_ID` bigint(20) NOT NULL,
  `ANSWER_DISPLAY_ID` bigint(20) NOT NULL,
  `ANSWER_TEXT` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `ANSWER_DESCRIPTION` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `ANSWER_VALUE` int(11) NOT NULL,
  PRIMARY KEY (`ANSWER_ID`),
  KEY `ANSWER_QUESTION_ID` (`ANSWER_QUESTION_ID`),
  CONSTRAINT `FK_ANSWER_QUESTION` FOREIGN KEY (`ANSWER_QUESTION_ID`) REFERENCES `questions` (`QUESTION_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=193 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `answers`
--

LOCK TABLES `answers` WRITE;
/*!40000 ALTER TABLE `answers` DISABLE KEYS */;
INSERT INTO `answers` VALUES (1,1,1,'Safety experts','Safety experts',1),(2,1,2,'Supervisors','Supervisors',2),(3,1,3,'Managers','Managers',3),(4,1,4,'Safety trainers','Safety trainers',4),(5,1,5,'Employees','Employees',5),(6,2,6,'None','None',1),(7,2,7,'Once','Once',2),(8,2,8,'2 - 3 times','2 - 3 times',3),(9,2,9,'4 - 6 times','4 - 6 times',4),(10,2,10,'7 - 10 times','7 - 10 times',5),(11,2,11,'More than 10 times','More than 10 times',6),(12,3,12,'Take a small break','Take a small break',1),(13,3,13,'Talk with a friend','Talk with a friend',2),(14,3,14,'Stand around','Stand around',3),(15,3,15,'Find someone who can use your help','Find someone who can use your help',4),(16,3,16,'Ask a manager what to do','Ask a manager what to do',5),(17,4,17,'Get ready to work extra hard tomorrow','Get ready to work extra hard tomorrow',1),(18,4,18,'Call other team members to see if anyone will be available to cover the shift','Call other team members to see if anyone will be available to cover the shift',2),(19,4,19,'Explain to the team member that not coming to work will make it hard for everyone else','Explain to the team member that not coming to work will make it hard for everyone else',3),(20,4,20,'Let your manager know about the situation','Let your manager know about the situation',4),(21,5,21,'I don\'t know','I don\'t know',1),(22,5,22,'No experience','No experience',2),(23,5,23,'Limited experience','Limited experience',3),(24,5,24,'Much experience','Much experience',4),(25,5,25,'A great deal of experience','A great deal of experience',5),(26,6,26,'Always','Always',1),(27,6,27,'Often','Often',2),(28,6,28,'Sometimes','Sometimes',3),(29,6,29,'Rarely','Rarely',4),(30,6,30,'Never','Never',5),(31,7,31,'I usually don\'t consider what other people say','I usually don\'t consider what other people say',1),(32,7,32,'I listen, then give my point of view','I listen, then give my point of view',2),(33,7,33,'I always make the changes people suggest','I always make the changes people suggest',3),(34,7,34,'I will consider what other people say and decide what is best for me','I will consider what other people say and decide what is best for me',4),(35,7,35,'People are never critical of me or my work','People are never critical of me or my work',5),(36,8,36,'None','None',1),(37,8,37,'Once','Once',2),(38,8,38,'Twice','Twice',3),(39,8,39,'Three times','Three times',4),(40,8,40,'Four or more times','Four or more times',5),(41,9,41,'Very important','Very important',1),(42,9,42,'Important','Important',2),(43,9,43,'Somewhat important','Somewhat important',3),(44,9,44,'Unimportant','Unimportant',4),(45,9,45,'Very unimportan','Very unimportan',5),(46,10,46,'None','None',1),(47,10,47,'One','One',2),(48,10,48,'Two','Two',3),(49,10,49,'Three','Three',4),(50,10,50,'Four or more','Four or more',5),(51,11,51,'The instruction/guidance was inadequate','The instruction/guidance was inadequate',1),(52,11,52,'Other team members\' efforts were lacking','Other team members\' efforts were lacking',2),(53,11,53,'I was unprepared','I was unprepared',3),(54,11,54,'I didn\'t have enough time','I didn\'t have enough time',4),(55,11,55,'I misunderstood the objectives','I misunderstood the objectives',5),(56,11,56,'I don\'t know or I have never had this experience','I don\'t know or I have never had this experience',6),(57,12,57,'I completed the task I felt was most important first','I completed the task I felt was most important first',1),(58,12,58,'I completed the easiest task first','I completed the easiest task first',2),(59,12,59,'I didn\'t know what to do, so I did nothing','I didn\'t know what to do, so I did nothing',3),(60,12,60,'I asked for more direction','I asked for more direction',4),(61,12,61,'I reconciled the differences myself, then proceeded','I reconciled the differences myself, then proceeded',5),(62,12,62,'I have never had this experience','I have never had this experience',6),(63,13,63,'Find out who was responsible for making the error','Find out who was responsible for making the error',1),(64,13,64,'Apologize for the mistake','Apologize for the mistake',2),(65,13,65,'Have the correct order made as quickly as possible','Have the correct order made as quickly as possible',3),(66,13,66,'Tell the customer you will get a manager for them to speak to','Tell the customer you will get a manager for them to speak to',4),(67,14,67,'Healthy','Healthy',1),(68,14,68,'Destructive','Destructive',2),(69,14,69,'Unavoidable','Unavoidable',3),(70,14,70,'Challenging','Challenging',4),(71,14,71,'I don\'t know','I don\'t know',5),(72,15,72,'Kept them to myself','Kept them to myself',1),(73,15,73,'Asked other employees for their opinions','Asked other employees for their opinions',2),(74,15,74,'Asked a supervisor to help resolve the conflict','Asked a supervisor to help resolve the conflict',3),(75,15,75,'Tried to resolve conflicts with co-workers','Tried to resolve conflicts with co-workers',4),(76,16,76,'None','None',1),(77,16,77,'One','One',2),(78,16,78,'Two','Two',3),(79,16,79,'Three','Three',4),(80,16,80,'Four or more','Four or more',5),(81,17,81,'Tell your supervisor what the guest said','Tell your supervisor what the guest said',1),(82,17,82,'Tell the coworker what the guest said','Tell the coworker what the guest said',2),(83,17,83,'Tell the guest you can handle her business from now on','Tell the guest you can handle her business from now on',3),(84,17,84,'Tell the guest that you can report her comments to the supervisor','Tell the guest that you can report her comments to the supervisor',4),(85,17,85,'Say ok, then continue with your job','Say ok, then continue with your job',5),(86,18,86,'Speak with my manager','Speak with my manager',1),(87,18,87,'Try to remove the team member from the group','Try to remove the team member from the group',2),(88,18,88,'Work harder to compensate','Work harder to compensate',3),(89,18,89,'Try to help bring the team member up to date','Try to help bring the team member up to date',4),(90,18,90,'Talk with the team member to clarify responsibilities','Talk with the team member to clarify responsibilities',5),(91,18,91,'Do nothing','Do nothing',6),(92,19,92,'Confront the supervisor directly about his management style.','Confront the supervisor directly about his management style.',1),(93,19,93,'Discreetly tell a higher-level manager.','Discreetly tell a higher-level manager.',2),(94,19,94,'Try to make the coworker feel better.','Try to make the coworker feel better.',3),(95,19,95,'Tell your supervisor.','Tell your supervisor.',4),(96,19,96,'Nothing, the new coworker will get over it.','Nothing, the new coworker will get over it.',5),(97,20,97,'Quick and friendly service','Quick and friendly service',1),(98,20,98,'Getting good value for their money','Getting good value for their money',2),(99,20,99,'Order is made correctly','Order is made correctly',3),(100,20,100,'Employees who look for ways to make them feel special','Employees who look for ways to make them feel special',4),(101,21,101,'Much more often than others','Much more often than others',1),(102,21,102,'More often than others','More often than others',2),(103,21,103,'About the same as others','About the same as others',3),(104,21,104,'Less often than others','Less often than others',4),(105,21,105,'Much less often than others','Much less often than others',5),(106,22,106,'None','None',1),(107,22,107,'Once','Once',2),(108,22,108,'Twice','Twice',3),(109,22,109,'Three times','Three times',4),(110,22,110,'Four or more times','Four or more times',5),(111,23,111,'Ask the guests that were next in line if they mind the impatient customer going next','Ask the guests that were next in line if they mind the impatient customer going next',1),(112,23,112,'Settle this sale fast to keep the other guests quiet','Settle this sale fast to keep the other guests quiet',2),(113,23,113,'Politely tell the impatient guest to wait their turn','Politely tell the impatient guest to wait their turn',3),(114,23,114,'Pretend to not notice what happened to avoid a scene','Pretend to not notice what happened to avoid a scene',4),(115,23,115,'I don\'t know','I don\'t know',5),(116,24,116,'I make sure I understand the desired results','I make sure I understand the desired results',1),(117,24,117,'I outline a schedule','I outline a schedule',2),(118,24,118,'I find the materials or people I need to complete the project','I find the materials or people I need to complete the project',3),(119,24,119,'I research and investigate','I research and investigate',4),(120,24,120,'I get started and see how it goes','I get started and see how it goes',5),(121,25,121,'To seek a lot of help','To seek a lot of help',1),(122,25,122,'Work day and night until it is finished','Work day and night until it is finished',2),(123,25,123,'Refuse the assignment','Refuse the assignment',3),(124,25,124,'Renegotiate the deadline','Renegotiate the deadline',4),(125,26,125,'Try to remember it all and do the best you can','Try to remember it all and do the best you can',1),(126,26,126,'Ask an experienced coworker for help','Ask an experienced coworker for help',2),(127,26,127,'Ask the manager to repeat what she said','Ask the manager to repeat what she said',3),(128,26,128,'Ask the manager to stop while you get something to take notes','Ask the manager to stop while you get something to take notes',4),(129,26,129,'Ask the manager to please give you a list','Ask the manager to please give you a list',5),(130,27,130,'Tell the employees not to complain about another team member','Tell the employees not to complain about another team member',1),(131,27,131,'Tell the team member that others are talking about him and he should work faster','Tell the team member that others are talking about him and he should work faster',2),(132,27,132,'Suggest that the employees talk about their concerns with your manager','Suggest that the employees talk about their concerns with your manager',3),(133,27,133,'Keep working on my own tasks and say nothing to the employees','Keep working on my own tasks and say nothing to the employees',4),(134,27,134,'Suggest that the employees directly talk with the employee who works too slowly','Suggest that the employees directly talk with the employee who works too slowly',5),(135,28,135,'Report it to the supervisor','Report it to the supervisor',1),(136,28,136,'Work harder to take up the slack','Work harder to take up the slack',2),(137,28,137,'Talk to the co-worker to see how I could help','Talk to the co-worker to see how I could help',3),(138,28,138,'Do nothing','Do nothing',4),(139,29,139,'Very often','Very often',1),(140,29,140,'Often','Often',2),(141,29,141,'Sometimes','Sometimes',3),(142,29,142,'Rarely','Rarely',4),(143,29,143,'Never','Never',5),(144,30,144,'Strongly Agree','Strongly Agree',1),(145,30,145,'Agree','Agree',2),(146,30,146,'Neither Agree nor Disagree','Neither Agree nor Disagree',3),(147,30,147,'Disagree','Disagree',4),(148,30,148,'Strongly Disagree','Strongly Disagree',5),(149,31,149,'To own and run my own restaurant','To own and run my own restaurant',1),(150,31,150,'To own multiple restaurants or businesses','To own multiple restaurants or businesses',2),(151,31,151,'To make enough money to retire early','To make enough money to retire early',3),(152,31,152,'None of these describes my goals','None of these describes my goals',4),(153,32,153,'Never','Never',1),(154,32,154,'Once or twice','Once or twice',2),(155,32,155,'Up to five times','Up to five times',3),(156,32,156,'About once a month','About once a month',4),(157,32,157,'More than once a month','More than once a month',5),(158,33,158,'Less than 3 months','Less than 3 months',1),(159,33,159,'Between 3 months and 1 year','Between 3 months and 1 year',2),(160,33,160,'Between 1 and 2 years','Between 1 and 2 years',3),(161,33,161,'Between 2 and 3 years','Between 2 and 3 years',4),(162,33,162,'More than 3 years','More than 3 years',5),(163,34,163,'Give them my full and undivided attention','Give them my full and undivided attention',1),(164,34,164,'Make eye contact and smile','Make eye contact and smile',2),(165,34,165,'Make conversation with them','Make conversation with them',3),(166,34,166,'Listen to their needs before offering suggestions','Listen to their needs before offering suggestions',4),(167,34,167,'Answer their questions promptly','Answer their questions promptly',5),(168,35,168,'Much easier than others','Much easier than others',1),(169,35,169,'Easier than others','Easier than others',2),(170,35,170,'About the same as others','About the same as others',3),(171,35,171,'Harder than others','Harder than others',4),(172,35,172,'Much harder than others','Much harder than others',5),(173,36,173,'Fun to work with','Fun to work with',1),(174,36,174,'Knowledgeable','Knowledgeable',2),(175,36,175,'Helpful','Helpful',3),(176,36,176,'Dependable','Dependable',4),(177,36,177,'Something not listed above','Something not listed above',5),(178,37,178,'Having good relationships with my co-workers','Having good relationships with my co-workers',1),(179,37,179,'Praise from my manager','Praise from my manager',2),(180,37,180,'Knowing that I help the company succeed','Knowing that I help the company succeed',3),(181,37,181,'Having good relationships with my guests','Having good relationships with my guests',4),(182,37,182,'Something not listed above','Something not listed above',5),(183,38,183,'Tell the employee that if he doesn\'t stop stealing, he might get caught and lose his job','Tell the employee that if he doesn\'t stop stealing, he might get caught and lose his job',1),(184,38,184,'Tell your manager only if you are asked','Tell your manager only if you are asked',2),(185,38,185,'Tell your manager immediately','Tell your manager immediately',3),(186,38,186,'Try to explain to the employee why he shouldn\'t steal from the company','Try to explain to the employee why he shouldn\'t steal from the company',4),(187,38,187,'Not say anything to the employee or your manager','Not say anything to the employee or your manager',5),(188,39,188,'Being rushed all the time','Being rushed all the time',1),(189,39,189,'Being busy all the time, but not rushed too often','Being busy all the time, but not rushed too often',2),(190,39,190,'Being busy most of the time, but having an occasional lull','Being busy most of the time, but having an occasional lull',3),(191,39,191,'Having a consistent, steady work flow','Having a consistent, steady work flow',4),(192,39,192,'Having quite a bit of spare time','Having quite a bit of spare time',5);
/*!40000 ALTER TABLE `answers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `persons`
--

DROP TABLE IF EXISTS `persons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `persons` (
  `person_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `person_fname` varchar(45) DEFAULT NULL,
  `person_lname` varchar(45) DEFAULT NULL,
  `person_email` varchar(255) DEFAULT NULL,
  `person_street1` varchar(255) DEFAULT NULL,
  `person_street2` varchar(255) DEFAULT NULL,
  `person_city` varchar(45) DEFAULT NULL,
  `person_state` varchar(45) DEFAULT NULL,
  `person_zip` varchar(45) DEFAULT NULL,
  `person_ssn` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`person_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `persons`
--

LOCK TABLES `persons` WRITE;
/*!40000 ALTER TABLE `persons` DISABLE KEYS */;
INSERT INTO `persons` VALUES (0,'Joe','Person','joe@email.com',NULL,NULL,NULL,NULL,NULL,NULL),(2,'Bob','Person','bob@email.com',NULL,NULL,NULL,NULL,NULL,NULL),(3,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(5,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(6,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(7,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(8,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(9,'Sri','Kaza','sridharkaza@gmail.com',NULL,NULL,NULL,NULL,NULL,NULL),(10,'Sri','Kaza','sridharkaza@gmail.com',NULL,NULL,NULL,NULL,NULL,NULL),(11,'Sri','Kaza','sridharkaza@gmail.com',NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `persons` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `positions`
--

DROP TABLE IF EXISTS `positions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `positions` (
  `position_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `position_account` bigint(20) NOT NULL,
  `position_name` varchar(45) DEFAULT NULL,
  `position_target_tenure` decimal(10,0) DEFAULT NULL,
  `position_target_hireratio` decimal(10,0) DEFAULT NULL,
  PRIMARY KEY (`position_id`),
  KEY `position_account_idx` (`position_account`),
  CONSTRAINT `position_account` FOREIGN KEY (`position_account`) REFERENCES `accounts` (`ACCOUNT_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `positions`
--

LOCK TABLES `positions` WRITE;
/*!40000 ALTER TABLE `positions` DISABLE KEYS */;
INSERT INTO `positions` VALUES (0,0,'Clerk',1,4);
/*!40000 ALTER TABLE `positions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `questions`
--

DROP TABLE IF EXISTS `questions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `questions` (
  `QUESTION_ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `QUESTION_TYPE` int(11) NOT NULL,
  `QUESTION_TEXT` varchar(255) CHARACTER SET utf8 NOT NULL,
  `QUESTION_DESCRIPTION` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `QUESTION_DISPLAY_ID` bigint(20) NOT NULL,
  `MODIFIED_DATE` int(11) NOT NULL,
  PRIMARY KEY (`QUESTION_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `questions`
--

LOCK TABLES `questions` WRITE;
/*!40000 ALTER TABLE `questions` DISABLE KEYS */;
INSERT INTO `questions` VALUES (1,2,'I have a high school diploma or equivalent','Completed High School',1,0),(2,2,'I have worked a cash register before','Cash Register',2,0),(3,3,'I am available for these shifts','Shift Availability',3,0),(4,2,'I have worked in a kitchen before','Kitchen',4,0),(5,2,'I can adhere to the dress code policy','Dress Code',5,0),(6,2,'I have authorization to work in the US','US Work Auth',6,0),(7,1,'How do you normally react when someone complains about you or your work?','How do you normally react when someone complains about you or your work?',7,0),(8,1,'In the last year, how many times has your supervisor asked you to take on a difficult project?','In the last year, how many times has your supervisor asked you to take on a difficult project?',8,0),(9,1,'How important is it to you to work in an environment that requires you to learn new things?','How important is it to you to work in an environment that requires you to learn new things?',9,0),(10,1,'In the past year, how many training courses or programs have you taken?','In the past year, how many training courses or programs have you taken?',10,0),(11,1,'What was the main reason projects that you worked on failed?','What was the main reason projects that you worked on failed?',11,0),(12,1,'In the past, how have you handled receiving conflicting instructions from different people at the same time?','In the past, how have you handled receiving conflicting instructions from different people at the same time?',12,0),(13,1,'A guest complains that their order was made incorrectly. What would you do?','A guest complains that their order was made incorrectly. What would you do?',13,0),(14,1,'Do you think competition between co-workers is:','Do you think competition between co-workers is:',14,0),(15,1,'How have you reacted to conflicts between yourself and co-workers?','How have you reacted to conflicts between yourself and co-workers?',15,0),(16,1,'In the past year, how many \'new\' interests or hobbies have you started?','In the past year, how many \'new\' interests or hobbies have you started?',16,0),(17,1,'A guest says the last employee she talked to provided poor service and tells you the employee\'s name. How do you respond?','A guest says the last employee she talked to provided poor service and tells you the employee\'s name. How do you respond?',17,0),(18,1,'You are working on an important team project. One of the team members is clearly not pulling his weight. What action will you take?','You are working on an important team project. One of the team members is clearly not pulling his weight. What action will you take?',18,0),(19,1,'A supervisor, who is not yours, becomes upset with a coworker and severely criticizes the coworker in front of you. The coworker becomes upset and begins to cry. What would you do?','A supervisor, who is not yours, becomes upset with a coworker and severely criticizes the coworker in front of you. The coworker becomes upset and begins to cry. What would you do?',19,0),(20,1,'Which of the following is most important to guests dining at a restaurant?','Which of the following is most important to guests dining at a restaurant?',20,0),(21,1,'Compared to your friends or coworkers, how often do you smile?','Compared to your friends or coworkers, how often do you smile?',21,0),(22,1,'In the past year, how many times have you taken on new responsibilities at work or school?','In the past year, how many times have you taken on new responsibilities at work or school?',22,0),(23,1,'Your job is on the cash register at the check-out counter. An impatient guest jumps ahead of several guests already waiting in line. What would you do?','Your job is on the cash register at the check-out counter. An impatient guest jumps ahead of several guests already waiting in line. What would you do?',23,0),(24,1,'What do you do first when you start a new project?','What do you do first when you start a new project?',24,0),(25,1,'What is your most likely response to an unexpected project with a short deadline?','What is your most likely response to an unexpected project with a short deadline?',25,0),(26,1,'You are new. Your manager is giving you lots of instructions for tasks she wants done. She is speaking so quickly and telling you so many things, you are having trouble remembering everything. What would you do?','You are new. Your manager is giving you lots of instructions for tasks she wants done. She is speaking so quickly and telling you so many things, you are having trouble remembering everything. What would you do?',26,0),(27,1,'You overhear a co-worker talking about another team member because he works too slow. What would you do?','You overhear a co-worker talking about another team member because he works too slow. What would you do?',27,0),(28,1,'What would you do if you knew a co-worker was having a difficult day?','What would you do if you knew a co-worker was having a difficult day?',28,0),(29,1,'In situations where you received assignments without direct supervision or feedback, how often did you seek out instruction or feedback?','In situations where you received assignments without direct supervision or feedback, how often did you seek out instruction or feedback?',29,0),(30,1,'When dealing with guests, it is sometimes necessary to apologize, even when you didn\'t do anything wrong personally.','When dealing with guests, it is sometimes necessary to apologize, even when you didn\'t do anything wrong personally.',30,0),(31,1,'Which of the following would best describe your long-term goals?','Which of the following would best describe your long-term goals?',31,0),(32,1,'Over a year\'s time, how often is it acceptable for a person to be late for work?','Over a year\'s time, how often is it acceptable for a person to be late for work?',32,0),(33,1,'If you are hired, what is your best estimate of how long you will stay with the company?','If you are hired, what is your best estimate of how long you will stay with the company?',33,0),(34,1,'How do you make guests feel important?','How do you make guests feel important?',34,0),(35,1,'Compared to your co-workers and friends, how easily do you understand what others are feeling?','Compared to your co-workers and friends, how easily do you understand what others are feeling?',35,0),(36,1,'How would you like to be described by your co-workers?','How would you like to be described by your co-workers?',36,0),(37,1,'Which of the following is most motivating to you at work?','Which of the following is most motivating to you at work?',37,0),(38,1,'Another employee tells you that he has been taking home a few supplies because he feels his pay is too low. What would you do?','Another employee tells you that he has been taking home a few supplies because he feels his pay is too low. What would you do?',38,0),(39,1,'Which of the following do you prefer during work?','Which of the following do you prefer during work?',39,0);
/*!40000 ALTER TABLE `questions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `respondants`
--

DROP TABLE IF EXISTS `respondants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `respondants` (
  `respondant_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `respondant_person_id` bigint(20) NOT NULL DEFAULT '0',
  `respondant_created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `respondant_survey_id` bigint(20) NOT NULL,
  `respondant_account_id` bigint(20) NOT NULL,
  `respondant_status` int(11) NOT NULL DEFAULT '1',
  PRIMARY KEY (`respondant_id`),
  KEY `FK_RESPONDANT_PERSON_idx` (`respondant_person_id`),
  CONSTRAINT `FK_RESPONDANT_PERSON` FOREIGN KEY (`respondant_person_id`) REFERENCES `persons` (`person_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=105 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `respondants`
--

LOCK TABLES `respondants` WRITE;
/*!40000 ALTER TABLE `respondants` DISABLE KEYS */;
INSERT INTO `respondants` VALUES (1,0,'2015-12-26 17:42:04',2,0,1),(2,0,'2015-12-26 17:44:48',2,0,1),(3,0,'2015-12-26 18:45:01',2,0,1),(4,0,'2015-12-26 18:46:12',2,0,1),(5,0,'2015-12-26 18:55:59',2,0,1),(6,0,'2015-12-26 19:50:13',2,0,1),(7,0,'2015-12-26 19:58:03',2,0,1),(8,0,'2015-12-26 19:58:08',2,0,1),(9,0,'2015-12-26 19:59:09',2,0,1),(10,0,'2015-12-26 19:59:55',2,0,1),(11,0,'2015-12-26 23:02:49',2,0,1),(12,0,'2015-12-26 23:02:51',2,0,1),(13,0,'2015-12-26 23:05:38',2,0,1),(14,0,'2015-12-26 23:06:29',2,0,1),(15,0,'2015-12-26 23:07:12',2,0,1),(16,0,'2015-12-26 23:16:58',2,0,1),(17,0,'2015-12-26 23:17:27',2,0,1),(18,0,'2015-12-26 23:29:33',2,0,1),(19,0,'2015-12-26 23:40:39',2,0,1),(20,0,'2015-12-26 23:48:19',2,0,1),(21,0,'2016-02-16 14:30:22',2,0,1),(22,0,'2016-02-16 14:36:07',2,0,1),(23,0,'2016-02-16 14:37:46',2,0,1),(24,0,'2016-02-16 14:38:53',2,0,1),(25,0,'2016-02-16 14:41:09',2,0,1),(26,0,'2016-02-16 14:45:18',2,0,1),(27,0,'2016-02-16 14:51:56',2,0,1),(28,0,'2016-02-16 14:52:40',2,0,1),(29,0,'2016-02-16 14:53:04',2,0,1),(30,0,'2016-02-16 14:53:16',2,0,1),(31,0,'2016-02-16 14:54:29',2,0,1),(32,0,'2016-02-16 14:56:34',2,0,1),(33,0,'2016-02-16 14:59:40',2,0,1),(34,0,'2016-02-16 15:00:58',2,0,1),(35,0,'2016-02-16 15:04:18',2,0,1),(36,0,'2016-02-16 15:06:00',2,0,1),(37,0,'2016-02-16 15:08:48',2,0,1),(38,0,'2016-02-16 15:11:19',2,0,1),(39,0,'2016-02-16 15:11:26',2,0,1),(40,0,'2016-02-16 15:11:40',2,0,1),(41,0,'2016-02-16 15:12:08',2,0,1),(42,0,'2016-02-16 15:12:20',2,0,1),(43,0,'2016-02-16 15:12:34',2,0,1),(44,0,'2016-02-16 15:12:44',2,0,1),(45,0,'2016-02-16 16:03:26',2,0,1),(46,0,'2016-02-16 16:18:39',2,0,1),(47,0,'2016-02-16 16:29:51',2,0,1),(48,0,'2016-02-16 16:33:40',2,0,1),(49,0,'2016-02-16 16:38:02',2,0,1),(50,0,'2016-02-16 16:39:30',2,0,1),(51,0,'2016-02-16 16:40:39',2,0,1),(52,0,'2016-02-16 16:42:06',2,0,1),(53,0,'2016-02-16 16:53:00',2,0,1),(54,0,'2016-02-16 16:54:19',2,0,1),(55,0,'2016-02-16 16:54:48',2,0,1),(56,0,'2016-02-16 17:17:00',2,0,1),(57,0,'2016-02-16 17:44:08',2,0,1),(58,0,'2016-02-16 17:49:17',2,0,1),(59,0,'2016-02-16 17:52:03',2,0,1),(60,0,'2016-02-16 17:56:13',2,0,1),(61,0,'2016-02-16 17:57:36',2,0,1),(62,0,'2016-02-16 17:58:30',2,0,1),(63,0,'2016-02-16 17:59:06',2,0,1),(64,0,'2016-02-16 18:01:43',2,0,1),(65,0,'2016-02-16 18:04:06',2,0,1),(66,0,'2016-02-16 18:10:30',2,0,1),(67,0,'2016-02-16 18:18:35',2,0,1),(68,0,'2016-02-16 18:32:11',2,0,1),(69,0,'2016-02-16 18:42:21',2,0,1),(70,0,'2016-02-16 18:46:54',2,0,1),(71,0,'2016-02-16 18:50:40',2,0,1),(72,0,'2016-02-16 18:52:06',2,0,1),(73,0,'2016-02-16 18:59:15',2,0,1),(74,0,'2016-02-16 18:59:48',2,0,1),(75,0,'2016-02-16 19:11:17',2,0,1),(76,0,'2016-02-16 19:18:00',2,0,1),(77,0,'2016-02-16 19:23:42',2,0,1),(78,0,'2016-02-16 19:25:27',2,0,1),(79,0,'2016-02-16 19:27:16',2,0,1),(80,0,'2016-02-16 19:33:05',2,0,1),(81,0,'2016-02-16 19:35:59',2,0,1),(82,0,'2016-02-16 21:59:12',2,0,1),(83,0,'2016-02-16 22:20:02',2,0,1),(84,0,'2016-02-16 22:40:06',2,0,1),(85,0,'2016-02-16 22:41:47',2,0,1),(86,0,'2016-02-16 22:47:35',2,0,1),(87,0,'2016-02-16 22:52:36',2,0,1),(88,0,'2016-02-16 22:54:46',2,0,1),(89,0,'2016-02-16 22:54:49',2,0,1),(90,0,'2016-02-16 22:56:12',2,0,1),(91,0,'2016-02-16 23:05:19',2,0,1),(92,0,'2016-02-16 23:15:00',2,0,1),(93,0,'2016-02-16 23:18:47',2,0,1),(94,0,'2016-02-18 21:30:45',2,0,1),(95,0,'2016-02-19 19:13:45',2,0,1),(96,3,'2016-02-29 17:26:07',2,0,0),(97,4,'2016-02-29 17:55:56',2,0,0),(98,5,'2016-02-29 17:57:41',2,0,0),(99,6,'2016-02-29 17:57:44',2,0,0),(100,7,'2016-02-29 17:58:26',2,0,0),(101,8,'2016-02-29 17:59:17',2,0,0),(102,9,'2016-02-29 19:08:21',2,0,0),(103,10,'2016-02-29 19:09:18',2,0,0),(104,11,'2016-02-29 19:10:11',2,0,0);
/*!40000 ALTER TABLE `respondants` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `responses`
--

DROP TABLE IF EXISTS `responses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `responses` (
  `response_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `response_question_id` bigint(20) NOT NULL,
  `response_respondant_id` bigint(20) NOT NULL,
  `response_text` varchar(255) DEFAULT NULL,
  `response_value` int(11) DEFAULT NULL,
  PRIMARY KEY (`response_id`),
  KEY `FK_QUESTION_RESPONSE_idx` (`response_question_id`),
  CONSTRAINT `FK_QUESTION_RESPONSE` FOREIGN KEY (`response_question_id`) REFERENCES `questions` (`QUESTION_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=271 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `responses`
--

LOCK TABLES `responses` WRITE;
/*!40000 ALTER TABLE `responses` DISABLE KEYS */;
INSERT INTO `responses` VALUES (1,1,3,'',1),(2,1,3,'',1),(3,1,3,'',1),(4,1,3,'',1),(12,18,18,'',2),(13,18,18,'',3),(14,18,18,'',4),(15,18,18,'',5),(16,18,18,'',1),(17,18,18,'',2),(18,18,18,'',3),(19,18,18,'',4),(20,18,18,'',5),(21,18,18,'',6),(22,18,18,'',1),(23,19,19,'',4),(24,19,19,'',3),(25,19,19,'',4),(26,18,18,'',1),(27,18,18,'',3),(28,18,18,'',3),(29,18,18,'',1),(30,18,18,'',1),(31,18,18,'',2),(32,18,18,'',1),(33,18,18,'',2),(34,18,18,'',3),(35,18,18,'',2),(36,18,18,'',3),(37,18,18,'',2),(38,18,18,'',3),(39,18,18,'',1),(40,18,18,'',1),(41,18,18,'',4),(42,18,18,'',2),(43,18,18,'',1),(44,18,18,'',1),(45,18,18,'',4),(46,18,18,'',3),(47,18,18,'',1),(48,18,18,'',1),(49,18,18,'',2),(50,18,18,'',1),(51,18,18,'',1),(52,18,18,'',2),(53,18,18,'',3),(54,1,18,'',1),(55,1,18,'',2),(56,1,18,'',3),(57,2,18,'',1),(58,2,18,'',2),(59,1,18,'',1),(60,1,18,'',2),(61,2,18,'',1),(62,1,18,'',1),(63,1,18,'',2),(64,1,18,'',3),(65,1,18,'',4),(66,1,18,'',5),(67,2,18,'',3),(68,2,18,'',3),(69,2,18,'',4),(70,2,18,'',5),(71,1,18,'',1),(72,1,18,'',1),(73,1,18,'',2),(74,1,18,'',3),(75,2,18,'',3),(76,1,18,'',1),(77,1,18,'',2),(78,1,18,'',3),(79,1,18,'',3),(80,1,18,'',1),(81,1,18,'',2),(82,1,18,'',2),(83,1,18,'',3),(84,1,18,'',5),(85,2,18,'',6),(86,3,18,'',1),(87,4,18,'',2),(88,5,18,'',3),(89,6,18,'',2),(90,2,18,'',1),(91,3,18,'',3),(92,6,18,'',1),(93,1,18,'',1),(94,1,18,'',1),(95,1,18,'',3),(96,2,18,'',3),(97,3,18,'',3),(98,4,18,'',3),(99,5,18,'',5),(100,6,18,'',5),(101,1,21,'',4),(102,2,21,'',6),(103,3,21,'',4),(104,4,21,'',3),(105,5,21,'',1),(106,1,25,'',5),(107,2,25,'',6),(108,3,25,'',3),(109,4,25,'',1),(110,5,25,'',5),(111,6,25,'',2),(112,1,32,'',1),(113,2,32,'',5),(114,3,32,'',5),(115,4,32,'',4),(116,5,32,'',5),(117,1,33,'',5),(118,2,33,'',5),(119,1,34,'',5),(120,2,34,'',4),(121,3,34,'',5),(122,1,36,'',5),(123,2,36,'',3),(124,3,36,'',5),(125,1,37,'',1),(126,2,37,'',4),(127,3,37,'',5),(128,4,37,'',3),(129,5,37,'',5),(130,1,44,'',5),(131,2,44,'',6),(132,3,44,'',5),(133,4,44,'',4),(134,5,44,'',1),(135,6,44,'',1),(136,1,46,'',5),(137,1,48,'',2),(138,1,49,'',2),(139,2,49,'',6),(140,1,52,'',2),(141,1,53,'',3),(142,1,67,'',1),(143,1,67,'',1),(145,2,75,'',2),(146,3,75,'',3),(148,2,76,'',2),(149,3,76,'',3),(150,4,76,'',4),(151,4,76,'',4),(153,1,77,'',1),(154,2,77,'',2),(155,3,77,'',4),(156,4,77,'',4),(157,4,77,'',4),(158,1,78,'',1),(159,2,78,'',1),(160,3,78,'',2),(164,1,79,'',3),(165,2,79,'',2),(166,3,79,'',4),(170,1,80,'',4),(171,2,80,'',2),(172,3,80,'',3),(176,1,81,'',5),(177,2,81,'',2),(178,3,81,'',4),(198,2,82,'',2),(199,3,82,'',3),(200,4,82,'',0),(201,5,82,'',0),(202,6,82,'',0),(206,2,83,'',2),(207,3,83,'',3),(208,4,83,'',1),(209,5,83,'',0),(210,6,83,'',1),(212,1,84,'',2),(213,2,84,'',2),(215,4,84,'',1),(216,5,84,'',2),(217,6,84,'',1),(219,1,85,'',2),(220,2,85,'',2),(222,4,85,'',1),(223,5,85,'',2),(224,6,85,'',1),(225,1,86,'',1),(226,2,86,'',2),(228,4,86,'',2),(229,5,86,'',2),(230,6,86,'',2),(232,1,87,'',1),(233,2,87,'',2),(235,4,87,'',1),(236,5,87,'',2),(237,6,87,'',1),(238,1,90,'',1),(239,2,90,'',1),(241,4,90,'',1),(242,5,90,'',1),(243,6,90,'',1),(245,1,91,'',1),(246,2,91,'',2),(248,4,91,'',1),(249,5,91,'',1),(250,6,91,'',1),(251,1,92,'',1),(252,2,92,'',1),(253,1,93,'',1),(254,2,93,'',2),(256,4,93,'',2),(257,5,93,'',2),(258,6,93,'',1),(259,1,94,'',1),(260,2,94,'',2),(261,1,95,'',2),(262,2,95,'',2),(264,4,95,'',1),(265,5,95,'',1),(266,6,95,'',2),(267,1,97,NULL,1),(268,2,97,NULL,2),(269,1,101,NULL,1),(270,2,101,NULL,2);
/*!40000 ALTER TABLE `responses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `survey_questions`
--

DROP TABLE IF EXISTS `survey_questions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `survey_questions` (
  `SQ_ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `SURVEY_ID` bigint(20) NOT NULL,
  `SQ_QUESTION_ID` bigint(20) NOT NULL,
  `SQ_SEQENCE` int(11) NOT NULL,
  `SQ_REQUIRED` tinyint(1) NOT NULL,
  `SQ_DEPENDENCY` tinyint(1) NOT NULL,
  PRIMARY KEY (`SQ_ID`),
  KEY `SURVEY_ID` (`SURVEY_ID`),
  KEY `SQ_QUESTION_ID` (`SQ_QUESTION_ID`),
  CONSTRAINT `FK_QUESTION_SQ` FOREIGN KEY (`SQ_QUESTION_ID`) REFERENCES `questions` (`QUESTION_ID`),
  CONSTRAINT `SURVEY_QUESTIONS_ibfk_1` FOREIGN KEY (`SURVEY_ID`) REFERENCES `surveys` (`SURVEY_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `survey_questions`
--

LOCK TABLES `survey_questions` WRITE;
/*!40000 ALTER TABLE `survey_questions` DISABLE KEYS */;
INSERT INTO `survey_questions` VALUES (1,2,1,1,0,0),(2,2,2,2,0,0),(3,2,3,3,0,0),(4,2,4,4,0,0),(5,2,5,5,0,0),(6,2,6,6,0,0);
/*!40000 ALTER TABLE `survey_questions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `surveys`
--

DROP TABLE IF EXISTS `surveys`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `surveys` (
  `SURVEY_ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `SURVEY_NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `SURVEY_CREATOR` bigint(20) NOT NULL,
  `SURVEY_ACCOUNT_ID` bigint(20) NOT NULL,
  `SURVEY_TYPE` int(11) NOT NULL DEFAULT '1',
  `SURVEY_STATUS` int(11) NOT NULL DEFAULT '1',
  `MODIFIED_DATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `SURVEY_POSITION` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`SURVEY_ID`),
  KEY `SURVEY_CREATED_BY` (`SURVEY_CREATOR`),
  KEY `FK_SURVEU_ACCOUNT_idx` (`SURVEY_ACCOUNT_ID`),
  KEY `FK_SURVEY_POSITION_idx` (`SURVEY_POSITION`),
  CONSTRAINT `FK_SURVEY_POSITION` FOREIGN KEY (`SURVEY_POSITION`) REFERENCES `positions` (`position_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_SURVEY_USER` FOREIGN KEY (`SURVEY_CREATOR`) REFERENCES `users` (`USER_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `surveys`
--

LOCK TABLES `surveys` WRITE;
/*!40000 ALTER TABLE `surveys` DISABLE KEYS */;
INSERT INTO `surveys` VALUES (1,'test survey',8,1,1,1,'2015-12-25 20:57:34',0),(2,'panda survey',8,0,1,1,'2015-12-25 20:57:34',0),(3,'demo survey',8,1,1,1,'2016-02-16 21:49:39',0);
/*!40000 ALTER TABLE `surveys` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `USER_ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `USER_STATUS` int(11) NOT NULL DEFAULT '1',
  `USER_TYPE` int(11) NOT NULL DEFAULT '1',
  `USER_ACCOUNT_ID` bigint(20) NOT NULL DEFAULT '0',
  `USER_FNAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `USER_LNAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `USER_PASSWORD` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `USER_EMAIL` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `MODIFIED_DATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `USER_PERSIST_LOGIN` tinyint(4) NOT NULL,
  `USER_AVATAR_URL` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `USER_LOCALE` varchar(5) CHARACTER SET utf8 DEFAULT 'en_US',
  `USER_NAME` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`USER_ID`),
  UNIQUE KEY `USER_EMAIL_UNIQUE` (`USER_EMAIL`),
  KEY `ACCOUNT_ID` (`USER_ACCOUNT_ID`),
  CONSTRAINT `FK_USER_ACCOUNT` FOREIGN KEY (`USER_ACCOUNT_ID`) REFERENCES `accounts` (`ACCOUNT_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (2,1,1,1,'Sri','Kaza','password','sri@employmeo.com','2015-12-21 20:26:46',0,'','en_US',NULL),(4,1,1,0,'kam','tastic','5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8','kamtastic@kammy.com','2015-12-24 21:18:24',0,NULL,'en',NULL),(6,1,1,0,'kam','tastic','5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8','kamtastic@kam.com','2015-12-24 21:18:44',0,NULL,'en',NULL),(7,1,1,0,'kamper','tastic','6cf615d5bcaac778352a8f1f3360d23f02f34ec182e259897fd6ce485d7870d4','kampertastic@kam.com','2015-12-27 16:01:53',0,NULL,'en',NULL),(8,1,1,0,'1234','4321','5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8','kampertas3tic@kam.com','2015-12-24 22:11:36',0,NULL,'en',NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'employmeo'
--

--
-- Dumping routines for database 'employmeo'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-02-29 11:25:41
