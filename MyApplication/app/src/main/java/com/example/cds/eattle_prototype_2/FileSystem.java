package com.example.cds.eattle_prototype_2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.example.cds.eattle_prototype_2.device.BlockDevice;
import com.example.cds.eattle_prototype_2.device.CachedBlockDevice;
import com.example.cds.eattle_prototype_2.host.BlockDeviceApp;
import com.example.cds.eattle_prototype_2.host.UsbDeviceHost;


public class FileSystem {
    final int CLUSTERSPACESIZE = 512; //클러스터 전체 사이즈
    final int CLUSTERCNT = 30000; //클러스터 갯수

    final int SPACELOCATION = 504; //다음공간 위치
    final int NEXTLOCATION = 506; //다음주소 위치
    //final int ISEMPTYLOCATION = 511; // 빈공간인지 확인해주는 위치
    final int ISCOMPLETELOCATION = 510;  // 백업 완료했는지 확인해주는것의 위치

    final int STRINGSIZE = 77; //문자 블럭 크기
    final int STRINGLENSIZE = 3; //문자길이 블럭 크기
    final int SPACESIZE = 2; //공간위치 블럭 크기
    final int LOCATIONSIZE = 4; //주소 블럭 크기

    byte[] buffer = new byte[(int) CLUSTERSPACESIZE];

    int dummycnt = 1;

    private static FileSystem Instance;
    //탐색 테이블
    String[][] searchtable = new String[100][5];
    int endpoint = 0;

    //비트맵 이미지 리사이징
    public Bitmap resizeBitmapImageFn(Bitmap bmpSource, int maxResolution) {
        int iWidth = bmpSource.getWidth();      //비트맵이미지의 넓이
        int iHeight = bmpSource.getHeight();     //비트맵이미지의 높이
        int newWidth = iWidth;
        int newHeight = iHeight;
        float rate = 0.0f;

        //이미지의 가로 세로 비율에 맞게 조절
        if (iWidth > iHeight) {
            if (maxResolution < iWidth) {
                rate = maxResolution / (float) iWidth;
                newHeight = (int) (iHeight * rate);
                newWidth = maxResolution;
            }
        } else {
            if (maxResolution < iHeight) {
                rate = maxResolution / (float) iHeight;
                newWidth = (int) (iWidth * rate);
                newHeight = maxResolution;
            }
        }

        return Bitmap.createScaledBitmap(bmpSource, newWidth, newHeight, true);
    }

    private void passwardInput(int passward, CachedBlockDevice blockDevice){//암호넣기
        pushBinary(2,passward,4,0,blockDevice);
        blockDevice.flush();
    }

    private int passwardOutput(CachedBlockDevice blockDevice){//암호빼기
        byte[] dummyBuffer = new byte[(int) CLUSTERSPACESIZE];
        blockDevice.readBlock(2, dummyBuffer);
        int passward = readIntToBinary(2,0,4,dummyBuffer,blockDevice);
        return passward;
    }

    private int emptySpaceSearch(CachedBlockDevice blockDevice){//빈공간 반환
        byte[] dummyBuffer = new byte[(int) CLUSTERSPACESIZE];
        int num=0;
        int i;


        blockDevice.readBlock(3, dummyBuffer);
        for(i=0;i<CLUSTERSPACESIZE;i++){
            if( (dummyBuffer[i] & 0x80) == 0 ){ num = 0; break; }
            if( (dummyBuffer[i] & 0x40) == 0 ){ num = 1; break; }
            if( (dummyBuffer[i] & 0x20) == 0 ){ num = 2; break; }
            if( (dummyBuffer[i] & 0x10) == 0 ){ num = 3; break; }
            if( (dummyBuffer[i] & 0x08) == 0 ){ num = 4; break; }
            if( (dummyBuffer[i] & 0x04) == 0 ){ num = 5; break; }
            if( (dummyBuffer[i] & 0x02) == 0 ){ num = 6; break; }
            if( (dummyBuffer[i] & 0x01) == 0 ){ num = 7; break; }
        }

        // (i * 8) + num ->몇번째 인지
        int addressnum = (i * 8) + num;
        blockDevice.readBlock(addressnum + 4, dummyBuffer);

        for(i=0;i<CLUSTERSPACESIZE;i++){
            if( (dummyBuffer[i] & 0x80) == 0 ){ return (4096 * (addressnum +1)) + (i * 8) + 0 + 4; }
            if( (dummyBuffer[i] & 0x40) == 0 ){ return (4096 * (addressnum +1)) + (i * 8) + 1 + 4; }
            if( (dummyBuffer[i] & 0x20) == 0 ){ return (4096 * (addressnum +1)) + (i * 8) + 2 + 4; }
            if( (dummyBuffer[i] & 0x10) == 0 ){ return (4096 * (addressnum +1)) + (i * 8) + 3 + 4; }
            if( (dummyBuffer[i] & 0x08) == 0 ){ return (4096 * (addressnum +1)) + (i * 8) + 4 + 4; }
            if( (dummyBuffer[i] & 0x04) == 0 ){ return (4096 * (addressnum +1)) + (i * 8) + 5 + 4; }
            if( (dummyBuffer[i] & 0x02) == 0 ){ return (4096 * (addressnum +1)) + (i * 8) + 6 + 4; }
            if( (dummyBuffer[i] & 0x01) == 0 ){ return (4096 * (addressnum +1)) + (i * 8) + 7 + 4; }
        }

        return 0;

		/*
		for(int i=2; i<CLUSTERCNT ;i++){
			if(BLOCK[i][ISEMPTYLOCATION] == (byte)0)
				return i;
		}
		return 0;
		*/
    }



