
# Work in Progress

[![Deploy to Bluemix](https://bluemix.net/deploy/button.png)](https://bluemix.net/deploy?repository=https://github.com/IBM/gdpr-fingerprint-pii.git)

# Personal Data Extraction and Scoring of an unstructured text document

The General Data Protection Regulation (GDPR) is a regulation by which the European Parliament, the Council of the European Union and the European Commission intend to strengthen and unify data protection for all individuals within the European Union (EU). It also addresses the export of personal data outside the EU. 

Under the EU's new General Data Protection Regulation, enterprises around the world must not only keep personal data private, but they will also be required to "forget" any personal data related to an individual on request -- and the GDPR right to be forgotten will be a significant part of compliance with the new rule. 

In this this journey, we show you how to extract personal data from unstructured text document and assign confidence score which represents the probability of identifying an individual from the personal data identified. 

**What does this journey achieve?**
This application extracts personal data from an unstructured chat transcript. It also provides a confidence score, which is an indicator of how confidently an individual can be identified from the personal data available and extracted from the text.

Let us try to understand this with an example chat transcript as below<br /><br />
   
*Rep: This is Thomas. How can I help you? <br />
Caller: This is Alex. I want to change my plan to corporate plan <br />
Rep: Sure, I can help you. Do you want to change the plan for the number from which you are calling now? <br />
Caller: yes <br />
Rep: For verification purpose may I know your date of birth and email id <br />
Caller: My data of birth is 10-Aug-1979 and my email id is alex@gmail.com <br />
Rep: Which plan do you want to migrate to <br />
Caller: Plan 450 unlimited <br />
Rep: Can I have your company name and date of joining <br />
Caller: I work for IBM and doj 01-Feb-99 <br />
Rep: Ok.. I have taken your request to migrate plan to 450 unlimited. You will get an update in 3 hours. Is there anything else that I can help you with <br />
Caller: No <br />
Rep: Thanks for calling Vodaphone. Have a good day. <br />
Caller: you too <br />*


Personal Data extracted from the above text: <br />

*Name: Alex <br />
Date of birth: 10-Aug-1979 <br />
Email id: alex@gmail.com <br />
Company: IBM  <br />
Date of joining: 01-Feb-99 <br />
Also the confidence score is calculated <br />*
**Confidence score:** 0.7


This journey gives you a step by step instructions for:
- Building a custom model using WKS and having NLU use that model for personal data extraction
- Using regular expressions, in addition to NLU, to extract personal data from unstructured text
- Configuring pre-identified personal data with weightage and coming up with a score representing the confidence level of identifying an individual using the personal data identified
- Viewing the score and the personal data identified in a tree structure for better visualization

# Flow
<img src="images/Architecture.png" alt="Architecture/Flow diagram" width="640" border="10" />


# Included Components
* [Watson Knowledge Studio](https://console.bluemix.net/docs/services/knowledge-studio/index.html#wks_overview_full): 
  A tool to create a machine-learning model that understands the linguistic nuances, meaning, and relationships specific to your industry or to create a rule-based model that finds entities in documents based on rules that you define.

* [Watson Natural Language Understanding](https://www.ibm.com/watson/services/natural-language-understanding/): 
  A Bluemix service that can analyze text to extract meta-data from content such as concepts, entities, keywords, categories, sentiment, emotion, relations, semantic roles, using natural language understanding.

* [Liberty for Java](https://console.bluemix.net/docs/runtimes/liberty/index.html#liberty_runtime): Develop, deploy, and scale Java web apps with ease. IBM WebSphere Liberty Profile is a highly composable, ultra-fast, ultra-light profile of IBM WebSphere Application Server designed for the cloud.


# Watch the Overview Video

[![Coming Soon]](http://localhost)


# Steps
1. [Prerequisites](#1-prerequisites)
2. [Deploy the application to Bluemix](#2-deploy-the-application-to-bluemix)
3. [Develop Watson Knowledge Studio model](#3-develop-watson-knowledge-studio-model)
4. [Deploy WKS model to Watson Natural Language Understanding](#4-deploy-wks-model-to-watson-natural-language-understanding)
5. [Verify that configuration parameters are correct](#5-verify-that-configuration-parameters-are-correct)
6. [Using Personal Data Extractor application](#6-using-personal-data-extractor-application)


### 1. Prerequisites
- Bluemix account
- Watson Knowledge Studio account and WKS skills

### 2. Deploy the application to Bluemix
- Use the "Deploy to Bluemix" button at the top of this documentation to deploy the 
  application to Bluemix
- Brief description of application components
    - **Personal Data Extractor component:** <br />
Personal Data Extractor component is the controller which controls the flow of data between all the components. 
    - **Scorer component:**  <br />
Scorer component calculates the score of a document, which will be between 0 and 1, based on the personal data identified and the configuration data. It uses the below algorithm 
<br />
Let doc_score be 0 <br />
For each category{ <br />
&emsp;cat_weight = weightage for the category<br />
&emsp;cat_entity_types = list of entity types for the category<br />
&emsp;for each cat_entity_types{<br />
&emsp;&emsp;doc_score = doc_score +( ( cat_weight/100 ) * ( 100 - doc_score ) )<br />
&emsp;}<br />
}
    - **Regex component:**  <br />
Regex component parses the input text using the regular expressions provided in the configuration files to extract personal data. Regular expressions are used to extract personal data where NLU won’t is not effective enough. It augments the results provided by NLU.
    - **Viewer component:**  <br />
Viewer component is the user interface component of the application. User can browse to a file, containing chat transcripts, and submit it for personal data extraction the scoring. The personal data is then shown in a tree structure along with scores. Overall confidence score for the document is also shown
<img src="images/Viewer.png" alt="Personal Data View diagram" width="640" border="10" />

### 3. Develop Watson Knowledge Studio model
Note that building Watson Knowledge Studio annotations and building a model is a complex and iterative process. The intention here is not to deal with the end to end process but to give an idea on the process so that it can be modified/extended as the requirements suit<br/>
The steps described here is to import the Type Systems and ground truth on which to train the machine learning model, annotator development and evaluation, and then deploying it into Natural Language Understanding
#### 3.1 Import Artifacts
In github repository, navigate to WKS folder. Download the files named “Documents.zip” and “TypeSystems.json” to your local filesystem
#### 3.2 Create Project
Login to the WKS instance. If you do not have a WKS account, create a Watson Knowledge Studio Account. You can sign up for a 30 day free trial here:
https://www.ibm.com/us-en/marketplace/supervised-machine-learning/purchase#product-header-top
- Click "Create Project". In the “Create New Project” pop up window, enter the name of the new project.
<img src="images/WKSCreateProject.png" alt="Create Project" width="640" border="10" />
- The “Description” field is optional. 
- In the "Select a language" drop down field, choose “English”. Machine learning-based tokenizer is default option you need to choose. 
Click "Create"
<img src="images/WKSCreateProjectOptions.png" alt="Create Project Options" width="640" border="10" />
#### 3.3 Import type system
- After the project is created, click “Type Systems” on the top navigation bar
- Select “Entity Types” tab and click “Import”
<img src="images/WKSImportTypeSystems.png" alt="Import Type Systems" width="640" border="10" />
- Click the import/download icon and browse to the file “TypeSystems.json” file that was downloaded, from git repository, earlier
<img src="images/WKSImportTypeSystemsBrowse.png" alt="Import Type Systems Browse" width="640" border="10" />
- The selected file gets listed in the popup window. Click “Import”
<img src="images/WKSTypeSystemsImport.png" alt="WKSTypeSystemsImport" width="640" border="10" />
- The documents are listed as below. Make a note of entity types or keywords that we are interested in. You can add/edit entities if you wish.
<img src="images/WKSImportedEntityTypes.png" alt="WKSImportedEntityTypes" width="640" border="10" />
#### 3.4 Import Documents
- Click “Documents” on the top navigation bar
<img src="images/WKSImportDocuments.png" alt="WKSImportDocuments" width="640" border="10" />
- Click “Import Document Set”
<img src="images/WKSImportDocSet.png" alt="WKSImportDocSet" width="640" border="10" />
- Click import/download button on the popup window
- Browse to and select “Documents.zip” file that was downloaded from github repository earlier
- Click “Import”
<img src="images/WKSDocImport.png" alt="WKSDocImport" width="640" border="10" />
- Documents are now imported. 
#### 3.5 Create and assign annotation sets
- Click “Annotation Sets” to create annotation sets
<img src="images/WKSAnnotationSet.png" alt="WKSAnnotationSet" width="640" border="10" />
- Click “Create Annotation Sets”
<img src="images/WKSCreateAnnotationSet.png" alt="WKSCreateAnnotationSet" width="640" border="10" />
- Type in name for the annotation set and click “Generate”
<img src="images/WKSAnnotationGenerate.png" alt="WKSAnnotationGenerate" width="640" border="10" />
- Annotation set is created. 
<img src="images/WKSAnnotationCreated.png" alt="WKSAnnotationCreated" width="640" border="10" />
#### 3.6 Human Annotation
- Click “Human Annotation” on the top navigation bar
- Click “Add Task”
<img src="images/WKSAddTask.png" alt="WKSAddTask" width="640" border="10" />
- Enter a name for the task and click “Create”
<img src="images/WKSCreateTask.png" alt="WKSCreateTask" width="640" border="10" />
- In the popup window, select the Annotation Set that was created earlier
- Click “Create Task”
<img src="images/WKSCreateTask2.png" alt="WKSCreateTask2" width="640" border="10" />
- Task should get created. Click on the Task
<img src="images/WKSTaskCreated.png" alt="WKSTaskCreated" width="640" border="10" />
- Next we need to annotate, mapping document entries with entity types defined in Type system
- Click “Annotate”
<img src="images/WKSAnnotate.png" alt="WKSAnnotate" width="640" border="10" />
- Click OK for any Alert message that pops up
- Ground truth editor opens up. Here you can select each document one by one to annotate all the documents. Click on any of the documents
<img src="images/WKSGroundTruthFiles.png" alt="WKSGroundTruthFiles" width="640" border="10" />
- From the documents select an entry that you want to be extracted from the document as entities. Then click on the entity type on the right hand side of the screen
- Similarly do this for all the keywords in the document
<img src="images/WKSEntityMapping.png" alt="WKSEntityMapping" width="640" border="10" />
- Once all the keywords are mapped to entity types, select “Completed” from the status dropdown
<img src="images/WKSMappingComplete.png" alt="WKSMappingComplete" width="640" border="10" />
- Click “Save” to save the changes
<img src="images/WKSMappingSaved.png" alt="WKSMappingSaved" width="640" border="10" />
- Repeat above steps for all the document. Once all the documents are annotated and completed, click “Submit All”
<img src="images/WKSAllFilesAnnotationCompleted.png" alt="WKSAllFilesAnnotationCompleted" width="640" border="10" />
- If the status shows “IN PROGRESS”, click “Refresh” button
<img src="images/WKSAnnotationStatusRefresh.png" alt="WKSAnnotationStatusRefresh" width="640" border="10" />
- Status should now change to “SUBMITTED”
- Select the Annotation Set name and click “Accept” button
<img src="images/WKSAnnotationAccept.png" alt="WKSAnnotationAccept" width="640" border="10" />
- Click “OK” on the confirmation popup window
- Task status now changes to “COMPLETED”
<img src="images/WKSAnnotationCompleted.png" alt="WKSAnnotationCompleted" width="640" border="10" />
- Click “Annotator Component” on the top navigation bar 
<img src="images/WKSAnnotatorComponentLink.png" alt="WKSAnnotatorComponentLink" width="640" border="10" />
- We will create “Machine Learning” annotator. So click “Create this type of annotator” under “Machine Learning”
<img src="images/WKSMachineLearning.png" alt="WKSMachineLearning" width="640" border="10" />
- Under “Document Set” select the set whose annotation was completed in previous steps. Click “Next”
<img src="images/WKSCreateAnnotator.png" alt="WKSCreateAnnotator" width="640" border="10" />
- Click “Train and Evaluate”
<img src="images/WKSTrainEvaluate.png" alt="WKSTrainEvaluate" width="640" border="10" />
- Train and Evaluate process takes place. It will take a few minutes for this step to complete 
<img src="images/WKSAnnotatorProcessing.png" alt="WKSAnnotatorProcessing" width="640" border="10" />

### 4. Deploy WKS model to Watson Natural Language Understanding
- Once Train and Evaluate processes are over the model is created. Click “Details”
<img src="images/WKSAnnotatorCreated.png" alt="WKSAnnotatorCreated" width="640" border="10" />
- Click “Take Snapshot”
<img src="images/WKSSnapshot.png" alt="WKSSnapshot" width="640" border="10" />
- Enter any meaningful description for the snapshot. Click “OK”
<img src="images/WKSSnapshotOK.png" alt="WKSSnapshotOK" width="640" border="10" />
- Snapshot is created
<img src="images/WKSSnapshotCreated.png" alt="WKSSnapshotCreated" width="640" border="10" />
- Click “Deploy” to deploy on the NLU instance created when the application was deployed on Bluemix. Click “Deploy”
<img src="images/WKSDeploy.png" alt="WKSDeploy" width="640" border="10" />
- Select “Natural Language Understanding”. Click “Next”
<img src="images/WKSDeployModel.png" alt="WKSDeployModel" width="640" border="10" />
- Select your Bluemix Region, Space and NLU service instances. Click “Deploy”
<img src="images/WKSDeployNLUIntsance.png" alt="WKSDeployNLUIntsance" width="640" border="10" />
- WKS model should get deployed on the NLU. Make a note of the Model Id. Click “OK”
<img src="images/WKSModelId.png" alt="WKSModelId" width="640" border="10" />
- Click OK. Model is deployed to NLU
<img src="images/WKSDeployedSnapshot.png" alt="WKSDeployedSnapshot" width="640" border="10" />

### 5. Verify that configuration parameters are correct
- Navigate to the Bluemix dashboard. Click on the GDPR application that is deployed
<img src="images/BMDashboard.png" alt="BMDashboard" width="640" border="10" />
- Click “Runtime”
<img src="images/Runtime.png" alt="Runtime" width="640" border="10" />
- Click “Environment Variables” and scroll down to user defined variables
<img src="images/EnvVar.png" alt="EnvVar" width="640" border="10" />
- Update the model id against “wks_model” and verify that all other configuration 
parameters are correct. Click “Save”
<img src="images/EnvVarModelId.png" alt="EnvVarModelId" width="640" border="10" />
- The application restages. When the application is running, we are ready to use the application to extract personal data and score them from unstructured text
<img src="images/AppRestarting.png" alt="AppRestarting" width="640" border="10" />

### 6. Using Personal Data Extractor application
- Open the application URL from a browser
<img src="images/AppHomePage.png" alt="AppHomePage" width="640" border="10" />
- Click "Choose File". On the popup window browse to the text file from which personal 
data should be extracted. Select the file and click "Open"
- Initially you see a collapsed tree view as below
<img src="images/TreeView1.png" alt="TreeView1" width="640" border="10" />
- Click on nodes to expand and collapse the nodes. Full tree view looks as below
<img src="images/TreeView2.png" alt="TreeView2" width="640" border="10" />

  
