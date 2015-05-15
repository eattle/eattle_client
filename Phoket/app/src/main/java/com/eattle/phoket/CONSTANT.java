package com.eattle.phoket;

import com.eattle.phoket.device.BlockDevice;
import com.eattle.phoket.device.CachedBlockDevice;
import com.eattle.phoket.helper.DatabaseHelper;

/**
 * Created by GA on 2015. 5. 14..
 */
public class CONSTANT {
    public static long TIMEINTERVAL=15000L;//사진 분류시 간격(millisecond)

    public static int FOLDER = 0;
    public static int TAG = 1;
    public static int DEFAULT_TAG = 2;
    public static int BOUNDARY = 3; //스토리에 있는 사진의 개수가 BOUNDARY 이하일 경우, 다른 형식으로 보여지게 된다
    public static int ISUSBCONNECTED = 0; //USB가 연결되어 있으면 1, 아니면 0
    public static int PASSWORD = 0;//비밀번호 해제 안됬으면 0, 해제 됬으면 1
    public static int PASSWORD_TRIAL = 5; //비밀번호가 PASSWORD_TRIAL보다 많이 틀릴 경우 앱 종료
    public static CachedBlockDevice BLOCKDEVICE;

    public static final String PACKAGENAME="com.example.cds.eattle_prototype_2";
    public static final String appDBPath="/data/" + CONSTANT.PACKAGENAME + "/databases/" + DatabaseHelper.DATABASE_NAME;//스마트폰 앱단의 DB 경로

    public static final int BIGSTORYCARD = 0;
    public static final int DAILYCARD = 1;
    public static final int TAGSCARD = 2;
    public static final int TOPHOKETCARD = 3;
    public static final int NOTIFICARD = 4;

    public static final int MSG_REGISTER_CLIENT = 1;//MainActivity와 Service가 bind 되었을 때
    public static final int MSG_UNREGISTER_CLIENT = 2;//MainActivity와 Service가 bind를 중단하라는 메세지
    public static final int START_OF_PICTURE_CLASSIFICATION = 3;//MainActivity가 Service에게 사진 정리를 요청하는 메세지
    public static final int END_OF_PICTURE_CLASSIFICATION = 4;//Service가 MainActivity에게 사진 정리를 완료 했다고 보내는 메세지
    public static final int END_OF_SINGLE_STORY = 5;//스토리가 정리되는대로 바로바로 보여주기 위하여 정의한 메세지,하나의 스토리가 정리될때마다 보낸다

    public static String convertFolderNameToStoryName(String folderName){
        String name = "";
        if (folderName.contains("~")) {//여러 날짜를 포함하는 스토리일 경우
            String[] bigSplit = folderName.split("~");
            String[] tempName = bigSplit[0].split("_");
            name += tempName[0] + "년 " + tempName[1] + "월 " + tempName[2] + "일 ~ ";
            tempName = bigSplit[1].split("_");
            name += tempName[1] + "월 " + tempName[2].replace("의", "일의");
        } else {//단일 날짜의 스토리일 경우
            String[] tempName = folderName.split("_");
            name = tempName[0] + "년 " + tempName[1] + "월 " + tempName[2].replace("의", "일의");
        }

        return name;
    }

    public static String convertFolderNameToDate(String folderName){
        String name = "";
        if (folderName.contains("~")) {//여러 날짜를 포함하는 스토리일 경우
            String[] bigSplit = folderName.split("~");
            String[] tempName = bigSplit[0].split("_");
            name += tempName[0] + "." + tempName[1] + "." + tempName[2] + " ~ ";
            tempName = bigSplit[1].split("_");
            name += tempName[1] + "." + tempName[2];
        } else {//단일 날짜의 스토리일 경우
            String[] tempName = folderName.split("_");
            name = tempName[0] + "." + tempName[1] + "." + tempName[2];
        }
        name = name.replace("의 스토리", "");


        return name;

    }


}

