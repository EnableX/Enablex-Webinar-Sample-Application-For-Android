# EnableX Webinar Android App: Video Calling, Raise Hand, and API Integration Guide
Explore the EnableX-Webinar-Sample-Application-For-Android, a video calling app that harnesses the power of EnableX infrastructure, APIs, and Toolkit. This application allows developers to dive into real-time video communication while incorporating the unique ""Raise Hand"" feature. 

With this sample app, you can effortlessly: 

1. Create a Virtual Room using the REST video API. 

2. Obtain the essential Room Credential (Room ID). 

3. Seamlessly Join Virtual Rooms as either a moderator or participant while ensuring security. 

Enjoy a range of valuable features, including: 

Mute/Unmute Video: Take control of your video feed. 

Mute/Unmute Audio: Manage your audio input with ease. 

Raise Hand: Signal your desire to speak or engage. 

Allow User to Come on Floor: Grant others the opportunity to participate actively. 

For additional details, please visit our comprehensive Developer Center at https://developer.enablex.io/ 

Experience the future of video communication with EnableX! 

## 1. Get started

### 1.1 Prerequisites

#### 1.1.1 App Id and App Key 

You would need API Credential to access EnableX platform. To do that, simply create an account with us. It’s absolutely free!

* Create an account with EnableX - https://www.enablex.io/free-trial/
* Create your Application
* Get your App ID and App Key delivered to your email

#### 1.1.2 Sample Android Client 

* Clone or download this Repository [https://github.com/EnableX/Enablex-Webinar-Sample-Application-For-Android.git] 


#### 1.1.3 Test Application Server 

An Application Server is required for your android App to communicate with EnableX. We have different variants of Application Server Sample Code. Pick one in your preferred language and follow instructions given in README.md file of respective Repository.

* NodeJS: https://github.com/EnableX/Video-Conferencing-Open-Source-Web-Application-Sample.git 
* PHP: https://github.com/EnableX/Group-Video-Call-Conferencing-Sample-Application-in-PHP

Note the following:
•    You need to use App ID and App Key to run this Service.
•    Your android Client End Point needs to connect to this Service to create Virtual Room and Create Token to join the session.
•    Application Server is created using [EnableX Server API] (https://developer.enablex.io/docs/references/apis/video-api/index/), a Rest API Service helps in provisioning, session access and post-session reporting.

If you would like to test the quality of EnableX video call before setting up your own application server,  you may run the test on our pre-configured environment. Refer to point 3 for more details on this.



#### 1.1.4 Configure Android Client 

* Open the App
* Go to WebConstants and change the following:
``` 
    /* To try the App with Enablex Hosted Service you need to set the kTry = true When you setup your own Application Service, set kTry = false */
        
        public  static  final  boolean kTry = true;
        
    /* Your Web Service Host URL. Keet the defined host when kTry = true */
    
        String kBaseURL = "https://demo.enablex.io/"
        
    /* Your Application Credential required to try with EnableX Hosted Service
        When you setup your own Application Service, remove these */
        
        String kAppId = ""  
        String kAppkey = ""  
 ```


### 1.2 Test

#### 1.2.1 Open the App

* Open the App in your Device. You get a form to enter Credentials i.e. Name & Room Id.
* You need to create a Room by clicking the "Create Room" button.
* Once the Room Id is created, you can use it and share with others to connect to the Virtual Room to carry out an RTC Session either as a Moderator or a Participant (Choose applicable Role in the Form).

Note: Only one user with Moderator Role allowed to connect to a Virtual Room while trying with EnableX Hosted Service. Your Own Application Server may allow upto 5 Moderators. 
  
Note:- In case of emulator/simulator your local stream will not create. It will create only on real device.


## 2. Testing Environment

If you would like to test the quality of EnableX video call before setting up your own application server,  you may run the test on our pre-configured environment.https://try.enablex.io/
In this environment, you will only be able to:

* Conduct a single session with a total durations of no more than 15 minutes
* Host a multiparty call with no more than 6 participants 

> More information on Testing Environment: https://developer.enablex.io/docs/guides/video-guide/sample-codes/video-calling-app/#demo-application-server

Once you have tested them, it is important that you set up your own Application Server to continue building a multiparty android video calling app. Refer to section 1.1.3 on how to set up the application server. 


## 3. Set up Your Own Application Server

You may need to set up your own Application Server after you tried the Sample Application with EnableX hosted Server. We have different variants of Application Server Sample Code. Pick the one in your preferred language and follow instructions given in respective README.md file.

* NodeJS: [https://github.com/EnableX/Video-Conferencing-Open-Source-Web-Application-Sample.git]
* PHP: [https://github.com/EnableX/Group-Video-Call-Conferencing-Sample-Application-in-PHP]

Note the following:
* You need to use App ID and App Key to run this Service.
* Your Android Client End Point needs to connect to this Service to create Virtual Room and Create Token to join the session.
* Application Server is created using EnableX Server API, a Rest API Service helps in provisioning, session access and post-session reporting.  

To know more about Server API, go to:
https://developer.enablex.io/docs/guides/video-guide/sample-codes/video-calling-app/#demo-application-server
  
  
## 4 Android Toolkit

This Sample Applcation uses EnableX Android Toolkit to communicate with EnableX Servers to initiate and manage Real Time Communications. Please update your Application with latest version of EnableX Android Toolkit as and when a new release is available.

* Documentation: https://developer.enablex.io/docs/references/sdks/video-sdk/android-sdk/index/
* Download Toolkit: https://developer.enablex.io/docs/references/sdks/video-sdk/android-sdk/index/


## 5. Support

EnableX provides a library of Documentations, How-to Guides and Sample Codes to help software developers get started. 

> Go to https://developer.enablex.io/. 

You may also write to us for additional support at support@enablex.io.
