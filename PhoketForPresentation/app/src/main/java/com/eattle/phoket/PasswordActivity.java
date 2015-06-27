package com.eattle.phoket;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


public class PasswordActivity extends ActionBarActivity {

    String input;
    String rightPassword="1234";//USB에 저장되어 있는 비밀번호
    int count;//비밀번호는 4자리 숫자
    ImageView firstDot;
    ImageView secondDot;
    ImageView thirdDot;
    ImageView fourthDot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (CONSTANT.PASSWORD != 0) {//비밀 번호 해제 안됬으면
            //password 창을 띄운다
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }


        CONSTANT.PASSWORD_TRIAL = 5;
        setContentView(R.layout.activity_password);
        firstDot = (ImageView)findViewById(R.id.firstDot);
        secondDot = (ImageView)findViewById(R.id.secondDot);
        thirdDot = (ImageView)findViewById(R.id.thirdDot);
        fourthDot = (ImageView)findViewById(R.id.fourthDot);
        passwordInit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        passwordInit();
    }

    public void check(){
        if(count == 4){
            if(rightPassword.equals(input)){//비밀번호가 맞으면
                CONSTANT.PASSWORD = 1;
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
            else{//비밀번호가 틀리면
                passwordError();
                passwordInit();
            }

        }
    }

    public void passwordInit(){
        count = 0;
        input = "";
        firstDot.setBackgroundResource(R.mipmap.dot_before);
        secondDot.setBackgroundResource(R.mipmap.dot_before);
        thirdDot.setBackgroundResource(R.mipmap.dot_before);
        fourthDot.setBackgroundResource(R.mipmap.dot_before);
    }
    public void changeCircle(){
        switch(count){
            case 0:
                firstDot.setBackgroundResource(R.mipmap.dot_before);
                secondDot.setBackgroundResource(R.mipmap.dot_before);
                thirdDot.setBackgroundResource(R.mipmap.dot_before);
                fourthDot.setBackgroundResource(R.mipmap.dot_before);
                break;
            case 1:
                firstDot.setBackgroundResource(R.mipmap.dot_after);
                secondDot.setBackgroundResource(R.mipmap.dot_before);
                thirdDot.setBackgroundResource(R.mipmap.dot_before);
                fourthDot.setBackgroundResource(R.mipmap.dot_before);
                break;
            case 2:
                firstDot.setBackgroundResource(R.mipmap.dot_after);
                secondDot.setBackgroundResource(R.mipmap.dot_after);
                thirdDot.setBackgroundResource(R.mipmap.dot_before);
                fourthDot.setBackgroundResource(R.mipmap.dot_before);
                break;
            case 3:
                firstDot.setBackgroundResource(R.mipmap.dot_after);
                secondDot.setBackgroundResource(R.mipmap.dot_after);
                thirdDot.setBackgroundResource(R.mipmap.dot_after);
                fourthDot.setBackgroundResource(R.mipmap.dot_before);
                break;
            case 4:
                firstDot.setBackgroundResource(R.mipmap.dot_after);
                secondDot.setBackgroundResource(R.mipmap.dot_after);
                thirdDot.setBackgroundResource(R.mipmap.dot_after);
                fourthDot.setBackgroundResource(R.mipmap.dot_after);
                break;
            default:
                break;

        }
    }

    public void onPasswordClick(View v){
        int delete=0;
        switch(v.getId()){
            case R.id.one:
                input += "1";
                break;
            case R.id.two:
                input += "2";
                break;
            case R.id.three:
                input += "3";
                break;
            case R.id.four:
                input += "4";
                break;
            case R.id.five:
                input += "5";
                break;
            case R.id.six:
                input += "6";
                break;
            case R.id.seven:
                input += "7";
                break;
            case R.id.eight:
                input += "8";
                break;
            case R.id.nine:
                input += "9";
                break;
            case R.id.zero:
                input += "0";
                break;
            case R.id.star:
                input += "*";
                break;
            case R.id.deletePassword:
                delete = 1;
                if(input.length() > 0) {
                    input = input.substring(0, input.length() - 1);
                    count--;
                }
                break;
            default:
                break;
        }
        if(delete == 0)
            count++;
        changeCircle();
        check();
    }

    public void passwordError(){//비밀번호가 틀렸을 경우
        CONSTANT.PASSWORD_TRIAL--;
        if(CONSTANT.PASSWORD_TRIAL == 0){//비밀번호 시도 횟수 초과시
            moveTaskToBack(true);
            finish();//앱을 종료한다
        }
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle("열쇠 오류");
        d.setMessage("비밀번호 해제 시도 \n"+CONSTANT.PASSWORD_TRIAL+"회 남았습니다!");
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        break;

                }
            }
        };
        d.setPositiveButton("Yes",l);
        d.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_password, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
