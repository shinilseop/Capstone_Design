package com.example.first;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
public class uviActivity extends AppCompatActivity
{
    private Button Button_back;
    private float Age;
    private float PBE;
    private float STF;
    private float time;
    private float UVI;
    private float EUV;
    private TextView uvi;
    private TextView euv;
    private TextView euvb;
    private TextView vitb;
    private TextView ery;

    public double get_euvb(float euv_1){
        double[] perlist = new double[]{0.574,0.574,0.569,0.587,0.583,0.599,0.655,0.629,0.602,0.603,0.591,0.575};
        Calendar dt;
        String format_time;
        SimpleDateFormat format;

        format = new SimpleDateFormat("MM");
        dt = Calendar.getInstance();
        format_time = format.format(dt.getTime());
        return (euv_1*perlist[Integer.parseInt(format_time)-1]);

    }

    public boolean what_ery(float euv_1, float STF_1, float time_1){
        double[] typelist = new double[]{2,2.5,3,4.5,6,6};
        double med = typelist[(int)STF_1-1]*100;
        double ery = euv_1*60*time_1;
        if(med>=ery)
            return true;
        else
            return false;
    }

    public double get_vitb(float euv, float age,float pbe,float stf,float time){
        double[] typelist = new double[]{2,2.5,3,4.5,6,6};
        double[] n_list;

        double duv_out = euv*1.029*0.655;
        double vud = duv_out*60*time;
        double af;
        if(age<=21){
            af=1.0;
        }
        else if(22<=age&&age<=40){
            af=0.8;
        }
        else if(41<=age&&age<=59){
            af=0.66;
        }
        else{
            af=0.49;
        }
        double vitD= 49*vud*typelist[(int)stf-1]*pbe*af;
        return vitD;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uvinfo);
        Intent intent=new Intent(this.getIntent());
        UVI = Float.parseFloat(intent.getExtras().getString("UVI"));
        Age = Float.parseFloat(intent.getExtras().getString("age"));
        PBE = Float.parseFloat(intent.getExtras().getString("PBE"));
        STF = Float.parseFloat(intent.getExtras().getString("STF"));
        time = Float.parseFloat(intent.getExtras().getString("time"));
        EUV = UVI/40;

        uvi = (TextView) findViewById(R.id.uvi);
        euv = (TextView) findViewById(R.id.euv);
        euvb = (TextView) findViewById(R.id.euvb);
        vitb = (TextView) findViewById(R.id.vitb);
        ery = (TextView) findViewById(R.id.ery);

        uvi.setText(Float.toString(UVI));
        euv.setText(Float.toString(EUV));
        euvb.setText(Float.toString((float)get_euvb(EUV)));


        vitb.setText("예상 비타민D 합성량\n하루권장 합성량 : 400IU\n - 사용자설정("+(int)time+"분)기준 \n\t"+ (int)get_vitb(EUV,Age,PBE,STF,time) +"IU 합성");

        if(what_ery(EUV, STF, time)){
            ery.setText("홍반 발생여부\n - 미발생");
        }
        else{
            ery.setText("홍반 발생여부\n - 발생! / 주의요망");
        }



        Button_back = (Button) findViewById(R.id.go_home2);
        Button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent();
//                intent.putExtra();
                setResult(RESULT_OK, intent1);
                finish();
            }
        });
    }
}
