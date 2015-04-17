package com.example.cds.eattle_prototype_2;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


public class Password extends ActionBarActivity {

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
                passwordInit();
            }

        }
    }

    public void passwordInit(){
        count = 0;
        input = "";
        firstDot.setBackgroundResource(R.mipmap.dotbefore);
        secondDot.setBackgroundResource(R.mipmap.dotbefore);
        thirdDot.setBackgroundResource(R.mipmap.dotbefore);
        fourthDot.setBackgroundResource(R.mipmap.dotbefore);
    }
    public void changeCircle(){
        switch(count){
            case 1:
                firstDot.setBackgroundResource(R.mipmap.dotafter);
                break;
            case 2:
                secondDot.setBackgroundResource(R.mipmap.dotafter);
                break;
            case 3:
                thirdDot.setBackgroundResource(R.mipmap.dotafter);
                break;
            case 4:
                fourthDot.setBackgroundResource(R.mipmap.dotafter);
                break;
            default:
                break;

        }
    }

    public void onPasswordClick(View v){
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
            case R.id.sharp:
                input += "#";
                break;
            default:
                break;
        }

        count++;
        changeCircle();
        check();
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