    private void clusterWriteCheck(int location, CachedBlockDevice blockDevice){ //클러스터 쓰고 표시
        byte[] dummyBuffer = new byte[(int) CLUSTERSPACESIZE];
        boolean flag = true;
        location = location -4;

        blockDevice.readBlock((location >> 12) -1 + 4, dummyBuffer);

        if(((location & 0x0FFF) & 0x07)  == 0){ dummyBuffer[(location & 0x0FFF) >> 3] |= 0x80; }
        if(((location & 0x0FFF) & 0x07)  == 1){ dummyBuffer[(location & 0x0FFF) >> 3] |= 0x40; }
        if(((location & 0x0FFF) & 0x07)  == 2){ dummyBuffer[(location & 0x0FFF) >> 3] |= 0x20; }
        if(((location & 0x0FFF) & 0x07)  == 3){ dummyBuffer[(location & 0x0FFF) >> 3] |= 0x10; }
        if(((location & 0x0FFF) & 0x07)  == 4){ dummyBuffer[(location & 0x0FFF) >> 3] |= 0x08; }
        if(((location & 0x0FFF) & 0x07)  == 5){ dummyBuffer[(location & 0x0FFF) >> 3] |= 0x04; }
        if(((location & 0x0FFF) & 0x07)  == 6){ dummyBuffer[(location & 0x0FFF) >> 3] |= 0x02; }
        if(((location & 0x0FFF) & 0x07)  == 7){ dummyBuffer[(location & 0x0FFF) >> 3] |= 0x01; }

        blockDevice.writeBlock((location >> 12) -1 + 4, dummyBuffer);

        for(int i=0; i<CLUSTERSPACESIZE;i++){
            if(dummyBuffer[i] != -1) {
                flag = false;
                break;
            }
        }

        if(flag){
            blockDevice.readBlock(3, dummyBuffer);
            if((((location >> 12) -1) & 0x07)  == 0){ dummyBuffer[((location >> 12) -1) >> 3] |= 0x80; }
            if((((location >> 12) -1) & 0x07)  == 1){ dummyBuffer[((location >> 12) -1) >> 3] |= 0x40; }
            if((((location >> 12) -1) & 0x07)  == 2){ dummyBuffer[((location >> 12) -1) >> 3] |= 0x20; }
            if((((location >> 12) -1) & 0x07)  == 3){ dummyBuffer[((location >> 12) -1) >> 3] |= 0x10; }
            if((((location >> 12) -1) & 0x07)  == 4){ dummyBuffer[((location >> 12) -1) >> 3] |= 0x08; }
            if((((location >> 12) -1) & 0x07)  == 5){ dummyBuffer[((location >> 12) -1) >> 3] |= 0x04; }
            if((((location >> 12) -1) & 0x07)  == 6){ dummyBuffer[((location >> 12) -1) >> 3] |= 0x02; }
            if((((location >> 12) -1) & 0x07)  == 7){ dummyBuffer[((location >> 12) -1) >> 3] |= 0x01; }
            blockDevice.writeBlock(3, dummyBuffer);
        }
    }

    private void clusterWriteUnCheck(int location, CachedBlockDevice blockDevice){ //클러스터 쓰고 표시
        byte[] dummyBuffer = new byte[(int) CLUSTERSPACESIZE];
        location = location -4;
        blockDevice.readBlock((location >> 12) -1 + 4, dummyBuffer);

        if(((location & 0x0FFF) & 0x07)  == 0){ dummyBuffer[(location & 0x0FFF) >> 3] &= ~0x80; }
        if(((location & 0x0FFF) & 0x07)  == 1){ dummyBuffer[(location & 0x0FFF) >> 3] &= ~0x40; }
        if(((location & 0x0FFF) & 0x07)  == 2){ dummyBuffer[(location & 0x0FFF) >> 3] &= ~0x20; }
        if(((location & 0x0FFF) & 0x07)  == 3){ dummyBuffer[(location & 0x0FFF) >> 3] &= ~0x10; }
        if(((location & 0x0FFF) & 0x07)  == 4){ dummyBuffer[(location & 0x0FFF) >> 3] &= ~0x08; }
        if(((location & 0x0FFF) & 0x07)  == 5){ dummyBuffer[(location & 0x0FFF) >> 3] &= ~0x04; }
        if(((location & 0x0FFF) & 0x07)  == 6){ dummyBuffer[(location & 0x0FFF) >> 3] &= ~0x02; }
        if(((location & 0x0FFF) & 0x07)  == 7){ dummyBuffer[(location & 0x0FFF) >> 3] &= ~0x01; }

        blockDevice.writeBlock((location >> 12) -1 + 4, dummyBuffer);

    }

