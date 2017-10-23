
# Work in Progress

[![Deploy to Bluemix](https://bluemix.net/deploy/button.png)](https://bluemix.net/deploy?repository=https://github.com/IBM/gdpr-fingerprint-pii.git)

# Personal Data Extraction and Scoring of an unstructured text document

The General Data Protection Regulation (GDPR) is a regulation by which the European Parliament, the Council of the European Union and the European Commission intend to strengthen and unify data protection for all individuals within the European Union (EU). It also addresses the export of personal data outside the EU. 

Under the EU's new General Data Protection Regulation, enterprises around the world must not only keep personal data private, but they will also be required to "forget" any personal data related to an individual on request -- and the GDPR right to be forgotten will be a significant part of compliance with the new rule. 

In this this journey, we show you how to extract personal data from unstructured text document and assign confidence score which represents the probability of identifying an individual from the personal data identified. 

**What does this journey achieve?**
This application extracts personal data from an unstructured chat transcript. It also provides a confidence score, which is an indicator of how confidently an individual can be identified from the personal data available and extracted from the text.

Let us try to understand this with an example
Sample transcript
Rep: This is Thomas. How can I help you?
Caller: This is Alex. I want to change my plan to corporate plan
Rep: Sure, I can help you. Do you want to change the plan for the number from which you are calling now?
Caller: yes
Rep: For verification purpose may I know your date of birth and email id
Caller: My data of birth is 10-Aug-1979 and my email id is alex@gmail.com
Rep: Which plan do you want to migrate to
Caller: Plan 450 unlimited
Rep: Can I have your company name and date of joining
Caller: I work for IBM and doj 01-Feb-99
Rep: Ok.. I have taken your request to migrate plan to 450 unlimited. You will get an update in 3 hours. Is there anything else that I can help you with
Caller: No
Rep: Thanks for calling Vodaphone. Have a good day.
Caller: you too

Personal Data extracted from the above text:
Name: Alex
Date of birth: 10-Aug-1979
Email id: alex@gmail.com
Company: IBM 
Date of joining: 01-Feb-99
Also the confidence score is calculated
**Confidence score:** 0.7


# Offering Type
Cognitive | Cloud | Emerging

# Introduction
Two sentence introduction

# Author
By Author(s)

# Included Journeys (Composite Journey only)
- List all the journeys that make up this composite journey

# Code
link to code repo

# Show repo (Composite Journey only)
- Yes, if code is provided that links the sub-journeys together
- No, otherwise

# Demo
link to demo video

# Video
link to youtube video

# Overview
Two to three sentences about what the journey does and uses.

When the reader has completed this journey, they will understand how to:

- goal 1
- goal 2

# Flow
![](link to architecture.png)

1. Step 1
2. Step 2

# Included components
- [title](http://localhost): description
- Optional for Composite Journey. If not provided defaults to the aggregate of the included journeys.

# Featured technologies
- [title](http://localhost): description
- Optional for Composite Journey. If not provided defaults to the aggregate of the included journeys.

# Blog
- Optional for Composite Journey. If not provided defaults to the aggregate of the included journeys.

### Author
### Title
### Content

# Links
- [title](http://localhost): description
- Optional for Composite Journey. If not provided defaults to the aggregate of the included journeys.
