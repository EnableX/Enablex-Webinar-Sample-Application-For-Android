package com.enablex.webinar.web_communication;

public class WebConstants {


    /* To try the app with Enablex hosted service you need to set the kTry = true */
        public  static  final  boolean kTry = true;

    /*Your webservice host URL, Keet the defined host when kTry = true */


        public static final String kBaseURL = "https://api.enablex.io/video/v2/rooms/";
    /*The following information required, Only when kTry = true, When you hosted your own webservice remove these fileds*/

    /*Use enablex portal to create your app and get these following credentials*/
    public static final String kAppId = "App-id";
    public static final String kAppkey = "App-key";


         public static final String getRoomId = "createRoom/";
         public static final int getRoomIdCode = 1;
         public static final String validateRoomId = "getRoom/";
         public static final int validateRoomIdCode = 2;
         public static final String getTokenURL = "tokens";
         public static final int getTokenURLCode = 3;
}
