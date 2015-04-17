package com.example.cds.eattle_prototype_2;

/**
 * Created by CDS on 15. 3. 21..
 */
public class CONSTANT {
    public static long TIMEINTERVAL=15000L;//사진 분류시 간격(millisecond)

    public static int FOLDER = 0;
    public static int TAG = 1;
    public static int BOUNDARY = 5; //스토리에 있는 사진의 개수가 BOUNDARY 이하일 경우, 다른 형식으로 보여지게 된다
    public static int ISUSBCONNECTED = 0; //USB가 연결되어 있으면 1, 아니면 0
    public static int PASSWORD = 0;//비밀번호 해제 안됬으면 0, 해제 됬으면 1
}
