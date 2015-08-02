package com.eattle.phoket;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FolderManage {
    public static final String TAG="FolderManage";

    //디렉토리(폴더) 생성
    public static File makeDirectory(String dir_path){
        File dir = new File(dir_path);
        if (!dir.exists())
        {
            dir.mkdirs();
        }else{
        }

        return dir;
    }

    //파일 생성
    public static File makeFile(File dir , String file_path){
        File file = null;
        boolean isSuccess = false;
        if(dir.isDirectory()){
            file = new File(file_path);
            if(file!=null&&!file.exists()){
                try {
                    isSuccess = file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{
                }
            }else{
            }
        }
        return file;
    }

    //디렉토리(폴더),파일 절대 경로 얻어오기
    public static String getAbsolutePath(File file){
        return ""+file.getAbsolutePath();
    }

    //디렉토리(폴더),파일 삭제하기
    public static boolean deleteFile(File file){
        boolean result;
        if(file!=null&&file.exists()){
            File[] childFileList = file.listFiles();
            if(childFileList != null) {//하위 폴더 및 하위 파일이 있으면
                for (File childFile : childFileList) {
                    if (childFile.isDirectory()) {
                        deleteFile(new File(childFile.getAbsolutePath()));    //하위 디렉토리
                    } else {
                        childFile.delete();    //하위 파일
                    }
                }
            }


            file.delete();    //root 삭제

            result = true;
        }else{
            result = false;
        }
        return result;
    }

    //파일 여부 체크하기
    public static boolean isFile(File file){
        boolean result;
        if(file!=null&&file.exists()&&file.isFile()){
            result=true;
        }else{
            result=false;
        }
        return result;
    }

    //디렉토리 여부 체크하기
    public static boolean isDirectory(File dir){
        boolean result;
        if(dir!=null&&dir.isDirectory()){
            result=true;
        }else{
            result=false;
        }
        return result;
    }

    //파일 존재 여부 확인하기
    public static boolean isFileExist(File file){
        boolean result;
        if(file!=null&&file.exists()){
            result=true;
        }else{
            result=false;
        }
        return result;
    }

    //파일 이름 바꾸기
    public static String reNameFile(File file , File new_name){
        String result;
        if(file!=null&&file.exists()){
            if(!new_name.exists()) {//이미 존재하지 않는 폴더 이름이라면
                file.renameTo(new_name);
                result = new_name.getName();
            }
            else{//이미 존재하는 폴더 이름이라면
                //폴더 이름에 적절한 숫자를 붙인다.
                int i=2;
                String path = new_name.getAbsolutePath();
                File temp = null;
                while(true){//new_name이 이름에 포함된 디렉토리의 개수를 파악한다.
                    String _path = path+"-"+i+"번째";
                    temp = new File(_path);
                    if(!temp.exists()) {
                        file.renameTo(temp);
                        break;
                    }
                    i++;
                }
                result=temp.getName();

            }
        }else{
            result="";
        }
        return result;
    }

    //디렉토리(폴더) 내용 불러오기
    public static String[] getList(File dir){
        if(dir!=null&&dir.exists())
            return dir.list();
        return null;
    }

    //파일에 내용 쓰기
    public static boolean writeFile(File file , byte[] file_content){
        boolean result;
        FileOutputStream fos;
        if(file!=null&&file.exists()&&file_content!=null){
            try {
                fos = new FileOutputStream(file);
                try {
                    fos.write(file_content);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            result = true;
        }else{
            result = false;
        }
        return result;
    }

    //파일 읽어오기
    public static void readFile(File file){
        int readcount=0;
        if(file!=null&&file.exists()){
            try {
                FileInputStream fis = new FileInputStream(file);
                readcount = (int)file.length();
                byte[] buffer = new byte[readcount];
                fis.read(buffer);
                for(int i=0 ; i<file.length();i++){
                }
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //파일 복사
    public static boolean copyFile(File file , String save_file){
        //file을 save_file로 복사
        boolean result;
        if(file!=null&&file.exists()){
            try {
                FileInputStream fis = new FileInputStream(file);
                FileOutputStream newfos = new FileOutputStream(save_file);
                int readcount=0;
                byte[] buffer = new byte[1024];
                while((readcount = fis.read(buffer,0,1024))!= -1){
                    newfos.write(buffer,0,readcount);
                }
                newfos.close();
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            result = true;
        }else{
            result = false;
        }
        return result;
    }
}