    public byte[] binaryDataImport(String file) {

        FileInputStream fileinputstream = null;
        int numberBytes = 0;


        try {
            fileinputstream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            numberBytes = fileinputstream.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.d("xxxxxx", "numberBytes " + numberBytes);

        byte bytearray[] = new byte[numberBytes];

        try {
            fileinputstream.read(bytearray);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytearray;
    }

    public void incaseSearchTable(CachedBlockDevice blockDevice) {//파일테이블 메모리에 넣기
        int location = 0;
        endpoint = 0;
        int startaddress = 0;
        int tablesize = STRINGSIZE + STRINGLENSIZE + LOCATIONSIZE;
        int stringaddresslocation = STRINGSIZE + STRINGLENSIZE;



        blockDevice.readBlock(location, buffer);


        while (true) { //0번지 파일테이블 끝날때까지
            //for (int i = 0; i < 3; i++) {);

            Log.d("xxxxxx", " location " + location + "  startaddress " + startaddress);

            String dummystring = readStringToBinary(location, startaddress, STRINGSIZE, buffer, blockDevice); //문자
            if(dummystring.equals("0"))
                break;

            int dummyfilelen = readIntToBinary(location, startaddress + STRINGSIZE, STRINGLENSIZE, buffer, blockDevice); //파일내용길이
            int dummystringaddress = readIntToBinary(location, stringaddresslocation + startaddress, LOCATIONSIZE, buffer, blockDevice); //번지

            Log.d("xxxxxx", " dummystring " + dummystring + "  dummystringaddress " + dummystringaddress);


            //탐색테이블 생성
            searchtable[endpoint][0] = dummystring; //문자
            searchtable[endpoint][1] = Integer.toString(dummystringaddress);//번지
            searchtable[endpoint][2] = Integer.toString(location);//문자의 위치
            searchtable[endpoint][3] = Integer.toString(startaddress);//문자의 주소
            searchtable[endpoint][4] = Integer.toString(dummyfilelen);//문자의 주소
            endpoint++;


            startaddress = startaddress + tablesize;

            if (startaddress >= readIntToBinary(location, SPACELOCATION, SPACESIZE, buffer, blockDevice)) {
                location = readIntToBinary(location, NEXTLOCATION, LOCATIONSIZE, buffer, blockDevice);
                if(location == 0)
                    break;
                blockDevice.readBlock(location, buffer);
                startaddress = 0;
            }

        }

        //for (int i = 0; i < endpoint; i++) {
        //Log.d("xxxxxx", " 0] " + searchtable[i][0] + "  1] " + searchtable[i][1] + " 2] " + searchtable[i][2] + " 3] " + searchtable[i][3] + " 4] " + searchtable[i][4]);

        //}

    }

    public void dropfiletable(int last, int result, CachedBlockDevice blockDevice) {
        byte[] dummyBuffer = new byte[(int) CLUSTERSPACESIZE];
        while (true) {
            blockDevice.readBlock(last, dummyBuffer);
            result = readIntToBinary(last, NEXTLOCATION, LOCATIONSIZE, dummyBuffer, blockDevice);

            for (int j = SPACELOCATION; j < CLUSTERSPACESIZE; j++)
                dummyBuffer[j] = 0;
            blockDevice.writeBlock(last, dummyBuffer);
            if (result == 0)
                break;
            last = result;
        }
    }

    public void filetablecopy(CachedBlockDevice blockDevice) {//예상치 못하게 빠젓을 시 백업본 백업

        byte[] dummyBuffer = new byte[(int) CLUSTERSPACESIZE];
        byte[] resultlittleBuffer = new byte[(int) CLUSTERSPACESIZE];

        int givetablelocation = 1;
        int taketablelocation = 0;

        //0번째 0으로 다 만들어준다.
        dropfiletable(taketablelocation, taketablelocation, blockDevice);
        //서치테이블보고 주소들 0만들어준다.
        for (int i = 0; i < endpoint; i++)
            dropfiletable(Integer.parseInt(searchtable[i][1]), Integer.parseInt(searchtable[i][1]), blockDevice);


        int blocksize = STRINGSIZE + STRINGLENSIZE + LOCATIONSIZE;
        int addresslocation;
        int last = givetablelocation;

        blockDevice.readBlock(last, buffer);
        int result = readIntToBinary(last, NEXTLOCATION, LOCATIONSIZE, buffer, blockDevice);

        //실제 백업
        while (true) {
            boolean flag = true;
            //해당되는 주소들집합
            for (int i = 0; i < CLUSTERSPACESIZE; i += blocksize) {
                int location = readIntToBinary(last, i + STRINGSIZE + STRINGLENSIZE, LOCATIONSIZE, buffer, blockDevice);
                if (location == 0)
                    break;

                //0번지
                String dummystring = readStringToBinary(last, i, STRINGSIZE, buffer, blockDevice); //문자
                int dummyfilelen = readIntToBinary(last, i+STRINGSIZE, STRINGLENSIZE, buffer, blockDevice); //파일내용길이
                addresslocation = emptySpaceSearch(blockDevice);

                ////////////여기 dummystring////////////////
                pushAddress(0, STRINGSIZE, taketablelocation, dummystring, blockDevice); //문자
                pushAddress(dummyfilelen, STRINGLENSIZE, taketablelocation, "0", blockDevice); //길이
                int limit = pushAddress(addresslocation, LOCATIONSIZE, taketablelocation, "0", blockDevice); //번지

                if (limit >= SPACELOCATION) {
                    int dummySpace = emptySpaceSearch(blockDevice);
                    pushBinary(taketablelocation, dummySpace, LOCATIONSIZE, NEXTLOCATION, blockDevice); //마지막 위치 넣어주기
                    taketablelocation = dummySpace;
                    clusterWriteCheck(taketablelocation,blockDevice);//비어있는공간 찾고 표시
                }


                //주소번지
                int littlelast = location;


                blockDevice.readBlock(littlelast, resultlittleBuffer);
                int littleresult = readIntToBinary(littlelast, NEXTLOCATION, LOCATIONSIZE, resultlittleBuffer, blockDevice);
                while (true) {
                    blockDevice.readBlock(littlelast, dummyBuffer);
                    blockDevice.writeBlock(addresslocation, dummyBuffer);

                    clusterWriteCheck(addresslocation,blockDevice);//비어있는공간 찾고 표시

                    littlelast = littleresult;
                    if (littleresult == 0)
                        break;
                    blockDevice.readBlock(littlelast, resultlittleBuffer);
                    littleresult = readIntToBinary(littlelast, NEXTLOCATION, LOCATIONSIZE, resultlittleBuffer, blockDevice);

                    addresslocation = emptySpaceSearch(blockDevice);
                }
                flag = false;

            }
            if (flag)
                break;

            last = result;
            if (result == 0)
                break;
            blockDevice.readBlock(result, buffer);
            result = readIntToBinary(last, NEXTLOCATION, LOCATIONSIZE, buffer, blockDevice);
        }


        //바뀐 0번지로 서치테이블 완성
        incaseSearchTable(blockDevice);

        blockDevice.flush();

    }

    public void copytableinit(CachedBlockDevice blockDevice) {//원본데이터 복사 처음초기화
        int tablelocation = 1;
        int addresslocation;

        //0으로 만들기
        int blocksize = STRINGSIZE + STRINGLENSIZE + LOCATIONSIZE;
        int last = tablelocation;
        int result = tablelocation;

        while (true) {
            blockDevice.readBlock(last, buffer);
            boolean flag = true;
            //해당되는 주소들집합
            for (int i = 0; i < CLUSTERSPACESIZE; i += blocksize) {
                int location = readIntToBinary(last, i + STRINGSIZE + STRINGLENSIZE, LOCATIONSIZE, buffer, blockDevice); //번지
                if (location == 0)
                    break;

                //주소번지
                dropfiletable(location, location, blockDevice);

                flag = false;

            }
            if (flag)
                break;

            result = readIntToBinary(last, NEXTLOCATION, LOCATIONSIZE, buffer, blockDevice);

            for (int j = SPACELOCATION; j < CLUSTERSPACESIZE; j++)
                buffer[j] = 0;
            blockDevice.writeBlock(last, buffer);

            if (result == 0)
                break;
            last = result;
        }


        for (int i = 0; i < endpoint; i++) {
            addresslocation = emptySpaceSearch(blockDevice);
            int beforeaddresslocation = addresslocation;

            //주소
            result = Integer.parseInt(searchtable[i][1]);
            while (true) {
                blockDevice.readBlock(result, buffer);
                blockDevice.writeBlock(addresslocation, buffer);

                clusterWriteCheck(addresslocation,blockDevice);//비어있는공간 찾고 표시

                result = readIntToBinary(result, NEXTLOCATION, LOCATIONSIZE, buffer, blockDevice);
                if (result == 0)
                    break;

                int dummySpace = emptySpaceSearch(blockDevice);
                pushBinary(addresslocation, dummySpace, LOCATIONSIZE, NEXTLOCATION, blockDevice); //마지막 위치 넣어주기
                addresslocation = dummySpace;
                clusterWriteCheck(addresslocation,blockDevice);//비어있는공간 찾고 표시
            }

            //1번째
            ////////////여기 searchtable[i][0]////////////////
            pushAddress(0, STRINGSIZE, tablelocation, searchtable[i][0], blockDevice); //문자
            pushAddress(Integer.parseInt(searchtable[i][4]), STRINGLENSIZE, tablelocation, "0", blockDevice); //길이
            int limit = pushAddress(beforeaddresslocation, LOCATIONSIZE, tablelocation, "0", blockDevice); //번지

            if (limit >= SPACELOCATION) {
                int dummySpace = emptySpaceSearch(blockDevice);
                pushBinary(tablelocation, dummySpace, LOCATIONSIZE, NEXTLOCATION, blockDevice); //마지막 위치 넣어주기
                tablelocation = dummySpace;
                clusterWriteCheck(tablelocation,blockDevice);//비어있는공간 찾고 표시
            }
        }

        blockDevice.flush();
    }

    public void fileInit(String content[], CachedBlockDevice blockDevice) {//파일 처음 초기화
        int allAddressSpace = 0;

        for (int i = 1; i <= 2; i++) { //content.length
            byte[] bytearray;
            String file = "/storage/emulated/0/DCIM/Camera/" + i + ".jpg";
            bytearray = binaryDataImport(file);
            int emptyCoreAdressSpace = emptySpaceSearch(blockDevice);

            //0번째
            String stringContent = content[i];
            if (content[i].length() > 78)
                stringContent = content[i].substring(0, 78);

            pushAddress(0, STRINGSIZE, allAddressSpace, stringContent, blockDevice); //문자
            pushAddress(bytearray.length, STRINGLENSIZE, allAddressSpace, "0", blockDevice); //길이
            int limit = pushAddress(emptyCoreAdressSpace, LOCATIONSIZE, allAddressSpace, "0", blockDevice); //번지

            clusterWriteCheck(emptyCoreAdressSpace,blockDevice);//비어있는공간 찾고 표시


            if (limit >= SPACELOCATION) {
                int dummySpace = emptySpaceSearch(blockDevice);
                pushBinary(allAddressSpace, dummySpace, LOCATIONSIZE, NEXTLOCATION, blockDevice); //마지막 위치 넣어주기
                allAddressSpace = dummySpace;
                clusterWriteCheck(allAddressSpace,blockDevice);//비어있는공간 찾고 표시
            }

            int cnt = 0;

            for (int j = 0; j <= bytearray.length / CLUSTERSPACESIZE; j++) { // size[i] -> bytearray.length
                int emptyCoreSpace = emptySpaceSearch(blockDevice);

                for (int k = 0; k < CLUSTERSPACESIZE; k++) {
                    if (cnt < bytearray.length)
                        buffer[k] = bytearray[cnt++];//실제 내용 넣기
                }
                blockDevice.writeBlock(emptyCoreSpace, buffer);

                clusterWriteCheck(emptyCoreSpace,blockDevice);//비어있는공간 찾고 표시

                //주소들 값 넣기
                limit = pushAddress(emptyCoreSpace, LOCATIONSIZE, emptyCoreAdressSpace, "0", blockDevice);

                if (limit >= SPACELOCATION) {
                    int dummySpace = emptySpaceSearch(blockDevice);
                    pushBinary(emptyCoreAdressSpace, dummySpace, LOCATIONSIZE, NEXTLOCATION, blockDevice);
                    emptyCoreAdressSpace = dummySpace;
                    clusterWriteCheck(emptyCoreAdressSpace,blockDevice);//비어있는공간 찾고 표시
                }

            }
            blockDevice.flush();
        }


    }

    public String readStringToBinary(int location, int address, int currentlocationsize, byte[] resultbuffer, CachedBlockDevice blockDevice) {
        //byte -> string

        int cnt = 0;

        //blockDevice.readBlock(location, buffer);

        for (int i = address; i < address + currentlocationsize; i++) {
            if (resultbuffer[i] == (byte) 0)
                break;
            cnt++;
        }

        if (cnt ==  0)
            return "0";

        byte[] binaryByte = new byte[cnt];
        int count = 0;
        for (int i = address; i < address + cnt; i++) {
            binaryByte[count++] = resultbuffer[i];
        }

        String binary = new String(binaryByte);



        return binary;
    }

    public int readIntToBinary(int location, int address, int currentlocationsize, byte[] resultbuffer, CachedBlockDevice blockDevice ) {
        //byte -> int
        int result = 0;
        //blockDevice.readBlock(location, buffer);
        for (int i = 0; i < currentlocationsize; i++) {
            result += ((resultbuffer[address + currentlocationsize - i - 1] & 0xFF) << (i * 8));
        }
        return result;
    }

    public void pushStringBinary(int location, String conversion, int currentlocationsize, int address, CachedBlockDevice blockDevice) { //넣어줄 위치 , 바꿔줄 숫자, 크기, 넣어줄 위치안에주소
        //int -> string
        byte[] dummyBuffer = new byte[(int) CLUSTERSPACESIZE];
        blockDevice.readBlock(location, dummyBuffer);
        if (conversion.equals("0")){
            for (int i = 0; i < currentlocationsize; i++)
                dummyBuffer[address + i] = 0;
            blockDevice.writeBlock(location, dummyBuffer);
        }
        else {

            byte[] binaryByte = conversion.getBytes();
            for (int i = 0; i < currentlocationsize; i++) {
                if (i < binaryByte.length)
                    dummyBuffer[address + i] = binaryByte[i];
                else
                    dummyBuffer[address + i] = (byte) 0x00;
            }

            blockDevice.writeBlock(location, dummyBuffer);
        }
    }

    public void pushBinary(int location, int conversion, int currentlocationsize, int address, CachedBlockDevice blockDevice) { //넣어줄 위치 , 바꿔줄 숫자, 크기, 넣어줄 위치안에주소
        //int -> byte
        byte[] dummyBuffer = new byte[(int) CLUSTERSPACESIZE];
        blockDevice.readBlock(location, dummyBuffer);
        if (conversion == 0) {
            for (int i = 0; i < currentlocationsize; i++)
                dummyBuffer[address + i] = 0;
            blockDevice.writeBlock(location, dummyBuffer);
        }
        else {
            for (int i = 0; i < currentlocationsize; i++)
                dummyBuffer[address + currentlocationsize - i - 1] = (byte) (conversion >> 8 * i);
            blockDevice.writeBlock(location, dummyBuffer);
        }
    }

    public int pushAddress(int conversion, int currentlocationsize, int location, String stringconversion, CachedBlockDevice blockDevice) {//위치에 계산하여 넣어주기
        byte[] dummyBuffer = new byte[(int) CLUSTERSPACESIZE];
        blockDevice.readBlock(location, dummyBuffer);
        int address = readIntToBinary(location, SPACELOCATION, SPACESIZE, dummyBuffer, blockDevice);

        if (stringconversion.equalsIgnoreCase("0"))
            pushBinary(location, conversion, currentlocationsize, address, blockDevice);
        else
            pushStringBinary(location, stringconversion, currentlocationsize, address, blockDevice);


        pushBinary(location, address + currentlocationsize, SPACESIZE, SPACELOCATION, blockDevice);

        return address + currentlocationsize;
    }

    public void addElementPushCopy(CachedBlockDevice blockDevice) {
        //0번지 백업
        int last = lastHaveSpaceReturn(1, blockDevice);
        String dummystring = searchtable[endpoint - 1][0];
        int dummyfilelen = Integer.parseInt(searchtable[endpoint - 1][4]);
        int dummystringaddress = Integer.parseInt(searchtable[endpoint - 1][1]); //마지막 번째 번지

        int addresslocation = emptySpaceSearch(blockDevice);

        ////////////여기 dummystring////////////////
        pushAddress(0, STRINGSIZE, last, dummystring, blockDevice); //문자
        pushAddress(dummyfilelen, STRINGLENSIZE, last, "0", blockDevice); //길이
        int limit = pushAddress(addresslocation, LOCATIONSIZE, last, "0", blockDevice); //번지

        if (limit >= SPACELOCATION) {
            int dummySpace = emptySpaceSearch(blockDevice);
            pushBinary(last, dummySpace, LOCATIONSIZE, NEXTLOCATION, blockDevice); //마지막 위치 넣어주기
            last = dummySpace;
            clusterWriteCheck(last,blockDevice);//비어있는공간 찾고 표시
        }


        //주소들 백업
        int result = dummystringaddress;
        while (true) {
            blockDevice.readBlock(result, buffer);
            blockDevice.writeBlock(addresslocation, buffer);

            clusterWriteCheck(addresslocation,blockDevice);//비어있는공간 찾고 표시

            result = readIntToBinary(result, NEXTLOCATION, LOCATIONSIZE, buffer, blockDevice);
            if (result == 0)
                break;

            int dummySpace = emptySpaceSearch(blockDevice);
            pushBinary(addresslocation, dummySpace, LOCATIONSIZE, NEXTLOCATION, blockDevice); //마지막 위치 넣어주기
            addresslocation = dummySpace;
            clusterWriteCheck(addresslocation,blockDevice);//비어있는공간 찾고 표시
        }

        //완료 표시
        blockDevice.readBlock(1, buffer);
        buffer[ISCOMPLETELOCATION] = 1;
        blockDevice.writeBlock(1, buffer);

        blockDevice.flush();
    }

    public void addElementPush(String content, CachedBlockDevice blockDevice) {//새로운 입력

        //넣기전에 표시
        blockDevice.readBlock(0, buffer);
        buffer[ISCOMPLETELOCATION] = 0;
        blockDevice.writeBlock(0, buffer);

        blockDevice.readBlock(1, buffer);
        buffer[ISCOMPLETELOCATION] = 0;
        blockDevice.writeBlock(1, buffer);

        byte[] bytearray;
        String file = "/storage/emulated/0/DCIM/Camera/" + content;
        bytearray = binaryDataImport(file);

        int emptyCoreAdressSpace = emptySpaceSearch(blockDevice);

        // '0'번지 정렬 새롭게 빌드
        int last = lastReturn(0, blockDevice);

        String stringContent = content;
        if (content.length() > 78)
            stringContent = content.substring(0, 78);

        ////////////여기 stringContent////////////////
        pushAddress(0, STRINGSIZE, last, stringContent, blockDevice); //문자
        pushAddress(bytearray.length, STRINGLENSIZE, last, "0", blockDevice); //길이
        int limit = pushAddress(emptyCoreAdressSpace, LOCATIONSIZE, last, "0", blockDevice);//번지

        clusterWriteCheck(emptyCoreAdressSpace,blockDevice);//비어있는공간 찾고 표시


        int fristemptyCoreAdressSpace = emptyCoreAdressSpace;

        if (limit >= SPACELOCATION) {
            int dummySpace = emptySpaceSearch(blockDevice);
            pushBinary(last, dummySpace, LOCATIONSIZE, NEXTLOCATION, blockDevice); //마지막 위치 넣어주기
            clusterWriteCheck(dummySpace,blockDevice);//비어있는공간 찾고 표시
        }

        int cnt = 0;
        for (int i = 0; i <= bytearray.length / CLUSTERSPACESIZE; i++) {
            int emptyCoreSpace = emptySpaceSearch(blockDevice);

            for (int j = 0; j < CLUSTERSPACESIZE; j++) {
                if (cnt < bytearray.length)
                    buffer[j] = bytearray[cnt++];   //실제 내용 넣기
            }
            blockDevice.writeBlock(emptyCoreSpace, buffer);

            clusterWriteCheck(emptyCoreSpace,blockDevice);//비어있는공간 찾고 표시


            //주소들 값 넣기 (원본)
            limit = pushAddress(emptyCoreSpace, LOCATIONSIZE, emptyCoreAdressSpace, "0", blockDevice);
            if (limit >= SPACELOCATION) {
                int dummySpace = emptySpaceSearch(blockDevice);
                pushBinary(emptyCoreAdressSpace, dummySpace, LOCATIONSIZE, NEXTLOCATION, blockDevice);
                emptyCoreAdressSpace = dummySpace;
                clusterWriteCheck(emptyCoreAdressSpace,blockDevice);//비어있는공간 찾고 표시
            }


        }

        //탐색테이블 생성
        searchtable[endpoint][0] = stringContent; //문자
        searchtable[endpoint][1] = Integer.toString(fristemptyCoreAdressSpace);//번지
        searchtable[endpoint][2] = Integer.toString(last);//문자의 위치
        searchtable[endpoint][3] = Integer.toString(limit - LOCATIONSIZE - STRINGLENSIZE - STRINGSIZE);//문자의 주소
        searchtable[endpoint][4] = Integer.toString(bytearray.length);//문자의 주소
        endpoint++;

        //완료 표시
        blockDevice.readBlock(0, buffer);
        buffer[ISCOMPLETELOCATION] = 1;
        blockDevice.writeBlock(0, buffer);

        blockDevice.flush();

    }

    public int lastReturn(int last, CachedBlockDevice blockDevice) { //값없어도 되는 마지막 값 호출
        byte[] dummyBuffer = new byte[(int) CLUSTERSPACESIZE];
        blockDevice.readBlock(last, dummyBuffer);
        int result = readIntToBinary(last, NEXTLOCATION, LOCATIONSIZE, dummyBuffer, blockDevice);
        while (result != 0) {
            last = result;
            blockDevice.readBlock(last, dummyBuffer);
            result = readIntToBinary(last, NEXTLOCATION, LOCATIONSIZE, dummyBuffer, blockDevice);
        }
        return last;
    }

    public int lastHaveSpaceReturn(int last, CachedBlockDevice blockDevice) { //값이 있는 마지막 값 호출
        byte[] dummyBuffer = new byte[(int) CLUSTERSPACESIZE];
        int sublast = last;
        blockDevice.readBlock(last, dummyBuffer);
        int result = readIntToBinary(last, NEXTLOCATION, LOCATIONSIZE, dummyBuffer, blockDevice);
        while (result != 0) {
            last = result;
            sublast = result;
            blockDevice.readBlock(last, dummyBuffer);
            result = readIntToBinary(last, NEXTLOCATION, LOCATIONSIZE, dummyBuffer, blockDevice);
        }

        blockDevice.readBlock(last, dummyBuffer);
        int lastResult = readIntToBinary(last, SPACELOCATION, SPACESIZE, dummyBuffer, blockDevice);
        if (lastResult == 0)
            return sublast;

        return last;
    }

    public void deleteCopy(String delString, CachedBlockDevice blockDevice) {
        int blocksize = LOCATIONSIZE + STRINGLENSIZE + STRINGSIZE;
        int last = 1;
        int result;

        int dummystringaddress = 0;

        int location = 0;
        int address = 0;
        blockDevice.readBlock(last, buffer);

        byte[] deleteBuffer = new byte[(int) CLUSTERSPACESIZE];
        for (int i = 0; i < CLUSTERSPACESIZE; i++)
            deleteBuffer[i] = 0;

        //검색
        while (true) {
            for (int i = 0; i < CLUSTERSPACESIZE; i += blocksize) {
                String dummystring = readStringToBinary(last, i, STRINGSIZE, buffer, blockDevice);
                if (dummystring.equalsIgnoreCase(delString)) {
                    location = last;
                    address = i;
                    break;
                }
            }
            result = readIntToBinary(last, NEXTLOCATION, LOCATIONSIZE, buffer, blockDevice);
            if (result == 0)
                break;
            last = result;
            blockDevice.readBlock(last, buffer);
        }

        //주소값 삭제
        while (dummystringaddress != 0) {
            blockDevice.readBlock(dummystringaddress, buffer);
            int dummy = readIntToBinary(dummystringaddress, NEXTLOCATION, LOCATIONSIZE, buffer, blockDevice);

            blockDevice.writeBlock(dummystringaddress, deleteBuffer);
            clusterWriteUnCheck(dummystringaddress,blockDevice); //비어있는 체크된것 해제하기
            dummystringaddress = dummy;
        }

        //0번지 삭제
        last = lastHaveSpaceReturn(1, blockDevice);
        blockDevice.readBlock(last, buffer);
        int lastResult = readIntToBinary(last, SPACELOCATION, SPACESIZE, buffer, blockDevice);

        //변경할값 변수에 넣어주기
        String changeString = readStringToBinary(last, lastResult - (STRINGSIZE + STRINGLENSIZE + LOCATIONSIZE), STRINGSIZE, buffer, blockDevice);
        int changelen = readIntToBinary(last, lastResult - (LOCATIONSIZE + STRINGLENSIZE), STRINGLENSIZE, buffer, blockDevice);
        int changelocation = readIntToBinary(last, lastResult - LOCATIONSIZE, LOCATIONSIZE, buffer, blockDevice);


        for (int j = lastResult - (STRINGSIZE + STRINGLENSIZE + LOCATIONSIZE); j < lastResult; j++)
            buffer[j] = 0;
        blockDevice.writeBlock(last, buffer);

        pushBinary(last, lastResult - (STRINGSIZE + STRINGLENSIZE + LOCATIONSIZE), SPACESIZE, SPACELOCATION, blockDevice); //지우고 SPACELOCATION번쨰값 그전으로 변경

        ////////////여기 changeString////////////////
        pushStringBinary(location, changeString, STRINGSIZE, address, blockDevice);//이름
        pushBinary(location, changelen, STRINGLENSIZE, address + STRINGSIZE, blockDevice);//길이
        pushBinary(location, changelocation, LOCATIONSIZE, address + STRINGSIZE + STRINGLENSIZE, blockDevice);//번지

        //완료 표시
        blockDevice.readBlock(1, buffer);
        buffer[ISCOMPLETELOCATION] = 1;
        blockDevice.writeBlock(1, buffer);

        blockDevice.flush();
    }

    public void delete(String delString, CachedBlockDevice blockDevice) {

        //넣기전에 표시
        blockDevice.readBlock(0, buffer);
        buffer[ISCOMPLETELOCATION] = 0;
        blockDevice.writeBlock(0, buffer);

        blockDevice.readBlock(1, buffer);
        buffer[ISCOMPLETELOCATION] = 0;
        blockDevice.writeBlock(1, buffer);

        byte[] deleteBuffer = new byte[(int) CLUSTERSPACESIZE];
        for (int i = 0; i < CLUSTERSPACESIZE; i++)
            deleteBuffer[i] = 0;

        int[] result = stringSearch(delString);
        if (result[0] == -1)
            Log.d("xxxxxx", "값이1 잘못들어왔습니다");
            //Toast.makeText(this, "값이 잘못들어왔습니다", Toast.LENGTH_SHORT).show();

        else {
            int resultstringaddress = result[0];
            //int result = readIntToBinary(resultAdress[0],resultAdress[1]+80,LOCATIONSIZE);


            blockDevice.readBlock(resultstringaddress, buffer);
            Log.d("xxxxxx", "1 "+buffer);

            //실제파일과 주소값 삭제
            while (resultstringaddress != 0) {
                int endlocation = readIntToBinary(resultstringaddress, SPACELOCATION, SPACESIZE, buffer, blockDevice);
                for (int i = 0; i < endlocation; i += LOCATIONSIZE) {
                    int deleteSpace = readIntToBinary(resultstringaddress, i, LOCATIONSIZE, buffer, blockDevice);

                    blockDevice.writeBlock(deleteSpace, deleteBuffer);//실제파일 지우기
                    clusterWriteUnCheck(deleteSpace,blockDevice); //비어있는 체크된것 해제하기
                }
                int dummy = readIntToBinary(resultstringaddress, NEXTLOCATION, LOCATIONSIZE, buffer, blockDevice);

                blockDevice.writeBlock(resultstringaddress, deleteBuffer);
                clusterWriteUnCheck(resultstringaddress,blockDevice); //비어있는 체크된것 해제하기

                resultstringaddress = dummy;
                blockDevice.readBlock(resultstringaddress, buffer);
            }


            //0번째 값
            int last = lastHaveSpaceReturn(0, blockDevice);
            blockDevice.readBlock(last, buffer);
            int lastResult = readIntToBinary(last, SPACELOCATION, SPACESIZE, buffer, blockDevice);


            //변경할값 변수에 넣어주기
            String changeString = readStringToBinary(last, lastResult - (STRINGSIZE + STRINGLENSIZE + LOCATIONSIZE), STRINGSIZE, buffer, blockDevice);
            int changelen = readIntToBinary(last, lastResult - (LOCATIONSIZE + STRINGLENSIZE), STRINGLENSIZE, buffer, blockDevice);
            int changelocation = readIntToBinary(last, lastResult - LOCATIONSIZE, LOCATIONSIZE, buffer, blockDevice);

            Log.d("xxxxxx", "changeString " + changeString);

            //0번지의 마지막값 지우기
            for (int j = lastResult - (STRINGSIZE + STRINGLENSIZE + LOCATIONSIZE); j < lastResult; j++)
                buffer[j] = 0;
            blockDevice.writeBlock(last, buffer);

            //탐색테이블 삭제
            searchtable[result[1]][0] = searchtable[endpoint - 1][0]; // 마지막 문자열
            searchtable[result[1]][1] = searchtable[endpoint - 1][1]; // 마지막 번지
            searchtable[result[1]][2] = searchtable[endpoint - 1][2]; // 마지막 위치
            searchtable[result[1]][3] = searchtable[endpoint - 1][3]; // 마지막 주소
            searchtable[result[1]][4] = searchtable[endpoint - 1][4]; // 파일 길이
            searchtable[endpoint - 1][0] = "0";
            searchtable[endpoint - 1][1] = "0";
            searchtable[endpoint - 1][2] = "0";
            searchtable[endpoint - 1][3] = "0";
            searchtable[endpoint - 1][4] = "0";
            endpoint--;

            Log.d("xxxxxx", "sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss");
            pushBinary(last, lastResult - (STRINGSIZE + STRINGLENSIZE + LOCATIONSIZE), SPACESIZE, SPACELOCATION, blockDevice); //지우고 SPACELOCATION번쨰값 그전으로 변



            if (changeString.equals(delString)) { //마지막값과 찾는값과 같으면 끝낸다 (1개남은상태)
                Log.d("xxxxxx", "");
            } else {
                //변경할값 실제 넣어주기
                ////////////여기 changeString////////////////
                pushStringBinary(result[2], changeString, STRINGSIZE, result[3], blockDevice);//이름
                pushBinary(result[2], changelen, STRINGLENSIZE, result[3] + STRINGSIZE, blockDevice);//길이
                pushBinary(result[2], changelocation, LOCATIONSIZE, result[3] + STRINGSIZE + STRINGLENSIZE, blockDevice);//번지
            }
            //완료 표시
            blockDevice.readBlock(0, buffer);
            buffer[ISCOMPLETELOCATION] = 1;
            blockDevice.writeBlock(0, buffer);

            blockDevice.flush();

        }


    }

    public int[] stringSearch(String delString) {
        boolean flag = true;
        int[] result = new int[5];


        for (int i = 0; i < endpoint; i++) {
            //Log.d("xxxxxx", "delString " + delString);
            //Log.d("xxxxxx", "searchtable[i][0] " + searchtable[i][0]);
            if (searchtable[i][0].equals(delString)) {
                result[0] = Integer.parseInt(searchtable[i][1]); //번지수
                result[1] = i; //탐색테이블 문자열 위치
                result[2] = Integer.parseInt(searchtable[i][2]); //위치
                result[3] = Integer.parseInt(searchtable[i][3]); //주소
                result[4] = Integer.parseInt(searchtable[i][4]); //주소
                flag = false;
                break;
            }
        }
        if (flag)
            result[0] = -1;
        return result;

    }

    /*
    public int[] binarySearch(String delString) { // 해당문자열의 위치/어디레스 리턴

        int[] result = new int[2];
        int location = 0,address = -(STRINGSIZE + STRINGLENSIZE + LOCATIONSIZE);
        String cntString = null;

        while(true){
            address = address + (STRINGSIZE + STRINGLENSIZE + LOCATIONSIZE);
            if(address + (STRINGSIZE + STRINGLENSIZE + LOCATIONSIZE) >= SPACELOCATION){
                location = readIntToBinary(location,NEXTLOCATION,LOCATIONSIZE);
                if(location == 0){//값이 잘못들어왔을때
                    location = -1;
                    break;
                }
                address = 0;
            }
            cntString = readStringToBinary(location,address,STRINGSIZE);

            //문자 비교
            char delchar[] = delString.toCharArray();
            char cntchar[] = cntString.toCharArray();
            int cnt = 0;
            for(int i=0;i<delchar.length;i++){
                if(cntchar[i] != delchar[i])
                    break;
                cnt ++;
            }

            if(cnt == delchar.length)
                break;
        }


        result[0] = location;
        result[1] = address;


        return result;
    }
    */
    public void printAllBlock(int last, CachedBlockDevice blockDevice) { //해당 값 출력

        blockDevice.readBlock(last, buffer);
        int result = readIntToBinary(last, NEXTLOCATION, LOCATIONSIZE, buffer, blockDevice);
		/*
		for(int i=0;i<512;i++){
			Log.d("xxxxxx", i + " " + BLOCK[1][i]);

		}
		*/
        while (true) {

            //0번지
            Log.d("xxxxxx", readStringToBinary(last, 0, STRINGSIZE, buffer, blockDevice) + " " + readIntToBinary(last, 77, STRINGLENSIZE, buffer, blockDevice) + " " + readIntToBinary(last, 80, LOCATIONSIZE, buffer, blockDevice));
            Log.d("xxxxxx", readStringToBinary(last, 84, STRINGSIZE, buffer, blockDevice) + " " + readIntToBinary(last, 161, STRINGLENSIZE, buffer, blockDevice) + " " + readIntToBinary(last, 164, LOCATIONSIZE, buffer, blockDevice));
            //Log.d("xxxxxx", readStringToBinary(last, 168, STRINGSIZE, blockDevice) + " " + readIntToBinary(last, 246, STRINGLENSIZE, blockDevice) + " " + readIntToBinary(last, 248, LOCATIONSIZE, blockDevice));
            //Log.d("xxxxxx", readStringToBinary(last,252,STRINGSIZE) + " " + readIntToBinary(last,330,STRINGLENSIZE) + " " + readIntToBinary(last,332,LOCATIONSIZE));
            //Log.d("xxxxxx", readStringToBinary(last,336,STRINGSIZE) + " " + readIntToBinary(last,414,STRINGLENSIZE) + " " + readIntToBinary(last,416,LOCATIONSIZE));
            //Log.d("xxxxxx", readStringToBinary(last,420,STRINGSIZE) + " " + readIntToBinary(last,498,STRINGLENSIZE) + " " + readIntToBinary(last,500,LOCATIONSIZE));

            Log.d("xxxxxx", readIntToBinary(last, SPACELOCATION, SPACESIZE, buffer, blockDevice) + "");
            Log.d("xxxxxx", readIntToBinary(last, NEXTLOCATION, LOCATIONSIZE, buffer, blockDevice) + "");


            if (result == 0)
                break;

            last = result;
            blockDevice.readBlock(last, buffer);
            result = readIntToBinary(last, NEXTLOCATION, LOCATIONSIZE, buffer, blockDevice);

        }

    }
    public static FileSystem getInstance(){
        if(Instance == null)
            Instance = new FileSystem();
        return Instance;
    }
}


